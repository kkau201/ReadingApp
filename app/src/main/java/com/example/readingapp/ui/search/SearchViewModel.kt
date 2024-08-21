package com.example.readingapp.ui.search

import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.viewModelScope
import com.example.readingapp.common.BaseViewModel
import com.example.readingapp.common.DependencyContextWrapper
import com.example.readingapp.common.ErrorType
import com.example.readingapp.data.RemoteResult
import com.example.readingapp.config.Constants.DEFAULT_SEARCH
import com.example.readingapp.nav.NavigateTo
import com.example.readingapp.repo.BookRepository
import com.example.readingapp.ui.components.DialogState
import com.example.readingapp.ui.destinations.DetailsScreenDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    dependencyContextWrapper: DependencyContextWrapper,
    private val bookRepository: BookRepository
) : BaseViewModel(dependencyContextWrapper), DefaultLifecycleObserver {
    override val isNavigationDestination = true

    private val _uiState: MutableStateFlow<SearchUiState> = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState>
        get() = _uiState

    fun onLoad() {
        searchBooks(DEFAULT_SEARCH)
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

            try {
                bookRepository.getBooksByQuery(q).collect { result ->
                    Log.d("ReadingAppTesting", "Result collected from book flow: $result")
                    _uiState.update { state ->
                        state.copy(
                            results = when (result) {
                                is RemoteResult.Success -> SearchResults.Success(result.data)
                                is RemoteResult.Loading -> SearchResults.Loading
                                is RemoteResult.Error -> SearchResults.Error(result.exception)
                            }
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("ReadingAppTesting", "Failed to get books by query: $e")
                _uiState.update { it.copy(results = SearchResults.Error(e)) }
            }
        }
    }

    fun onBookClick(bookId: String) = navigate(NavigateTo(DetailsScreenDestination(bookId)))
}