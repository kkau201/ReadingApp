package com.example.readingapp.ui.search

import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.viewModelScope
import com.example.readingapp.common.BaseViewModel
import com.example.readingapp.common.DependencyContextWrapper
import com.example.readingapp.config.Constants.DEFAULT_SEARCH
import com.example.readingapp.data.RemoteResult
import com.example.readingapp.nav.NavigateTo
import com.example.readingapp.repo.BookRepository
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
    val uiState: StateFlow<SearchUiState>
        field: MutableStateFlow<SearchUiState> = MutableStateFlow(SearchUiState())

    fun onLoad() {
        val state = uiState.value
        if (state.results !is SearchResults.Loading) return
        val query = state.searchInput.trim().ifEmpty { DEFAULT_SEARCH }
        searchBooks(query)
    }

    fun onInputChange(input: String) {
        uiState.update {
            it.copy(
                searchInput = input,
                isValidInput = input.trim().isNotEmpty()
            )
        }
    }

    fun onDoneClick() {
        searchBooks(uiState.value.searchInput)
    }

    private var searchRequest = 0

    private fun searchBooks(q: String) {
        val query = q.trim()
        if (query.isEmpty()) return
        val request = ++searchRequest
        viewModelScope.launch(Dispatchers.IO) {
            try {
                bookRepository.getBooksByQuery(query).collect { result ->
                    if (request != searchRequest) return@collect
                    Log.d("ReadingAppTesting", "Result collected from book flow: $result")
                    uiState.update { state ->
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
                if (request != searchRequest) return@launch
                Log.e("ReadingAppTesting", "Failed to get books by query: $e")
                uiState.update { it.copy(results = SearchResults.Error(e)) }
            }
        }
    }

    fun onBookClick(bookId: String) = navigate(NavigateTo(DetailsScreenDestination(bookId)))
}