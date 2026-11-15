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
import com.example.readingapp.model.QUOTE
import com.example.readingapp.model.READING_GOAL
import com.example.readingapp.model.TextLimits
import com.example.readingapp.model.asReadingGoal
import com.example.readingapp.model.limitTo
import com.example.readingapp.auth.AuthRepository
import com.example.readingapp.repo.FireRepository
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
    private val authRepository: AuthRepository,
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
                        // Name and bio are always saved together, so trim values stored under
                        // older, longer limits or the save would be rejected by the rules.
                        displayNameInput = (user?.displayName ?: "").limitTo(TextLimits.DISPLAY_NAME),
                        bioInput = (user?.quote ?: "").limitTo(TextLimits.QUOTE),
                        readingGoal = user?.readingGoal.asReadingGoal(),
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
            val currentUserId =
                authRepository.currentUserId ?: throw ErrorType.UnknownUserException
            fireRepository.fetchUser(currentUserId).getOrThrow()
        } catch (e: Exception) {
            updateLoadingState(LoadingState.Failed(e.message))
            showErrorDialog(e)
        }
    }

    fun onDisplayNameInputChange(input: String) {
        uiState.update {
            it.copy(
                displayNameInput = input.limitTo(TextLimits.DISPLAY_NAME),
                displayNameError = false
            )
        }
    }

    fun onBioInputChange(input: String) {
        uiState.update { it.copy(bioInput = input.limitTo(TextLimits.QUOTE)) }
    }

    fun onReadingGoalChange(goal: Int) {
        uiState.update { it.copy(readingGoal = goal.asReadingGoal()) }
    }

    fun onAvatarChange(avatar: Avatar) {
        if (uiState.value.selectedAvatar != avatar) {
            uiState.update { it.copy(selectedAvatar = avatar) }
        }
    }

    fun onUpdateClick() {
        val displayName = displayNameForSave(uiState.value.displayNameInput)
        if (displayName == null) {
            uiState.update { it.copy(displayNameError = true) }
            return
        }
        uiState.value.userId?.let { id ->
            updateLoadingState(LoadingState.Loading(LoadingState.LoadingType.FULL_SCREEN))
            fireRepository.updateUser(
                userId = id,
                userUpdates = mapOf(
                    DISPLAY_NAME to displayName,
                    QUOTE to uiState.value.bioInput,
                    AVATAR_URL to uiState.value.selectedAvatar?.id,
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

/**
 * The display name to save: the input with surrounding spaces removed, or null if nothing is
 * left (a name is required, and firestore.rules rejects an empty one).
 */
internal fun displayNameForSave(input: String): String? = input.trim().takeIf { it.isNotEmpty() }