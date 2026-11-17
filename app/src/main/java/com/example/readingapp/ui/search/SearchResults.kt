package com.example.readingapp.ui.search

import com.example.readingapp.model.MBook
import javax.annotation.concurrent.Immutable

@Immutable
sealed interface SearchResults {
    /** Nothing to search yet (blank or too-short query). */
    data object Idle : SearchResults
    data object Loading : SearchResults
    data class Success(val books: List<MBook>) : SearchResults
    data class Error(val exception: Throwable?) : SearchResults
}