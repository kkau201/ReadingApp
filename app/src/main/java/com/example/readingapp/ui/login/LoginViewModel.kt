package com.example.readingapp.ui.login

import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.viewModelScope
import com.example.readingapp.R
import com.example.readingapp.common.ActivityProvider
import com.example.readingapp.common.BaseViewModel
import com.example.readingapp.common.DependencyContextWrapper
import com.example.readingapp.common.LoadingState
import com.example.readingapp.model.MUser
import com.example.readingapp.model.USER_ID
import com.example.readingapp.nav.NavigateTo
import com.example.readingapp.repo.FireRepository.Companion.USERS_COLLECTION
import com.example.readingapp.ui.destinations.DashboardScreenDestination
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val dependencyContextWrapper: DependencyContextWrapper,
    private val activityProvider: ActivityProvider
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
        val minimum = if (uiState.value.loginScreenState == LoginScreenState.SIGN_UP) 6 else 1
        val isValid = input.trim().length >= minimum
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

        if (getLoadingState().value !is LoadingState.Loading) {
            viewModelScope.launch(Dispatchers.IO) {
                updateLoadingState(LoadingState.Loading())
                try {
                    auth.signInWithEmailAndPassword(uiState.value.email, uiState.value.password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                updateLoadingState(LoadingState.Idle)
                                navigate(
                                    NavigateTo(
                                        to = DashboardScreenDestination,
                                        popCurrent = true
                                    )
                                )
                            } else {
                                updateLoadingState(LoadingState.Failed())
                                Log.d("LoginViewModel", "Failed logging in user: ${task.exception}")
                                _uiState.update { it.copy(errorMessage = task.exception?.message) }
                                updateLoadingState(LoadingState.Idle)
                            }
                        }
                } catch (e: Exception) {
                    Log.d("LoginViewModel", "Failed logging in user: ${e.message}")
                    updateLoadingState(LoadingState.Idle)
                }
            }
        }
    }

    private fun onRegisterClick() {
        Log.d("LoginViewModel", "Register button clicked")
        _uiState.update { it.copy(errorMessage = null) }
        if (getLoadingState().value is LoadingState.Loading) return

        viewModelScope.launch(Dispatchers.IO) {
            updateLoadingState(LoadingState.Loading())
            try {
                val result = auth.createUserWithEmailAndPassword(
                    uiState.value.email,
                    uiState.value.password
                ).await()
                val displayName =
                    result.user?.email?.substringBefore('@').orEmpty().ifBlank { "Reader" }
                createUser(displayName)
                updateLoadingState(LoadingState.Idle)
                navigate(NavigateTo(to = DashboardScreenDestination, popCurrent = true))
            } catch (e: Exception) {
                Log.d("LoginViewModel", "Failed registering user: ${e.message}")
                updateLoadingState(LoadingState.Idle)
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    fun onGoogleSignInClick() {
        Log.d("LoginViewModel", "Google sign-in clicked")
        _uiState.update { it.copy(errorMessage = null) }
        if (getLoadingState().value is LoadingState.Loading) return

        val webClientId = webClientId()
        if (webClientId == null) {
            _uiState.update { it.copy(errorMessage = getString(R.string.google_sign_in_not_configured)) }
            return
        }
        // Credential Manager needs an Activity context to show its bottom sheet.
        val activity = activityProvider.currentActivity()
        if (activity == null) {
            Log.d("LoginViewModel", "Google sign-in aborted: no current Activity")
            _uiState.update { it.copy(errorMessage = getString(R.string.google_sign_in_failed)) }
            return
        }

        viewModelScope.launch {
            updateLoadingState(LoadingState.Loading())
            try {
                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(GetSignInWithGoogleOption.Builder(webClientId).build())
                    .build()
                val credential = CredentialManager.create(activity)
                    .getCredential(activity, request)
                    .credential

                if (credential !is CustomCredential ||
                    credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    throw IllegalStateException("Unexpected credential type: ${credential.type}")
                }
                val idToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val result =
                    auth.signInWithCredential(GoogleAuthProvider.getCredential(idToken, null))
                        .await()

                val user =
                    result.user ?: throw IllegalStateException("No Firebase user after sign-in")
                if (!userDocumentExists(user.uid)) {
                    val displayName = user.displayName?.takeIf { it.isNotBlank() }
                        ?: user.email?.substringBefore('@').orEmpty().ifBlank { "Reader" }
                    createUser(displayName)
                }

                updateLoadingState(LoadingState.Idle)
                navigate(NavigateTo(to = DashboardScreenDestination, popCurrent = true))
            } catch (e: GetCredentialCancellationException) {
                Log.d("LoginViewModel", "Google sign-in cancelled")
                updateLoadingState(LoadingState.Idle)
            } catch (e: GetCredentialException) {
                Log.d("LoginViewModel", "Google credential error: ${e.type} ${e.message}")
                updateLoadingState(LoadingState.Idle)
                _uiState.update { it.copy(errorMessage = getString(R.string.google_sign_in_failed)) }
            } catch (e: Exception) {
                Log.d("LoginViewModel", "Failed Google sign-in: ${e.message}")
                updateLoadingState(LoadingState.Idle)
                _uiState.update {
                    it.copy(
                        errorMessage = e.message ?: getString(R.string.google_sign_in_failed)
                    )
                }
            }
        }
    }

    /**
     * `default_web_client_id` is generated by the google-services plugin only once the Google
     * provider is enabled in Firebase (it fills `oauth_client` in google-services.json).
     * It is looked up by name so the app still builds before that step is done.
     */
    private fun webClientId(): String? {
        val context = dependencyContextWrapper.context
        val resId =
            context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
        return if (resId != 0) context.getString(resId).takeIf { it.isNotBlank() } else null
    }

    private suspend fun userDocumentExists(userId: String): Boolean {
        return !FirebaseFirestore.getInstance().collection(USERS_COLLECTION)
            .whereEqualTo(USER_ID, userId)
            .limit(1)
            .get()
            .await()
            .isEmpty
    }

    private suspend fun createUser(displayName: String) {
        Log.d("LoginViewModel", "Adding user to database with display name: $displayName")
        val userId = auth.currentUser?.uid ?: return
        val user = MUser(
            userId = userId,
            displayName = displayName
        ).toMap()

        FirebaseFirestore.getInstance().collection(USERS_COLLECTION).add(user).await()
    }
}