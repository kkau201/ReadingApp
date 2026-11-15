package com.example.readingapp.auth

import com.example.readingapp.model.MUser
import com.example.readingapp.repo.FireRepository.Companion.USERS_COLLECTION
import com.google.firebase.Firebase
import com.google.firebase.FirebaseException
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

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
    suspend fun userDocumentExists(uid: String): Boolean
    suspend fun createUserDocument(uid: String, displayName: String)
}

class FirebaseAuthRepository @Inject constructor() : AuthRepository {

    private fun users(): CollectionReference =
        FirebaseFirestore.getInstance().collection(USERS_COLLECTION)

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

    override suspend fun userDocumentExists(uid: String): Boolean {
        return users().document(uid).get().await().exists()
    }

    override suspend fun createUserDocument(uid: String, displayName: String) {
        val user = MUser(userId = uid, displayName = displayName).toMap()
        users().document(uid).set(user).await()
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
}
