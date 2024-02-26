package com.example.readingapp.ui.search

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.viewModelScope
import com.example.readingapp.common.BaseViewModel
import com.example.readingapp.repo.BookRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val bookRepo: BookRepo
) : BaseViewModel(), DefaultLifecycleObserver {
    override val isNavigationDestination = true

    private val _uiState: MutableStateFlow<SearchUiState> = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState>
        get() = _uiState

    fun onLoad() {
        searchBooks("android")
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
        searchBooks(uiState.value.searchInput)
    }

    private fun searchBooks(q: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (q.isEmpty()) return@launch

            val response = bookRepo.getBooks(q)
            _uiState.update { it.copy(results = response) }
        }
    }
}