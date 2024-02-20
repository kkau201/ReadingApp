package com.example.readingapp.ui.user

import com.example.readingapp.common.BaseViewModel
import com.example.readingapp.nav.NavigateTo
import com.example.readingapp.ui.components.DialogState
import com.example.readingapp.ui.destinations.LoginScreenDestination
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(): BaseViewModel() {
    override val isNavigationDestination: Boolean = true

    fun onLogoutClick() {
        showDialog(
            DialogState(
                title = "Logout",
                message = "Are you sure you want to log out?",
                primaryButtonText = "Yes",
                onPrimaryClick = {
                    logout()
                    dismissDialog()
                },
                secondaryButtonText = "No",
                onSecondaryClick = { dismissDialog() }
            )
        )
    }

    private fun logout() {
        FirebaseAuth.getInstance().signOut().run {
            navigate(NavigateTo(LoginScreenDestination))
        }
    }
}