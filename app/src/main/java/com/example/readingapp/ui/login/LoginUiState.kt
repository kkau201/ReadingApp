package com.example.readingapp.ui.login

data class LoginUiState(
    val email: String = "",
    val isEmailValid: Boolean = false,
    val password: String = "",
    val isPasswordValid: Boolean = false,
    val passwordVisibility: Boolean = false,
    val loginScreenState: LoginScreenState = LoginScreenState.LOGIN,
    val errorMessage: String? = null
)

enum class LoginScreenState {
    LOGIN, SIGN_UP;

    companion object {
        fun switch(state: LoginScreenState): LoginScreenState {
            return when (state) {
                LOGIN -> SIGN_UP
                SIGN_UP -> LOGIN
            }
        }
    }

}