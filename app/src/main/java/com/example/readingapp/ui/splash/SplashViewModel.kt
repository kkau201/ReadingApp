package com.example.readingapp.ui.splash

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.readingapp.R
import com.example.readingapp.common.BaseViewModel
import com.example.readingapp.common.DependencyContextWrapper
import com.example.readingapp.data.DataOrException
import com.example.readingapp.model.MUser
import com.example.readingapp.nav.NavigateTo
import com.example.readingapp.repo.FireRepository
import com.example.readingapp.ui.components.DialogState
import com.example.readingapp.ui.destinations.HomeScreenDestination
import com.example.readingapp.ui.destinations.LoginScreenDestination
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    dependencyContextWrapper: DependencyContextWrapper,
    fireRepository: FireRepository
) : BaseViewModel(dependencyContextWrapper) {
    override val isNavigationDestination: Boolean = true

    val userData: MutableStateFlow<DataOrException<MUser?, Boolean, Exception>> =
        MutableStateFlow(DataOrException())

    init {
        viewModelScope.launch {
            delay(1000)
            // Show splash screen for 5 seconds
            val user = FirebaseAuth.getInstance().currentUser
            if (user?.email.isNullOrEmpty()) {
                Log.d("SplashViewModel", "Navigate to login screen")
                delay(4000)
                navigate(NavigateTo(to = LoginScreenDestination, popCurrent = true))
            }
            else {
                Log.d("SplashViewModel", "Navigate to home screen")
                userData.update { fireRepository.getUserDetailsFromDatabase(user?.uid) }
                userData.value.data?.let { updateUser(it) }
            }
        }
    }

    fun navigateToHomeScreen() {
        navigate(NavigateTo(to = HomeScreenDestination, popCurrent = true))
    }

    fun showErrorLoadingUser() {
        Log.e("ReadingAppTesting", "Error loading user")
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
}