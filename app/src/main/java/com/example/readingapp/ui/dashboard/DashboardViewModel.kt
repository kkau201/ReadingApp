package com.example.readingapp.ui.dashboard

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.viewModelScope
import com.example.readingapp.common.BaseViewModel
import com.example.readingapp.common.DependencyContextWrapper
import com.example.readingapp.common.ErrorType
import com.example.readingapp.common.LoadingState
import com.example.readingapp.auth.AuthRepository
import com.example.readingapp.repo.FireRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val fireRepository: FireRepository,
    private val authRepository: AuthRepository,
    dependencyContextWrapper: DependencyContextWrapper
) : BaseViewModel(dependencyContextWrapper), DefaultLifecycleObserver {
    override val isNavigationDestination: Boolean = true

    val uiState: StateFlow<DashboardUiState>
        field: MutableStateFlow<DashboardUiState> = MutableStateFlow(DashboardUiState())

    fun onLoad() {
        // Every tab then updates by itself when the user's data changes, even on another device.
        authRepository.currentUserId?.let(fireRepository::startListening)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val currentUserId =
                    authRepository.currentUserId ?: throw ErrorType.UnknownUserException
                fireRepository.fetchUser(currentUserId).getOrThrow()
            } catch (e: Exception) {
                updateLoadingState(LoadingState.Failed(e.message))
                showErrorDialog(e)
            }
        }
    }


    fun onTabClick(tab: DashboardTab) {
        uiState.update { it.copy(currentTab = tab) }
    }
}