package com.example.readingapp.ui.details

import com.example.readingapp.model.MBook

data class DetailsUiState(
    val bookId: String? = null,
    val book: MBook? = null,
    val isSaved: Boolean = false
)