package com.example.readingapp.ui.login

import com.example.readingapp.common.BaseViewModel
import com.example.readingapp.nav.NavigateTo
import com.example.readingapp.ui.destinations.SignUpScreenDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor() : BaseViewModel() {
    override val isNavigationDestination: Boolean = true

    private val _uiState: MutableStateFlow<LoginUiState> = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    fun onEmailChange(input: String) {
        val isValid = input.trim().isNotEmpty()
        _uiState.update { it.copy(email = input, isEmailValid = isValid) }
    }

    fun onPasswordChange(input: String) {
        val isValid = input.trim().isNotEmpty()
        _uiState.update { it.copy(password = input, isPasswordValid = isValid) }
    }

    fun updatePasswordVisibility(visible: Boolean) {
        _uiState.update { it.copy(passwordVisibility = visible) }
    }

    fun onLoginClick() {
        /* TODO */
    }

    fun onSignUpClick() {
        navigate(NavigateTo(SignUpScreenDestination))
    }
}