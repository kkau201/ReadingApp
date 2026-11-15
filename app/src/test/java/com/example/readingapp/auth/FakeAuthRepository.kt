package com.example.readingapp.auth

class FakeAuthRepository : AuthRepository {
    var signedInUser = AuthUser(uid = "uid-1", displayName = "Jane Austen", email = "jane@example.com")

    /** Thrown by every sign-in / register call when set. */
    var signInError: Exception? = null

    val existingUserDocuments = mutableSetOf<String>()
    val createdUserDocuments = mutableListOf<Pair<String, String>>()
    val receivedIdTokens = mutableListOf<String>()
    val emailSignIns = mutableListOf<Pair<String, String>>()
    val emailRegistrations = mutableListOf<Pair<String, String>>()

    override suspend fun signInWithEmail(email: String, password: String): AuthUser {
        emailSignIns += email to password
        return result()
    }

    override suspend fun registerWithEmail(email: String, password: String): AuthUser {
        emailRegistrations += email to password
        return result()
    }

    override suspend fun signInWithGoogle(idToken: String): AuthUser {
        receivedIdTokens += idToken
        return result()
    }

    override suspend fun userDocumentExists(uid: String): Boolean = uid in existingUserDocuments

    override suspend fun createUserDocument(uid: String, displayName: String) {
        createdUserDocuments += uid to displayName
        existingUserDocuments += uid
    }

    private fun result(): AuthUser {
        signInError?.let { throw it }
        return signedInUser
    }
}
