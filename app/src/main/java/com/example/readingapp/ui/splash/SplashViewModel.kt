package com.example.readingapp.ui.splash

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.readingapp.R
import com.example.readingapp.auth.AuthRepository
import com.example.readingapp.common.BaseViewModel
import com.example.readingapp.common.DependencyContextWrapper
import com.example.readingapp.nav.NavigateTo
import com.example.readingapp.repo.FireRepository
import com.example.readingapp.ui.components.DialogState
import com.example.readingapp.ui.destinations.DashboardScreenDestination
import com.example.readingapp.ui.destinations.LoginScreenDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class SplashViewModel @Inject constructor(
    dependencyContextWrapper: DependencyContextWrapper,
    private val fireRepository: FireRepository,
    private val authRepository: AuthRepository
) : BaseViewModel(dependencyContextWrapper) {
    override val isNavigationDestination: Boolean = true

    init {
        viewModelScope.launch {
            // Brief pause so the splash animation is visible.
            delay(SPLASH_MILLIS.milliseconds)

            val user = authRepository.currentUser
            if (user == null) {
                Log.d(TAG, "Not signed in: navigate to login screen")
                navigate(NavigateTo(to = LoginScreenDestination, popCurrent = true))
                return@launch
            }

            // A signed-in user may have no profile if its write failed during sign-up:
            // create it here instead of sending them through the login screen to repair it.
            try {
                authRepository.ensureUserDocument(user)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.d(TAG, "Couldn't ensure user document: $e")
            }

            val result = fireRepository.fetchUser(user.uid)
            if (result.data != null) {
                Log.d(TAG, "Navigate to home screen")
                navigate(NavigateTo(to = DashboardScreenDestination, popCurrent = true))
            } else {
                showErrorLoadingUser()
            }
        }
    }

    private fun showErrorLoadingUser() {
        Log.e(TAG, "Error loading user")
        showDialog(
            DialogState(
                title = getString(R.string.login_error_title),
                message = getString(R.string.login_error_message),
                primaryButtonText = getString(R.string.login_error_primary_btn),
                onPrimaryClick = {
                    dismissDialog()
                    navigate(NavigateTo(to = LoginScreenDestination, popCurrent = true))
                }
            )
        )
    }

    private companion object {
        const val TAG = "SplashViewModel"
        const val SPLASH_MILLIS = 1000L
    }
}
