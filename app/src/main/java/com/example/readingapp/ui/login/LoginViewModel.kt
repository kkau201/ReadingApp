package com.example.readingapp.ui.login

import android.util.Log
import androidx.core.util.PatternsCompat
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.viewModelScope
import com.example.readingapp.R
import com.example.readingapp.auth.AuthRepository
import com.example.readingapp.auth.AuthUser
import com.example.readingapp.auth.GoogleIdTokenProvider
import com.example.readingapp.auth.NoActivityException
import com.example.readingapp.common.BaseViewModel
import com.example.readingapp.common.DependencyContextWrapper
import com.example.readingapp.common.LoadingState
import com.example.readingapp.model.TextLimits
import com.example.readingapp.model.limitTo
import com.example.readingapp.nav.NavigateTo
import com.example.readingapp.ui.destinations.DashboardScreenDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class LoginViewModel @Inject constructor(
    dependencyContextWrapper: DependencyContextWrapper,
    private val googleIdTokenProvider: GoogleIdTokenProvider,
    private val authRepository: AuthRepository
) : BaseViewModel(dependencyContextWrapper) {
    override val isNavigationDestination: Boolean = true

    private val _uiState: MutableStateFlow<LoginUiState> = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    fun onEmailChange(input: String) {
        _uiState.update { it.copy(email = input, isEmailValid = isValidEmail(input)) }
    }

    fun onPasswordChange(input: String) {
        val minimum = if (uiState.value.loginScreenState == LoginScreenState.SIGN_UP) 6 else 1
        val isValid = input.trim().length >= minimum
        _uiState.update { it.copy(password = input, isPasswordValid = isValid) }
    }

    fun updatePasswordVisibility(visible: Boolean) {
        _uiState.update { it.copy(passwordVisibility = visible) }
    }

    fun onSwitchScreens(state: LoginScreenState) {
        _uiState.update {
            it.copy(
                email = "",
                password = "",
                isEmailValid = false,
                isPasswordValid = false,
                loginScreenState = state,
                errorMessage = null
            )
        }
    }

    fun onSubmitClick() {
        if (uiState.value.loginScreenState == LoginScreenState.LOGIN) onLoginClick() else onRegisterClick()
    }

    private fun onLoginClick() = launchSignIn("Email login") {
        // Email is trimmed (keyboards often add a trailing space); the password is sent as typed.
        authRepository.signInWithEmail(uiState.value.email.trim(), uiState.value.password)
    }

    private fun onRegisterClick() = launchSignIn("Register") {
        authRepository.registerWithEmail(uiState.value.email.trim(), uiState.value.password)
    }

    fun onGoogleSignInClick() = launchSignIn("Google sign-in") {
        val idToken = googleIdTokenProvider.requestIdToken()
        authRepository.signInWithGoogle(idToken)
    }

    /**
     * Creates the user's profile document if it's missing. Runs after every sign-in, because
     * creating the Auth account and writing the profile aren't atomic: if the write failed during
     * sign-up (or an older build never wrote one), the next sign-in repairs it.
     */
    private suspend fun ensureUserDocument(user: AuthUser) {
        if (!authRepository.userDocumentExists(user.uid)) {
            authRepository.createUserDocument(
                user.uid,
                defaultDisplayName(user.displayName, user.email)
            )
        }
    }

    /**
     * Runs a sign-in [action] with the shared loading/error handling, makes sure the signed-in
     * user has a profile document, then opens the Dashboard.
     * Ignored while another sign-in is already loading.
     */
    private fun launchSignIn(name: String, action: suspend () -> AuthUser) {
        Log.d(TAG, "$name clicked")
        _uiState.update { it.copy(errorMessage = null) }
        if (getLoadingState().value is LoadingState.Loading) return

        viewModelScope.launch {
            updateLoadingState(LoadingState.Loading())
            try {
                ensureUserDocument(action())
                navigate(NavigateTo(to = DashboardScreenDestination, popCurrent = true))
            } catch (e: GetCredentialCancellationException) {
                // User dismissed the Google account picker: not an error.
                Log.d(TAG, "$name cancelled: ${e.message}")
            } catch (e: CancellationException) {
                // The coroutine itself was cancelled (e.g. screen closed); let it propagate.
                throw e
            } catch (e: Exception) {
                Log.d(TAG, "$name failed: $e")
                _uiState.update { it.copy(errorMessage = errorMessageFor(e)) }
            } finally {
                // Loading state lives in the Activity-scoped MainViewModel, so always reset it,
                // including on cancellation, or the next screen would be stuck loading.
                updateLoadingState(LoadingState.Idle)
            }
        }
    }

    private fun errorMessageFor(e: Exception): String = when (e) {
        is NoActivityException, is GetCredentialException -> getString(R.string.google_sign_in_failed)
        else -> e.message ?: getString(R.string.sign_in_failed)
    }

    private companion object {
        const val TAG = "LoginViewModel"
    }
}

/**
 * Basic shape check ("something@domain.tld", surrounding whitespace ignored). Anything stricter
 * is left to Firebase, which rejects malformed addresses itself.
 */
internal fun isValidEmail(input: String): Boolean =
    PatternsCompat.EMAIL_ADDRESS.matcher(input.trim()).matches()

/**
 * Display name for a new user record: the account's name if it has one, otherwise the part of
 * the email before '@', otherwise "Reader". Cut to [TextLimits.DISPLAY_NAME].
 */
internal fun defaultDisplayName(displayName: String?, email: String?): String {
    val name = displayName?.takeIf { it.isNotBlank() }
        ?: email?.substringBefore('@').orEmpty().ifBlank { "Reader" }
    // Google names and email prefixes can be longer than firestore.rules allows.
    return name.trim().limitTo(TextLimits.DISPLAY_NAME).trimEnd()
}
