package com.example.readingapp.ui.home

import com.example.readingapp.model.MBook
import com.example.readingapp.ui.components.BookStatus

data class HomeUiState(
    val displayName: String? = null,
    val readingList: List<MBook> = emptyList()
) {
    val currentReadingList: List<MBook>
        get() = readingList.filter { it.bookStatus == BookStatus.READING }
}