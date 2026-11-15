package com.example.readingapp.ui.user

import android.util.Log
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.viewModelScope
import com.example.readingapp.R
import com.example.readingapp.auth.AuthException
import com.example.readingapp.auth.AuthFailure
import com.example.readingapp.auth.AuthRepository
import com.example.readingapp.auth.EmailVerificationStatus
import com.example.readingapp.auth.GoogleIdTokenProvider
import com.example.readingapp.auth.NoActivityException
import com.example.readingapp.auth.ReauthMethod
import com.example.readingapp.common.BaseViewModel
import com.example.readingapp.common.DependencyContextWrapper
import com.example.readingapp.common.ErrorType
import com.example.readingapp.common.LoadingState
import com.example.readingapp.common.messageRes
import com.example.readingapp.nav.NavigateTo
import com.example.readingapp.repo.FireRepository
import com.example.readingapp.ui.components.DialogPasswordInput
import com.example.readingapp.ui.components.DialogState
import com.example.readingapp.ui.destinations.LoginScreenDestination
import com.example.readingapp.ui.destinations.UpdateUserScreenDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class UserViewModel @Inject constructor(
    private val fireRepository: FireRepository,
    private val authRepository: AuthRepository,
    private val googleIdTokenProvider: GoogleIdTokenProvider,
    dependencyContextWrapper: DependencyContextWrapper
) : BaseViewModel(dependencyContextWrapper), DefaultLifecycleObserver {
    val uiState: StateFlow<UserUiState>
        field: MutableStateFlow<UserUiState> = MutableStateFlow(UserUiState())

    private var isWatchingProfile = false

    /** "Not now" on the verify-email card hides it until the app is next opened. */
    private var verifyReminderDismissed = false

    fun loadUser() {
        // Runs on every resume, so a link clicked in the email app is picked up on return.
        checkEmailVerification()
        if (!isWatchingProfile) {
            isWatchingProfile = true
            viewModelScope.launch {
                fireRepository.user.collect { user ->
                    uiState.update { it.copy(user = user) }
                }
            }
            viewModelScope.launch {
                fireRepository.savedBooks.collect { books ->
                    uiState.update { it.copy(books = books) }
                }
            }
        }
        viewModelScope.launch {
            try {
                updateLoadingState(LoadingState.Loading())
                val currentUserId = authRepository.currentUserId
                    ?: throw ErrorType.UnknownUserException
                fireRepository.fetchUser(currentUserId).getOrThrow()
                fireRepository.fetchSavedBooksByUser()
                updateLoadingState(LoadingState.Success)
            } catch (e: Exception) {
                updateLoadingState(LoadingState.Failed(e.message))
                showErrorDialog(e)
            }
        }
    }

    fun navigateToUpdateUser() {
        navigate(NavigateTo(UpdateUserScreenDestination))
    }

    private fun checkEmailVerification() {
        viewModelScope.launch {
            val email = authRepository.currentUser?.email
            when (authRepository.checkEmailVerification()) {
                EmailVerificationStatus.PENDING -> uiState.update {
                    it.copy(unverifiedEmail = email.takeUnless { verifyReminderDismissed })
                }
                // Shown even if "Not now" was tapped: it's good news, and shown only once.
                EmailVerificationStatus.JUST_VERIFIED -> uiState.update {
                    it.copy(unverifiedEmail = null, emailJustVerified = true)
                }
                EmailVerificationStatus.NOT_NEEDED -> uiState.update {
                    it.copy(unverifiedEmail = null)
                }
            }
        }
    }

    /** The "Email verified" card has been on screen long enough; let it go. */
    fun onEmailVerifiedShown() {
        uiState.update { it.copy(emailJustVerified = false) }
    }

    fun onResendVerificationClick() {
        if (uiState.value.verificationResent) return
        viewModelScope.launch {
            if (authRepository.sendEmailVerification()) {
                uiState.update { it.copy(verificationResent = true) }
                showToast(getString(R.string.verify_email_resent))
            } else {
                showToast(getString(R.string.verify_email_resend_failed))
            }
        }
    }

    fun onDismissVerificationClick() {
        verifyReminderDismissed = true
        uiState.update { it.copy(unverifiedEmail = null) }
    }

    fun onLogoutClick() {
        showDialog(
            DialogState(
                title = getString(R.string.logout_title),
                message = getString(R.string.logout_message),
                primaryButtonText = getString(R.string.logout_yes),
                onPrimaryClick = {
                    dismissDialog()
                    logOut()
                },
                secondaryButtonText = getString(R.string.logout_no),
                onSecondaryClick = { dismissDialog() }
            )
        )
    }

    /**
     * Firebase only deletes an account after a recent sign-in, so the user confirms who they are
     * on the spot: with the Google account picker, or by typing their password.
     */
    fun onDeleteAccountClick() {
        when (authRepository.reauthMethod()) {
            ReauthMethod.GOOGLE -> showGoogleDeleteDialog()
            ReauthMethod.PASSWORD -> showPasswordDeleteDialog(password = "", error = null)
        }
    }

    private fun showGoogleDeleteDialog() {
        showDialog(
            DialogState(
                title = getString(R.string.delete_account_title),
                message = getString(R.string.delete_account_message_google),
                primaryButtonText = getString(R.string.delete_account_confirm),
                onPrimaryClick = {
                    dismissDialog()
                    deleteAccount {
                        authRepository.reauthenticateWithGoogle(googleIdTokenProvider.requestIdToken())
                    }
                },
                secondaryButtonText = getString(R.string.update_cancel),
                onSecondaryClick = { dismissDialog() }
            )
        )
    }

    private fun showPasswordDeleteDialog(password: String, error: String?) {
        showDialog(
            DialogState(
                title = getString(R.string.delete_account_title),
                message = getString(R.string.delete_account_message_password),
                passwordInput = DialogPasswordInput(
                    label = getString(R.string.delete_account_password_label),
                    value = password,
                    error = error,
                    onValueChange = { showPasswordDeleteDialog(password = it, error = null) }
                ),
                primaryButtonText = getString(R.string.delete_account_confirm),
                onPrimaryClick = {
                    if (password.isEmpty()) {
                        showPasswordDeleteDialog(
                            password = password,
                            error = getString(R.string.delete_account_password_required)
                        )
                    } else {
                        dismissDialog()
                        deleteAccount(
                            onWrongPassword = {
                                showPasswordDeleteDialog(
                                    password = "",
                                    error = getString(R.string.delete_account_wrong_password)
                                )
                            }
                        ) {
                            authRepository.reauthenticateWithPassword(password)
                        }
                    }
                },
                secondaryButtonText = getString(R.string.update_cancel),
                onSecondaryClick = { dismissDialog() }
            )
        )
    }

    /**
     * Runs [reauthenticate], then deletes the account. Nothing is deleted unless the user
     * successfully confirms who they are first.
     */
    private fun deleteAccount(
        onWrongPassword: (() -> Unit)? = null,
        reauthenticate: suspend () -> Unit
    ) {
        if (getLoadingState().value is LoadingState.Loading) return
        viewModelScope.launch {
            updateLoadingState(LoadingState.Loading(LoadingState.LoadingType.FULL_SCREEN))
            try {
                reauthenticate()
                authRepository.deleteAccount()
                fireRepository.clearUserData()
                showToast(getString(R.string.delete_account_done))
                navigate(NavigateTo(LoginScreenDestination, clearBackStack = true))
            } catch (e: GetCredentialCancellationException) {
                // User closed the Google account picker: they changed their mind.
                Log.d(TAG, "Delete account cancelled: ${e.message}")
            } catch (e: CancellationException) {
                throw e
            } catch (e: AuthException) {
                Log.d(TAG, "Delete account failed: $e")
                if (e.failure == AuthFailure.INVALID_CREDENTIALS && onWrongPassword != null) {
                    onWrongPassword()
                } else {
                    showToast(getString(e.failure.messageRes()))
                }
            } catch (e: Exception) {
                Log.d(TAG, "Delete account failed: $e")
                val message = when (e) {
                    is NoActivityException, is GetCredentialException -> R.string.google_sign_in_failed
                    else -> R.string.delete_account_error
                }
                showToast(getString(message))
            } finally {
                updateLoadingState(LoadingState.Idle)
            }
        }
    }

    private fun logOut() {
        viewModelScope.launch {
            authRepository.signOut()
            fireRepository.clearUserData()
            // Drop every signed-in screen so Back can't return to them.
            navigate(NavigateTo(LoginScreenDestination, clearBackStack = true))
        }
    }

    private companion object {
        const val TAG = "UserViewModel"
    }
}
