package com.example.readingapp.ui.dashboard

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.viewModelScope
import com.example.readingapp.common.BaseViewModel
import com.example.readingapp.common.DependencyContextWrapper
import com.example.readingapp.common.ErrorType
import com.example.readingapp.common.LoadingState
import com.example.readingapp.repo.FireRepository
import com.google.firebase.auth.FirebaseAuth
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
    dependencyContextWrapper: DependencyContextWrapper
) : BaseViewModel(dependencyContextWrapper), DefaultLifecycleObserver {
    override val isNavigationDestination: Boolean = true

    val uiState: StateFlow<DashboardUiState>
        field: MutableStateFlow<DashboardUiState> = MutableStateFlow(DashboardUiState())

    fun onLoad() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val currentUserId =
                    FirebaseAuth.getInstance().currentUser?.uid ?: throw ErrorType.UnknownUserException
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