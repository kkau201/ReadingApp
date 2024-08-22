package com.example.readingapp.ui.details.update

import com.example.readingapp.model.MBook
import com.example.readingapp.ui.components.BookStatus

data class UpdateUiState(
    val bookId: String? = null,
    val book: MBook? = null,
    val isSaved: Boolean = false,
    val noteInput: String = "",
    val selectedStatus: BookStatus = BookStatus.LIBRARY,
    val selectedRating: Int = 0
)