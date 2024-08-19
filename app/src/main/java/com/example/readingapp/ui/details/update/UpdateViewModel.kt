package com.example.readingapp.ui.details.update

import com.example.readingapp.common.BaseViewModel
import com.example.readingapp.common.DependencyContextWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class UpdateViewModel @Inject constructor(
    dependencyContextWrapper: DependencyContextWrapper
): BaseViewModel(dependencyContextWrapper) {
    override val isNavigationDestination: Boolean = true

    val _uiState: MutableStateFlow<UpdateUiState> = MutableStateFlow(UpdateUiState())
    val uiState: StateFlow<UpdateUiState>
        get() = _uiState
}