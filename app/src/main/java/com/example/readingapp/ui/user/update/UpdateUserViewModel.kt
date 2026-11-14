package com.example.readingapp.ui.user.update

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.viewModelScope
import com.example.readingapp.R
import com.example.readingapp.common.BaseViewModel
import com.example.readingapp.common.DependencyContextWrapper
import com.example.readingapp.common.ErrorType
import com.example.readingapp.common.LoadingState
import com.example.readingapp.model.AVATAR_URL
import com.example.readingapp.model.DISPLAY_NAME
import com.example.readingapp.model.PROFILE_COLOR
import com.example.readingapp.model.QUOTE
import com.example.readingapp.model.READING_GOAL
import com.example.readingapp.model.asReadingGoal
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
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class UpdateUserViewModel @Inject constructor(
    private val fireRepository: FireRepository,
    dependencyContextWrapper: DependencyContextWrapper
) : BaseViewModel(dependencyContextWrapper), DefaultLifecycleObserver {
    override val isNavigationDestination: Boolean = true

    val uiState: StateFlow<UpdateUserUiState>
        field: MutableStateFlow<UpdateUserUiState> = MutableStateFlow(UpdateUserUiState())

    private var userJob: Job? = null

    fun onLoad() {
        userJob = viewModelScope.launch {
            updateLoadingState(LoadingState.Loading())
            loadUser().join()
            fireRepository.user.collect { user ->
                uiState.update { state ->
                    state.copy(
                        userId = user?.id,
                        displayNameInput = user?.displayName ?: "",
                        bioInput = user?.quote ?: "",
                        readingGoal = user?.readingGoal.asReadingGoal(),
                        selectedAvatar = Avatar.entries.find { avatar -> avatar.id == user?.avatarUrl }
                            ?: Avatar.AVATAR_1,
                        selectedColor = ProfileColor.fromId(user?.profileColorId)
                            ?: ProfileColor.fromColor(
                                user?.avatar?.color ?: ProfileColor.PURPLE.color
                            )
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
            val currentUserId =
                FirebaseAuth.getInstance().currentUser?.uid ?: throw ErrorType.UnknownUserException
            fireRepository.fetchUser(currentUserId).getOrThrow()
        } catch (e: Exception) {
            updateLoadingState(LoadingState.Failed(e.message))
            showErrorDialog(e)
        }
    }

    fun onDisplayNameInputChange(input: String) {
        uiState.update { it.copy(displayNameInput = input) }
    }

    fun onBioInputChange(input: String) {
        uiState.update { it.copy(bioInput = input) }
    }

    fun onReadingGoalChange(goal: Int) {
        uiState.update { it.copy(readingGoal = goal.asReadingGoal()) }
    }

    fun onAvatarChange(avatar: Avatar) {
        if (uiState.value.selectedAvatar != avatar) {
            uiState.update { it.copy(selectedAvatar = avatar) }
        }
    }

    fun onColorChange(color: ProfileColor) {
        uiState.update { it.copy(selectedColor = color) }
    }

    fun onUpdateClick() {
        uiState.value.userId?.let { id ->
            updateLoadingState(LoadingState.Loading(LoadingState.LoadingType.FULL_SCREEN))
            fireRepository.updateUser(
                userId = id,
                userUpdates = mapOf(
                    DISPLAY_NAME to uiState.value.displayNameInput,
                    QUOTE to uiState.value.bioInput,
                    AVATAR_URL to uiState.value.selectedAvatar?.id,
                    PROFILE_COLOR to uiState.value.selectedColor.id,
                    READING_GOAL to uiState.value.readingGoal.asReadingGoal()
                ),
                onCompleteListener = {
                    viewModelScope.launch {
                        userJob?.cancel()
                        userJob = null
                        delay(3000.milliseconds)
                        loadUser()
                        showToast(getString(R.string.update_user_saved))
                        navigateBack()
                    }
                },
                onErrorListener = {
                    updateLoadingState(LoadingState.Idle)
                    showToast(getString(R.string.update_user_error))
                }
            )
        }
    }
}