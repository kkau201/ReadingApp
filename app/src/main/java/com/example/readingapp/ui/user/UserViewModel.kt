package com.example.readingapp.ui.user

import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.viewModelScope
import com.example.readingapp.R
import com.example.readingapp.auth.AuthException
import com.example.readingapp.auth.AuthFailure
import com.example.readingapp.auth.AuthRepository
import com.example.readingapp.common.BaseViewModel
import com.example.readingapp.common.DependencyContextWrapper
import com.example.readingapp.common.ErrorType
import com.example.readingapp.common.LoadingState
import com.example.readingapp.common.messageRes
import com.example.readingapp.nav.NavigateTo
import com.example.readingapp.repo.FireRepository
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
    dependencyContextWrapper: DependencyContextWrapper
) : BaseViewModel(dependencyContextWrapper), DefaultLifecycleObserver {
    val uiState: StateFlow<UserUiState>
        field: MutableStateFlow<UserUiState> = MutableStateFlow(UserUiState())

    private var isWatchingProfile = false

    fun loadUser() {
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

    fun onDeleteAccountClick() {
        // Firebase only deletes an account after a recent sign-in. Check first, so we never
        // delete the user's data and then fail to delete the account itself.
        if (!authRepository.isRecentlySignedIn()) {
            showSignInAgainDialog()
            return
        }
        showDialog(
            DialogState(
                title = getString(R.string.delete_account_title),
                message = getString(R.string.delete_account_message),
                primaryButtonText = getString(R.string.delete_account_confirm),
                onPrimaryClick = {
                    dismissDialog()
                    deleteAccount()
                },
                secondaryButtonText = getString(R.string.update_cancel),
                onSecondaryClick = { dismissDialog() }
            )
        )
    }

    private fun deleteAccount() {
        if (getLoadingState().value is LoadingState.Loading) return
        viewModelScope.launch {
            updateLoadingState(LoadingState.Loading(LoadingState.LoadingType.FULL_SCREEN))
            try {
                authRepository.deleteAccount()
                fireRepository.clearUserData()
                showToast(getString(R.string.delete_account_done))
                navigate(NavigateTo(LoginScreenDestination, clearBackStack = true))
            } catch (e: CancellationException) {
                throw e
            } catch (e: AuthException) {
                Log.d(TAG, "Delete account failed: $e")
                if (e.failure == AuthFailure.REQUIRES_RECENT_LOGIN) {
                    showSignInAgainDialog()
                } else {
                    showToast(getString(e.failure.messageRes()))
                }
            } catch (e: Exception) {
                Log.d(TAG, "Delete account failed: $e")
                showToast(getString(R.string.delete_account_error))
            } finally {
                updateLoadingState(LoadingState.Idle)
            }
        }
    }

    private fun showSignInAgainDialog() {
        showDialog(
            DialogState(
                title = getString(R.string.delete_account_title),
                message = getString(R.string.delete_account_sign_in_again),
                primaryButtonText = getString(R.string.profile_logout),
                onPrimaryClick = {
                    dismissDialog()
                    logOut()
                },
                secondaryButtonText = getString(R.string.update_cancel),
                onSecondaryClick = { dismissDialog() }
            )
        )
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
