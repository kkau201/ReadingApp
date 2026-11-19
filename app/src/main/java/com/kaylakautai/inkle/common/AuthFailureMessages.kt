package com.kaylakautai.inkle.common

import androidx.annotation.StringRes
import com.kaylakautai.inkle.R
import com.kaylakautai.inkle.auth.AuthFailure

/** User-facing message for an auth failure. */
@StringRes
fun AuthFailure.messageRes(): Int = when (this) {
    AuthFailure.INVALID_CREDENTIALS -> R.string.auth_error_invalid_credentials
    AuthFailure.INVALID_EMAIL -> R.string.auth_error_invalid_email
    AuthFailure.EMAIL_IN_USE -> R.string.auth_error_email_in_use
    AuthFailure.ACCOUNT_EXISTS_WITH_DIFFERENT_SIGN_IN -> R.string.auth_error_use_password
    AuthFailure.WEAK_PASSWORD -> R.string.auth_error_weak_password
    AuthFailure.USER_DISABLED -> R.string.auth_error_user_disabled
    AuthFailure.TOO_MANY_REQUESTS -> R.string.auth_error_too_many_requests
    AuthFailure.NETWORK -> R.string.auth_error_network
    AuthFailure.REQUIRES_RECENT_LOGIN -> R.string.auth_error_recent_login
    AuthFailure.WRONG_ACCOUNT -> R.string.auth_error_wrong_account
    AuthFailure.UNKNOWN -> R.string.sign_in_failed
}
