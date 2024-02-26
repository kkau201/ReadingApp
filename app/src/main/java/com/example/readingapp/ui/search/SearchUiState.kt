package com.example.readingapp.ui.search

import com.example.readingapp.model.MBook

data class SearchUiState(
    val searchInput: String = "",
    val isValidInput: Boolean = false,
    val results: List<MBook> = emptyList()
)