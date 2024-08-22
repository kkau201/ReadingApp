package com.example.readingapp.ui.user.update

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.viewModelScope
import com.example.readingapp.common.BaseViewModel
import com.example.readingapp.common.DependencyContextWrapper
import com.example.readingapp.common.ErrorType
import com.example.readingapp.common.LoadingState
import com.example.readingapp.model.AVATAR_URL
import com.example.readingapp.model.DISPLAY_NAME
import com.example.readingapp.model.QUOTE
import com.example.readingapp.repo.FireRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UpdateUserViewModel @Inject constructor(
    private val fireRepository: FireRepository,
    dependencyContextWrapper: DependencyContextWrapper
) : BaseViewModel(dependencyContextWrapper), DefaultLifecycleObserver {
    override val isNavigationDestination: Boolean = true

    private val _uiState: MutableStateFlow<UpdateUserUiState> = MutableStateFlow(UpdateUserUiState())
    val uiState: StateFlow<UpdateUserUiState>
        get() = _uiState

    private var userJob: Job? = null

    fun onLoad() {
        userJob = viewModelScope.launch {
            updateLoadingState(LoadingState.Loading())
            loadUser().join()
            fireRepository.user.collect { user ->
                _uiState.update { state ->
                    state.copy(
                        userId = user?.id,
                        displayNameInput = user?.displayName ?: "",
                        bioInput = user?.quote ?: "",
                        selectedAvatar = Avatar.entries.find { avatar -> avatar.id == user?.avatarUrl }
                            ?: Avatar.AVATAR_1
                    )
                }
                user?.let {
                    updateLoadingState(LoadingState.Success)
                }
            }
        }
    }

    private fun loadUser() = viewModelScope.launch {
        try {
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: throw ErrorType.UnknownUserException
            fireRepository.fetchUser(currentUserId).getOrThrow()
        } catch (e: Exception) {
            updateLoadingState(LoadingState.Failed(e.message))
            showErrorDialog(e)
        }
    }

    fun onDisplayNameInputChange(input: String) {
        _uiState.update { it.copy(displayNameInput = input) }
    }

    fun onBioInputChange(input: String) {
        _uiState.update { it.copy(bioInput = input) }
    }

    fun onAvatarChange(avatar: Avatar) {
        if (uiState.value.selectedAvatar != avatar) {
            _uiState.update { it.copy(selectedAvatar = avatar) }
        }
    }

    fun onUpdateClick() {
        uiState.value.userId?.let { id ->
            fireRepository.updateUser(
                userId = id,
                userUpdates = mapOf(
                    DISPLAY_NAME to uiState.value.displayNameInput,
                    QUOTE to uiState.value.bioInput,
                    AVATAR_URL to uiState.value.selectedAvatar?.id
                ),
                onCompleteListener = {
                    viewModelScope.launch {
                        userJob?.cancel()
                        userJob = null
                        updateLoadingState(LoadingState.Loading(LoadingState.LoadingType.FULL_SCREEN))
                        delay(3000)
                        loadUser()
                        showToast("Your details have successfully updated")
                        navigateBack()
                    }
                },
                onErrorListener = {
                    showToast("Error updating your details")
                }
            )
        }
    }
}