package com.example.readingapp.ui.search

import androidx.lifecycle.DefaultLifecycleObserver
import com.example.readingapp.common.BaseViewModel
import com.example.readingapp.mock.generateMockData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor() : BaseViewModel(), DefaultLifecycleObserver {
    override val isNavigationDestination = true

    private val _uiState: MutableStateFlow<SearchUiState> = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState>
        get() = _uiState

    fun onLoad() {
        val mockBooks = generateMockData()
        _uiState.update { it.copy(results = mockBooks) }
    }

    fun onInputChange(input: String) {
        _uiState.update {
            it.copy(
                searchInput = input,
                isValidInput = input.trim().isNotEmpty()
            )
        }
    }

    fun onDoneClick() {
        //TODO
    }
}