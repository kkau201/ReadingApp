package com.example.readingapp.auth

import com.example.readingapp.model.MUser
import com.example.readingapp.repo.FireRepository.Companion.USERS_COLLECTION
import com.google.firebase.Firebase
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

/** Firebase Auth sign-in plus the matching Firestore user record. */
interface AuthRepository {
    suspend fun signInWithEmail(email: String, password: String): AuthUser
    suspend fun registerWithEmail(email: String, password: String): AuthUser
    suspend fun signInWithGoogle(idToken: String): AuthUser
    suspend fun userDocumentExists(uid: String): Boolean
    suspend fun createUserDocument(uid: String, displayName: String)
}

class FirebaseAuthRepository @Inject constructor() : AuthRepository {

    private fun users(): CollectionReference =
        FirebaseFirestore.getInstance().collection(USERS_COLLECTION)

    override suspend fun signInWithEmail(email: String, password: String): AuthUser {
        return Firebase.auth.signInWithEmailAndPassword(email, password).await().toAuthUser()
    }

    override suspend fun registerWithEmail(email: String, password: String): AuthUser {
        return Firebase.auth.createUserWithEmailAndPassword(email, password).await().toAuthUser()
    }

    override suspend fun signInWithGoogle(idToken: String): AuthUser {
        return Firebase.auth
            .signInWithCredential(GoogleAuthProvider.getCredential(idToken, null))
            .await()
            .toAuthUser()
    }

    private fun AuthResult.toAuthUser(): AuthUser {
        val user = user ?: throw IllegalStateException("No Firebase user after sign-in")
        return AuthUser(uid = user.uid, displayName = user.displayName, email = user.email)
    }

    override suspend fun userDocumentExists(uid: String): Boolean {
        return users().document(uid).get().await().exists()
    }

    override suspend fun createUserDocument(uid: String, displayName: String) {
        val user = MUser(userId = uid, displayName = displayName).toMap()
        users().document(uid).set(user).await()
    }
}
