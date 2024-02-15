package com.example.readingapp.common

import androidx.lifecycle.ViewModel
import com.example.readingapp.MainViewModel
import com.example.readingapp.nav.NavigateBack
import com.example.readingapp.nav.NavigationEvent

abstract class BaseViewModel() : ViewModel() {
    private lateinit var mainViewModel: MainViewModel

    /**
     * If this model is owned by a navigation destination
     * (a composable with `@Destination` annotation)
     */
    open val isNavigationDestination: Boolean = false

    fun setMainViewModel(main: MainViewModel) {
        mainViewModel = main
    }

    fun navigate(navigationEvent: NavigationEvent) {
        mainViewModel.navigate(navigationEvent)
    }

    fun navigateBack() {
        mainViewModel.navigate(NavigateBack)
    }
}