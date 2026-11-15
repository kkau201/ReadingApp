package com.example.readingapp.auth

import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException

/** Why an auth operation failed, independent of Firebase, so the UI can show a clear message. */
enum class AuthFailure {
    /** Wrong password, no such account, or an account that only uses Google sign-in. */
    INVALID_CREDENTIALS,
    INVALID_EMAIL,
    /** Registering with an email that already has an account (password or Google). */
    EMAIL_IN_USE,
    /** Google sign-in for an email that already has a password account (non-Gmail addresses). */
    ACCOUNT_EXISTS_WITH_DIFFERENT_SIGN_IN,
    WEAK_PASSWORD,
    USER_DISABLED,
    TOO_MANY_REQUESTS,
    NETWORK,
    /** Deleting the account needs a recent sign-in. */
    REQUIRES_RECENT_LOGIN,
    /** Reauthenticated with a different account than the one signed in (e.g. another Google account). */
    WRONG_ACCOUNT,
    UNKNOWN
}

class AuthException(val failure: AuthFailure, cause: Throwable? = null) :
    Exception("Auth failed: $failure", cause)

/** Maps a Firebase exception to an [AuthFailure]. Order matters: some types extend others. */
internal fun FirebaseException.toAuthFailure(): AuthFailure = when (this) {
    is FirebaseAuthException if errorCode == "ERROR_USER_MISMATCH" -> AuthFailure.WRONG_ACCOUNT
    is FirebaseNetworkException -> AuthFailure.NETWORK
    is FirebaseTooManyRequestsException -> AuthFailure.TOO_MANY_REQUESTS
    is FirebaseAuthRecentLoginRequiredException -> AuthFailure.REQUIRES_RECENT_LOGIN
    // Extends FirebaseAuthInvalidCredentialsException, so it must come first.
    is FirebaseAuthWeakPasswordException -> AuthFailure.WEAK_PASSWORD
    is FirebaseAuthUserCollisionException ->
        if (errorCode == "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL") {
            AuthFailure.ACCOUNT_EXISTS_WITH_DIFFERENT_SIGN_IN
        } else {
            AuthFailure.EMAIL_IN_USE
        }
    is FirebaseAuthInvalidCredentialsException ->
        if (errorCode == "ERROR_INVALID_EMAIL") AuthFailure.INVALID_EMAIL else AuthFailure.INVALID_CREDENTIALS
    is FirebaseAuthInvalidUserException ->
        if (errorCode == "ERROR_USER_DISABLED") AuthFailure.USER_DISABLED else AuthFailure.INVALID_CREDENTIALS
    else -> AuthFailure.UNKNOWN
}
