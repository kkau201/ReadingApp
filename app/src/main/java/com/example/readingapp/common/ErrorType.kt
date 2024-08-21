package com.example.readingapp.common

import androidx.annotation.StringRes
import com.example.readingapp.R
import java.io.IOException

sealed interface ErrorType {
    @get:StringRes
    val title: Int
        get() = R.string.error_title

    @get:StringRes
    val body: Int

    @get:StringRes
    val primaryBtn: Int


    data object NoInternetException : IOException(), ErrorType {
        private fun readResolve(): Any = NoInternetException
        override val body = R.string.error_no_internet
        override val primaryBtn = R.string.error_ok
    }

    data class TimeoutException(override val cause: Throwable?) : IOException(), ErrorType {
        override val body = R.string.error_network
        override val primaryBtn = R.string.error_try_again
    }

    data class DataRequestException(override val message: String) : IOException(), ErrorType {
        override val body = R.string.error_bad_response
        override val primaryBtn = R.string.error_try_again
    }

    data class ServiceException(override val message: String) : IOException(), ErrorType {
        override val body = R.string.error_http
        override val primaryBtn = R.string.error_try_again
    }

    data class NetworkException(override val cause: Throwable?) : IOException(), ErrorType {
        override val body = R.string.error_network
        override val primaryBtn = R.string.error_try_again
    }

    data class UnknownNetworkException(override val cause: Throwable?) : IOException(), ErrorType {
        override val body: Int = R.string.error_unknown
        override val primaryBtn: Int = R.string.error_try_again
    }

    data object UnknownBookException : IOException(), ErrorType {
        private fun readResolve(): Any = UnknownBookException
        override val body: Int = R.string.error_unknown_book
        override val primaryBtn: Int = R.string.error_ok
    }

    data object UnknownUserException : IOException(), ErrorType {
        private fun readResolve(): Any = UnknownUserException
        override val body: Int = R.string.error_unknown_user
        override val primaryBtn: Int = R.string.error_ok
    }
}