package com.example.readingapp.ui.search

import com.example.readingapp.data.DataOrException
import com.example.readingapp.model.MBook

data class SearchUiState(
    val searchInput: String = "",
    val isValidInput: Boolean = false,
    val results: DataOrException<List<MBook>, Boolean, Exception> = DataOrException()
)