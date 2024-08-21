package com.example.readingapp.ui.dashboard

import com.example.readingapp.common.BaseViewModel
import com.example.readingapp.common.DependencyContextWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    dependencyContextWrapper: DependencyContextWrapper
): BaseViewModel(dependencyContextWrapper) {
    override val isNavigationDestination: Boolean = true

    private val _uiState: MutableStateFlow<DashboardUiState> = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState>
        get() = _uiState


    fun onTabClick(tab: DashboardTab) {
        _uiState.update { it.copy(currentTab = tab) }
    }
}