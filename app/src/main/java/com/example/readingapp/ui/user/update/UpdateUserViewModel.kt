package com.example.readingapp.ui.user.update

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.viewModelScope
import com.example.readingapp.common.BaseViewModel
import com.example.readingapp.common.DependencyContextWrapper
import com.example.readingapp.common.LoadingState
import com.example.readingapp.model.AVATAR_URL
import com.example.readingapp.model.DISPLAY_NAME
import com.example.readingapp.model.QUOTE
import com.example.readingapp.repo.FireRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
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

    fun loadUser() = viewModelScope.launch {
        updateLoadingState(LoadingState.LOADING)
        FirebaseAuth.getInstance().currentUser?.uid?.let { currentUserId ->
            val result = fireRepository.fetchUser(currentUserId)
            result.data?.let {
                updateLoadingState(LoadingState.SUCCESS)
            }
            result.e?.let {
                updateLoadingState(LoadingState.FAILED)
            }
        }
        fireRepository.user.collect { user ->
            _uiState.update {
                it.copy(
                    userId = user?.id,
                    displayNameInput = user?.displayName ?: "",
                    bioInput = user?.quote ?: "",
                    selectedAvatarId = user?.avatarUrl ?: Avatar.AVATAR_1.id
                )
            }
        }
    }

    fun onDisplayNameInputChange(input: String) {
        _uiState.update { it.copy(displayNameInput = input) }
    }

    fun onBioInputChange(input: String) {
        _uiState.update { it.copy(bioInput = input) }
    }

    fun onAvatarChange(id: String) {
        if (uiState.value.selectedAvatarId != id) {
            _uiState.update { it.copy(selectedAvatarId = id) }
        }
    }

    fun onUpdateClick() = uiState.value.userId?.let { id ->
        fireRepository.updateUser(
            userId = id,
            userUpdates = mapOf(
                DISPLAY_NAME to uiState.value.displayNameInput,
                QUOTE to uiState.value.bioInput,
                AVATAR_URL to uiState.value.selectedAvatarId
            ),
            onCompleteListener = {
                showToast("Your details have successfully updated")
                loadUser()
                navigateBack()
            },
            onErrorListener = {
                showToast("Error updating your details")
            }
        )
    }
}