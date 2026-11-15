package com.example.readingapp.auth

class FakeAuthRepository : AuthRepository {
    var signedInUser =
        AuthUser(uid = "uid-1", displayName = "Jane Austen", email = "jane@example.com")

    /** The user returned by [currentUser]; null means signed out. Set by sign-in calls. */
    var current: AuthUser? = null
    override val currentUser: AuthUser? get() = current

    /** Thrown by every sign-in / register call when set. */
    var signInError: Exception? = null

    /** Thrown by [createUserDocument] when set (e.g. a network or rules failure). */
    var createDocumentError: Exception? = null

    var verificationEmailSends = true
    var verificationStatus = EmailVerificationStatus.NOT_NEEDED
    var passwordResetError: Exception? = null
    var reauthMethod = ReauthMethod.GOOGLE
    var reauthError: Exception? = null
    var deleteError: Exception? = null

    val existingUserDocuments = mutableSetOf<String>()
    val createdUserDocuments = mutableListOf<Pair<String, String>>()
    val receivedIdTokens = mutableListOf<String>()
    val emailSignIns = mutableListOf<Pair<String, String>>()
    val emailRegistrations = mutableListOf<Pair<String, String>>()
    val passwordResetEmails = mutableListOf<String>()
    var verificationEmailsRequested = 0
    var signOutCount = 0
    val googleReauthTokens = mutableListOf<String>()
    val passwordReauths = mutableListOf<String>()
    var deleteCount = 0

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

    override suspend fun sendEmailVerification(): Boolean {
        verificationEmailsRequested++
        return verificationEmailSends
    }

    override suspend fun checkEmailVerification(): EmailVerificationStatus = verificationStatus

    override suspend fun sendPasswordResetEmail(email: String) {
        passwordResetEmails += email
        passwordResetError?.let { throw it }
    }

    override suspend fun signOut() {
        signOutCount++
        current = null
    }

    override fun reauthMethod(): ReauthMethod = reauthMethod

    override suspend fun reauthenticateWithGoogle(idToken: String) {
        googleReauthTokens += idToken
        reauthError?.let { throw it }
    }

    override suspend fun reauthenticateWithPassword(password: String) {
        passwordReauths += password
        reauthError?.let { throw it }
    }

    override suspend fun deleteAccount() {
        deleteCount++
        deleteError?.let { throw it }
        current = null
    }

    override suspend fun userDocumentExists(uid: String): Boolean = uid in existingUserDocuments

    override suspend fun createUserDocument(uid: String, displayName: String) {
        createDocumentError?.let { throw it }
        createdUserDocuments += uid to displayName
        existingUserDocuments += uid
    }

    private fun result(): AuthUser {
        signInError?.let { throw it }
        current = signedInUser
        return signedInUser
    }
}
