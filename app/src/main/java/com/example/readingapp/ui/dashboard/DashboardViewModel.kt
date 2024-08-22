package com.example.readingapp.ui.dashboard

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.viewModelScope
import com.example.readingapp.common.BaseViewModel
import com.example.readingapp.common.DependencyContextWrapper
import com.example.readingapp.common.ErrorType
import com.example.readingapp.common.LoadingState
import com.example.readingapp.repo.FireRepository
import com.example.readingapp.ui.theme.Purple
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val fireRepository: FireRepository,
    dependencyContextWrapper: DependencyContextWrapper
) : BaseViewModel(dependencyContextWrapper), DefaultLifecycleObserver {
    override val isNavigationDestination: Boolean = true

    private val _uiState: MutableStateFlow<DashboardUiState> = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState>
        get() = _uiState

    fun onLoad() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                updateLoadingState(LoadingState.LOADING)
                val currentUserId =
                    FirebaseAuth.getInstance().currentUser?.uid ?: throw ErrorType.UnknownUserException
                val user = fireRepository.user.value ?: fireRepository.fetchUser(currentUserId).getOrThrow()

                withContext(Dispatchers.Main) {
                    _uiState.update { it.copy(avatarColor = user?.avatar?.color ?: Purple) }
                    updateLoadingState(LoadingState.SUCCESS)
                }
            } catch (e: Exception) {
                updateLoadingState(LoadingState.FAILED)
                showErrorDialog(e)
            }
        }
    }


    fun onTabClick(tab: DashboardTab) {
        _uiState.update { it.copy(currentTab = tab) }
    }
}