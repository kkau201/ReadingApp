package com.example.readingapp.ui.details.update

import com.example.readingapp.common.LoadingState
import com.example.readingapp.model.MBook
import com.example.readingapp.ui.components.BookStatus

data class UpdateUiState(
    val loadingState: LoadingState = LoadingState.IDLE,
    val bookId: String? = null,
    val book: MBook? = null,
    val isSaved: Boolean = false,
    val noteInput: String = "",
    val selectedStatus: BookStatus = BookStatus.LIBRARY
)