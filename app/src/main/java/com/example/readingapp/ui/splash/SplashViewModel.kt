package com.example.readingapp.ui.splash

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.readingapp.common.BaseViewModel
import com.example.readingapp.nav.NavigateTo
import com.example.readingapp.ui.destinations.HomeScreenDestination
import com.example.readingapp.ui.destinations.LoginScreenDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor() : BaseViewModel() {
    override val isNavigationDestination: Boolean = true

    init {
        viewModelScope.launch {
            // Show splash screen for 7 seconds
            delay(7000)
            Log.d("Home", "Navigate to login screen")
            navigate(NavigateTo(to = LoginScreenDestination, popCurrent = true))
        }
    }
}