package com.example.readingapp.ui.user

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.viewModelScope
import com.example.readingapp.R
import com.example.readingapp.common.BaseViewModel
import com.example.readingapp.common.DependencyContextWrapper
import com.example.readingapp.nav.NavigateTo
import com.example.readingapp.repo.FireRepository
import com.example.readingapp.ui.components.DialogState
import com.example.readingapp.ui.destinations.LoginScreenDestination
import com.example.readingapp.ui.destinations.UpdateScreenDestination
import com.example.readingapp.ui.destinations.UpdateUserScreenDestination
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val fireRepository: FireRepository,
    dependencyContextWrapper: DependencyContextWrapper
) : BaseViewModel(dependencyContextWrapper), DefaultLifecycleObserver {
    override val isNavigationDestination: Boolean = true

    private val _uiState: MutableStateFlow<UserUiState> = MutableStateFlow(UserUiState())
    val uiState: StateFlow<UserUiState>
        get() = _uiState

    fun loadUser() = viewModelScope.launch {
        FirebaseAuth.getInstance().currentUser?.uid?.let { currentUserId ->
            fireRepository.fetchUser(currentUserId)
        }
        fireRepository.user.collect { user ->
            _uiState.update { it.copy(user = user) }
        }
    }

    fun onLogoutClick() {
        showDialog(
            DialogState(
                title = getString(R.string.logout_title),
                message = getString(R.string.logout_message),
                primaryButtonText = getString(R.string.logout_yes),
                onPrimaryClick = {
                    logout()
                    dismissDialog()
                },
                secondaryButtonText = getString(R.string.logout_no),
                onSecondaryClick = { dismissDialog() }
            )
        )
    }

    fun navigateToUpdateUser() {
        navigate(NavigateTo(UpdateUserScreenDestination))
    }

    private fun logout() {
        FirebaseAuth.getInstance().signOut().run {
            navigate(NavigateTo(LoginScreenDestination))
        }
    }
}