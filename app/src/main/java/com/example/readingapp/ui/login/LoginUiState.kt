package com.example.readingapp.ui.login

data class LoginUiState(
    val email: String = "",
    val isEmailValid: Boolean = false,
    val password: String = "",
    val isPasswordValid: Boolean = false,
    val passwordVisibility: Boolean = false,
    val loading: Boolean = false
)