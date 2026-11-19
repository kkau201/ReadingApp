package com.kaylakautai.inkle.ui.user.update

import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.viewModelScope
import com.kaylakautai.inkle.R
import com.kaylakautai.inkle.auth.AuthRepository
import com.kaylakautai.inkle.common.BaseViewModel
import com.kaylakautai.inkle.common.DependencyContextWrapper
import com.kaylakautai.inkle.common.ErrorType
import com.kaylakautai.inkle.common.LoadingState
import com.kaylakautai.inkle.model.AVATAR_URL
import com.kaylakautai.inkle.model.DISPLAY_NAME
import com.kaylakautai.inkle.model.MUser
import com.kaylakautai.inkle.model.QUOTE
import com.kaylakautai.inkle.model.TextLimits
import com.kaylakautai.inkle.model.limitTo
import com.kaylakautai.inkle.repo.FireRepository
import com.kaylakautai.inkle.ui.model.Avatar
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class UpdateUserViewModel @Inject constructor(
    private val fireRepository: FireRepository,
    private val authRepository: AuthRepository,
    dependencyContextWrapper: DependencyContextWrapper
) : BaseViewModel(dependencyContextWrapper), DefaultLifecycleObserver {
    override val isNavigationDestination: Boolean = true

    val uiState: StateFlow<UpdateUserUiState>
        field: MutableStateFlow<UpdateUserUiState> = MutableStateFlow(UpdateUserUiState())

    /** True once the form holds the saved profile; from then on it holds the user's edits. */
    private var formLoaded = false
    private var loadJob: Job? = null
    private var saveJob: Job? = null

    /**
     * Fills the form from the saved profile. Called on every start, but only loads once, so
     * leaving the app mid-edit and coming back keeps what was typed.
     */
    fun onLoad() {
        if (formLoaded || loadJob?.isActive == true) return
        loadJob = viewModelScope.launch {
            updateLoadingState(LoadingState.Loading())
            try {
                val userId = authRepository.currentUserId ?: throw ErrorType.UnknownUserException
                val user = fireRepository.fetchUser(userId).getOrThrow()
                    ?: throw ErrorType.UnknownUserException
                fillForm(userId, user)
                formLoaded = true
                updateLoadingState(LoadingState.Success)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                updateLoadingState(LoadingState.Failed(e.message))
                showErrorDialog(e)
            }
        }
    }

    private fun fillForm(userId: String, user: MUser) {
        uiState.update { state ->
            state.copy(
                userId = user.id ?: userId,
                // Name and bio are always saved together, so trim values stored under
                // older, longer limits or the save would be rejected by the rules.
                displayNameInput = user.displayName.limitTo(TextLimits.DISPLAY_NAME),
                bioInput = user.quote.limitTo(TextLimits.QUOTE),
                selectedAvatar = Avatar.entries.find { it.id == user.avatarUrl } ?: Avatar.AVATAR_1
            )
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

    fun onAvatarChange(avatar: Avatar) {
        if (uiState.value.selectedAvatar != avatar) {
            uiState.update { it.copy(selectedAvatar = avatar) }
        }
    }

    fun onUpdateClick() {
        // A second tap while saving would send the same write twice.
        if (saveJob?.isActive == true) return
        val state = uiState.value
        val displayName = displayNameForSave(state.displayNameInput)
        if (displayName == null) {
            uiState.update { it.copy(displayNameError = true) }
            return
        }
        val userId = state.userId ?: return
        saveJob = viewModelScope.launch {
            updateLoadingState(LoadingState.Loading(LoadingState.LoadingType.FULL_SCREEN))
            val result = try {
                fireRepository.updateUser(
                    userId = userId,
                    userUpdates = mapOf(
                        DISPLAY_NAME to displayName,
                        QUOTE to state.bioInput,
                        // Required, so never null (null removes a field).
                        AVATAR_URL to state.selectedAvatar?.id.orEmpty(),
                    )
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Stay on the screen with the edits intact so the user can try again.
                Log.d(TAG, "Saving profile failed: $e")
                updateLoadingState(LoadingState.Idle)
                showToast(getString(R.string.update_user_error))
                return@launch
            }
            // The save is done. Refresh the shared profile so the Profile screen shows the
            // new details; if that read fails, it reloads them itself.
            try {
                fireRepository.fetchUser(userId)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.d(TAG, "Refreshing profile after save failed: $e")
            }
            updateLoadingState(LoadingState.Idle)
            showWriteToast(result, savedRes = R.string.update_user_saved)
            navigateBack()
        }
    }

    private companion object {
        const val TAG = "UpdateUserViewModel"
    }
}

/**
 * The display name to save: the input with surrounding spaces removed, or null if nothing is
 * left (a name is required, and firestore.rules rejects an empty one).
 */
internal fun displayNameForSave(input: String): String? = input.trim().takeIf { it.isNotEmpty() }
