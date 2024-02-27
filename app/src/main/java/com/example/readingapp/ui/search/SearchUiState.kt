package com.example.readingapp.ui.search

import com.example.readingapp.model.MBook
import javax.annotation.concurrent.Immutable

data class SearchUiState(
    val searchInput: String = "",
    val isValidInput: Boolean = false,
    val results: SearchResults = SearchResults.Loading
)

@Immutable
sealed interface SearchResults {
    data class Success(val books: List<MBook>) : SearchResults
    data class Error(val exception: Throwable?) : SearchResults
    data object Loading : SearchResults
}