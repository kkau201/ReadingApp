package com.example.readingapp.ui.splash

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.readingapp.common.BaseViewModel
import com.example.readingapp.common.DependencyContextWrapper
import com.example.readingapp.nav.NavigateTo
import com.example.readingapp.ui.destinations.HomeScreenDestination
import com.example.readingapp.ui.destinations.LoginScreenDestination
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    dependencyContextWrapper: DependencyContextWrapper
) : BaseViewModel(dependencyContextWrapper) {
    override val isNavigationDestination: Boolean = true

    init {
        viewModelScope.launch {
            // Show splash screen for 5 seconds
            delay(5000)
            if(FirebaseAuth.getInstance().currentUser?.email.isNullOrEmpty()) {
                Log.d("SplashViewModel", "Navigate to login screen")
                navigate(NavigateTo(to = LoginScreenDestination, popCurrent = true))
            } else {
                Log.d("SplashViewModel", "Navigate to home screen")
                navigate(NavigateTo(to = HomeScreenDestination, popCurrent = true))
            }
        }
    }
}