package com.kaylakautai.inkle.ui.dashboard

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.viewModelScope
import com.kaylakautai.inkle.common.BaseViewModel
import com.kaylakautai.inkle.common.DependencyContextWrapper
import com.kaylakautai.inkle.common.ErrorType
import com.kaylakautai.inkle.common.LoadingState
import com.kaylakautai.inkle.auth.AuthRepository
import com.kaylakautai.inkle.repo.FireRepository
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