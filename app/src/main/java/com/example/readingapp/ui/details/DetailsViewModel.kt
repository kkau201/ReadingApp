package com.example.readingapp.ui.details

import com.example.readingapp.common.BaseViewModel
import com.example.readingapp.common.DependencyContextWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class DetailsViewModel @Inject constructor(
    dependencyContextWrapper: DependencyContextWrapper
): BaseViewModel(dependencyContextWrapper) {
    override val isNavigationDestination: Boolean = true

    private val _uiState: MutableStateFlow<DetailsUiState> = MutableStateFlow(DetailsUiState())
    val uiState: StateFlow<DetailsUiState>
        get() = _uiState
}