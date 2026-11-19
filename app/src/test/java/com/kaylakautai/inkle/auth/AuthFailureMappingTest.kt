package com.kaylakautai.inkle.auth

import com.kaylakautai.inkle.R
import com.kaylakautai.inkle.common.messageRes
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

/**
 * How Firebase Auth failures become [AuthFailure]s (and so which message the user sees).
 * Uses Firebase's real exception types and the error codes it sends.
 */
class AuthFailureMappingTest {

    private fun assertMaps(expected: AuthFailure, e: FirebaseException) =
        assertEquals("${e::class.simpleName} ${(e as? FirebaseAuthException)?.errorCode}", expected, e.toAuthFailure())

    // region Signing in

    @Test
    fun wrongPasswordOrUnknownAccount_isInvalidCredentials() {
        // Newer Firebase projects (email enumeration protection) send ERROR_INVALID_CREDENTIAL
        // for both; older ones send the specific codes.
        assertMaps(AuthFailure.INVALID_CREDENTIALS, invalidCredentials("ERROR_INVALID_CREDENTIAL"))
        assertMaps(AuthFailure.INVALID_CREDENTIALS, invalidCredentials("ERROR_WRONG_PASSWORD"))
        assertMaps(AuthFailure.INVALID_CREDENTIALS, invalidUser("ERROR_USER_NOT_FOUND"))
    }

    @Test
    fun malformedEmail_isInvalidEmail() {
        assertMaps(AuthFailure.INVALID_EMAIL, invalidCredentials("ERROR_INVALID_EMAIL"))
    }

    @Test
    fun disabledAccount_isUserDisabled() {
        assertMaps(AuthFailure.USER_DISABLED, invalidUser("ERROR_USER_DISABLED"))
    }

    @Test
    fun googleSignInForPasswordAccount_isAccountExistsWithDifferentSignIn() {
        assertMaps(
            AuthFailure.ACCOUNT_EXISTS_WITH_DIFFERENT_SIGN_IN,
            collision("ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL")
        )
    }

    // endregion

    // region Registering

    @Test
    fun emailAlreadyRegistered_isEmailInUse() {
        assertMaps(AuthFailure.EMAIL_IN_USE, collision("ERROR_EMAIL_ALREADY_IN_USE"))
        assertMaps(AuthFailure.EMAIL_IN_USE, collision("ERROR_CREDENTIAL_ALREADY_IN_USE"))
    }

    @Test
    fun weakPassword_isWeakPassword_notInvalidCredentials() {
        // FirebaseAuthWeakPasswordException extends FirebaseAuthInvalidCredentialsException,
        // so this checks the more specific case is matched first.
        assertMaps(
            AuthFailure.WEAK_PASSWORD,
            FirebaseAuthWeakPasswordException("ERROR_WEAK_PASSWORD", "weak", "too short")
        )
    }

    // endregion

    // region Reauthenticating and deleting

    @Test
    fun differentAccountPicked_isWrongAccount_whicheverTypeFirebaseUses() {
        // The code is checked before the type, so a mismatch is never reported as a bad password.
        assertMaps(AuthFailure.WRONG_ACCOUNT, invalidUser("ERROR_USER_MISMATCH"))
        assertMaps(AuthFailure.WRONG_ACCOUNT, invalidCredentials("ERROR_USER_MISMATCH"))
        assertMaps(AuthFailure.WRONG_ACCOUNT, FirebaseAuthException("ERROR_USER_MISMATCH", "mismatch"))
    }

    @Test
    fun staleSignIn_isRequiresRecentLogin() {
        assertMaps(
            AuthFailure.REQUIRES_RECENT_LOGIN,
            FirebaseAuthRecentLoginRequiredException("ERROR_REQUIRES_RECENT_LOGIN", "stale")
        )
    }

    // endregion

    // region Any operation

    @Test
    fun offline_isNetwork() {
        assertMaps(AuthFailure.NETWORK, FirebaseNetworkException("timeout"))
    }

    @Test
    fun throttled_isTooManyRequests() {
        assertMaps(AuthFailure.TOO_MANY_REQUESTS, FirebaseTooManyRequestsException("slow down"))
    }

    @Test
    fun anythingElse_isUnknown() {
        assertMaps(AuthFailure.UNKNOWN, FirebaseAuthException("ERROR_INTERNAL_ERROR", "internal"))
        assertMaps(AuthFailure.UNKNOWN, FirebaseException("something new"))
    }

    // endregion

    // region What the user is told

    @Test
    fun eachFailure_hasItsOwnMessage() {
        val messages = AuthFailure.entries.associateWith { it.messageRes() }

        assertEquals(R.string.auth_error_invalid_credentials, messages[AuthFailure.INVALID_CREDENTIALS])
        assertEquals(R.string.auth_error_use_password, messages[AuthFailure.ACCOUNT_EXISTS_WITH_DIFFERENT_SIGN_IN])
        assertEquals(R.string.auth_error_wrong_account, messages[AuthFailure.WRONG_ACCOUNT])
        assertEquals(R.string.sign_in_failed, messages[AuthFailure.UNKNOWN])
        // Only UNKNOWN falls back to the generic message.
        assertEquals(
            listOf(AuthFailure.UNKNOWN),
            messages.filterValues { it == R.string.sign_in_failed }.keys.toList()
        )
        assertEquals(messages.size, messages.values.toSet().size)
    }

    @Test
    fun authException_keepsFirebaseCauseForLogs() {
        val cause = FirebaseNetworkException("timeout")

        val e = AuthException(cause.toAuthFailure(), cause)

        assertEquals(AuthFailure.NETWORK, e.failure)
        assertSame(cause, e.cause)
    }

    // endregion

    private fun invalidCredentials(code: String) = FirebaseAuthInvalidCredentialsException(code, code)
    private fun invalidUser(code: String) = FirebaseAuthInvalidUserException(code, code)
    private fun collision(code: String) = FirebaseAuthUserCollisionException(code, code)
}