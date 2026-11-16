package com.example.readingapp.ui.home

import com.example.readingapp.model.MBook
import com.example.readingapp.ui.model.BookStatus
import com.example.readingapp.ui.model.Avatar

data class HomeUiState(
    val userAvatar: Avatar? = null,
    val displayName: String? = null,
    val readingList: List<MBook> = emptyList()
) {
    val currentReadingList: List<MBook>
        get() = readingList.filter { it.bookStatus == BookStatus.READING }
}