package com.example.readingapp.auth

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import com.example.readingapp.model.MUser
import com.example.readingapp.model.USER_ID
import com.example.readingapp.repo.FireRepository.Companion.BOOKS_COLLECTION
import com.example.readingapp.repo.FireRepository.Companion.SESSIONS_COLLECTION
import com.example.readingapp.repo.FireRepository.Companion.USERS_COLLECTION
import com.google.firebase.Firebase
import com.google.firebase.FirebaseException
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

data class AuthUser(
    val uid: String,
    val displayName: String?,
    val email: String?
)

/**
 * Firebase Auth plus the matching Firestore user record. Failures from Firebase are thrown as
 * [AuthException] so callers can show a clear message without depending on Firebase.
 */
interface AuthRepository {
    /** The signed-in user, or null. */
    val currentUser: AuthUser?
    val currentUserId: String? get() = currentUser?.uid

    suspend fun signInWithEmail(email: String, password: String): AuthUser
    suspend fun registerWithEmail(email: String, password: String): AuthUser
    suspend fun signInWithGoogle(idToken: String): AuthUser

    /** Sends a verification email to the signed-in user. Returns false if it couldn't be sent. */
    suspend fun sendEmailVerification(): Boolean

    /** Firebase doesn't reveal whether the account exists, so this succeeds either way. */
    suspend fun sendPasswordResetEmail(email: String)

    /** Signs out of Firebase and clears Credential Manager's sign-in state. */
    suspend fun signOut()

    /**
     * Whether the last sign-in is recent enough for Firebase to allow deleting the account.
     * Checked before deleting any data, so we don't delete it and then fail to delete the account.
     */
    fun isRecentlySignedIn(): Boolean

    /** Deletes the user's books, sessions and profile, then the account itself. */
    suspend fun deleteAccount()

    suspend fun userDocumentExists(uid: String): Boolean
    suspend fun createUserDocument(uid: String, displayName: String)

    /**
     * Creates the user's profile document if it's missing. Creating the Auth account and writing
     * the profile aren't atomic: if the write failed during sign-up, this repairs it later.
     */
    suspend fun ensureUserDocument(user: AuthUser) {
        if (!userDocumentExists(user.uid)) {
            createUserDocument(user.uid, defaultDisplayName(user.displayName, user.email))
        }
    }
}

class FirebaseAuthRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : AuthRepository {

    private val db: FirebaseFirestore get() = FirebaseFirestore.getInstance()
    private fun users(): CollectionReference = db.collection(USERS_COLLECTION)

    override val currentUser: AuthUser?
        get() = Firebase.auth.currentUser?.let {
            AuthUser(uid = it.uid, displayName = it.displayName, email = it.email)
        }

    override suspend fun signInWithEmail(email: String, password: String): AuthUser = mapErrors {
        Firebase.auth.signInWithEmailAndPassword(email, password).await().toAuthUser()
    }

    override suspend fun registerWithEmail(email: String, password: String): AuthUser = mapErrors {
        Firebase.auth.createUserWithEmailAndPassword(email, password).await().toAuthUser()
    }

    override suspend fun signInWithGoogle(idToken: String): AuthUser = mapErrors {
        Firebase.auth
            .signInWithCredential(GoogleAuthProvider.getCredential(idToken, null))
            .await()
            .toAuthUser()
    }

    override suspend fun sendEmailVerification(): Boolean {
        val user = Firebase.auth.currentUser ?: return false
        return try {
            user.sendEmailVerification().await()
            true
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.d(TAG, "Couldn't send verification email: $e")
            false
        }
    }

    override suspend fun sendPasswordResetEmail(email: String) = mapErrors {
        Firebase.auth.sendPasswordResetEmail(email).await()
        Unit
    }

    override suspend fun signOut() {
        Firebase.auth.signOut()
        clearCredentialState()
    }

    override fun isRecentlySignedIn(): Boolean {
        val lastSignIn = Firebase.auth.currentUser?.metadata?.lastSignInTimestamp ?: return false
        return System.currentTimeMillis() - lastSignIn < RECENT_SIGN_IN_MILLIS
    }

    override suspend fun deleteAccount() = mapErrors {
        val user = Firebase.auth.currentUser ?: throw AuthException(AuthFailure.UNKNOWN)

        // Delete Firestore data first: once the account is gone, the rules no longer let us.
        val owned = listOf(BOOKS_COLLECTION, SESSIONS_COLLECTION).flatMap { collection ->
            db.collection(collection).whereEqualTo(USER_ID, user.uid).get().await()
                .documents.map { it.reference }
        } + users().document(user.uid)
        owned.chunked(MAX_BATCH_WRITES).forEach { chunk ->
            db.batch().apply { chunk.forEach { delete(it) } }.commit().await()
        }

        user.delete().await()
        clearCredentialState()
    }

    override suspend fun userDocumentExists(uid: String): Boolean {
        return users().document(uid).get().await().exists()
    }

    override suspend fun createUserDocument(uid: String, displayName: String) {
        val user = MUser(userId = uid, displayName = displayName).toMap()
        users().document(uid).set(user).await()
    }

    private suspend fun clearCredentialState() {
        // Best effort: signing out of Firebase is what matters; this just resets Google sign-in.
        try {
            CredentialManager.create(context).clearCredentialState(ClearCredentialStateRequest())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.d(TAG, "Couldn't clear credential state: $e")
        }
    }

    private fun AuthResult.toAuthUser(): AuthUser {
        val user = user ?: throw IllegalStateException("No Firebase user after sign-in")
        return AuthUser(uid = user.uid, displayName = user.displayName, email = user.email)
    }

    /** Rethrows Firebase failures as [AuthException]. */
    private suspend fun <T> mapErrors(block: suspend () -> T): T {
        return try {
            block()
        } catch (e: FirebaseException) {
            throw AuthException(e.toAuthFailure(), e)
        }
    }

    private companion object {
        const val TAG = "AuthRepository"

        /** Firebase asks for a fresh sign-in after about 5 minutes for sensitive operations. */
        const val RECENT_SIGN_IN_MILLIS = 5 * 60 * 1000L

        /** Firestore's limit on writes per batch. */
        const val MAX_BATCH_WRITES = 500
    }
}
