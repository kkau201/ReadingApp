package com.example.readingapp.ui.login

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.readingapp.common.BaseViewModel
import com.example.readingapp.common.DependencyContextWrapper
import com.example.readingapp.common.LoadingState
import com.example.readingapp.model.MUser
import com.example.readingapp.nav.NavigateTo
import com.example.readingapp.repo.FireRepository.Companion.USERS_COLLECTION
import com.example.readingapp.ui.components.DialogState
import com.example.readingapp.ui.destinations.DashboardScreenDestination
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    dependencyContextWrapper: DependencyContextWrapper
) : BaseViewModel(dependencyContextWrapper) {
    override val isNavigationDestination: Boolean = true

    private val _uiState: MutableStateFlow<LoginUiState> = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    private val auth: FirebaseAuth = Firebase.auth

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

    fun onSwitchScreens(state: LoginScreenState) {
        _uiState.update {
            it.copy(
                email = "",
                password = "",
                isEmailValid = false,
                isPasswordValid = false,
                loginScreenState = state,
                errorMessage = null
            )
        }
    }

    fun onSubmitClick() {
        if (uiState.value.loginScreenState == LoginScreenState.LOGIN) onLoginClick() else onRegisterClick()
    }

    private fun onLoginClick() {
        Log.d("LoginViewModel", "Login button clicked")
        _uiState.update { it.copy(errorMessage = null) }

        if (getLoadingState().value != LoadingState.LOADING) {
            viewModelScope.launch(Dispatchers.IO) {
                updateLoadingState(LoadingState.LOADING)
                try {
                    auth.signInWithEmailAndPassword(uiState.value.email, uiState.value.password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                updateLoadingState(LoadingState.IDLE)
                                navigate(NavigateTo(to = DashboardScreenDestination, popCurrent = true))
                            }
                            else {
                                updateLoadingState(LoadingState.FAILED)
                                Log.d("LoginViewModel", "Failed logging in user: ${task.exception}")
                                _uiState.update { it.copy(errorMessage = task.exception?.message) }
                                updateLoadingState(LoadingState.IDLE)
                            }
                        }
                } catch (e: Exception) {
                    Log.d("LoginViewModel", "Failed logging in user: ${e.message}")
                    updateLoadingState(LoadingState.IDLE)
                }
            }
        }
    }

    private fun onRegisterClick() {
        Log.d("LoginViewModel", "Register button clicked")
        _uiState.update { it.copy(errorMessage = null) }

        if (getLoadingState().value != LoadingState.LOADING) {
            viewModelScope.launch(Dispatchers.IO) {
                updateLoadingState(LoadingState.LOADING)
                try {
                    auth.createUserWithEmailAndPassword(uiState.value.email, uiState.value.password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.d("LoginViewModel", "Success registering user: ${task.result.user}")
                                updateLoadingState(LoadingState.SUCCESS)

                                task.result?.user?.email?.split('@')?.get(0)?.let { displayName ->
                                    createUser(displayName)
                                }

                                showDialog(
                                    DialogState(
                                        title = "Register Success",
                                        message = "You have successfully created an account, you can now login",
                                        primaryButtonText = "Login",
                                        onPrimaryClick = {
                                            dismissDialog()
                                            updateLoadingState(LoadingState.IDLE)
                                            onSwitchScreens(LoginScreenState.LOGIN)
                                        }
                                    )
                                )
                            }
                            else {
                                updateLoadingState(LoadingState.FAILED)
                                Log.d("LoginViewModel", "Failed registering user: ${task.exception}")
                                _uiState.update { it.copy(errorMessage = task.exception?.message) }
                                updateLoadingState(LoadingState.IDLE)
                            }
                        }
                } catch (e: Exception) {
                    Log.d("LoginViewModel", "Failed registering user: ${e.message}")
                }
            }
        }
    }

    private fun createUser(displayName: String) {
        Log.d("LoginViewModel", "Adding user to database with display name: $displayName")
        val userId = auth.currentUser?.uid
        val user = MUser(
            userId = userId.toString(),
            displayName = displayName
        ).toMap()

        FirebaseFirestore.getInstance().collection(USERS_COLLECTION).add(user)
    }
}