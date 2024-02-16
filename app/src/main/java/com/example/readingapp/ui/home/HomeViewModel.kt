package com.example.readingapp.ui.home

import com.example.readingapp.common.BaseViewModel
import com.example.readingapp.nav.NavigateTo
import com.example.readingapp.ui.destinations.LoginScreenDestination
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor() : BaseViewModel() {
    override val isNavigationDestination: Boolean = true

    fun logout() {
        FirebaseAuth.getInstance().signOut()
        navigate(NavigateTo(LoginScreenDestination))
    }
}