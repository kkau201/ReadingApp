package com.example.readingapp.ui.home

import com.example.readingapp.model.MBook
import com.example.readingapp.ui.components.BookStatus
import com.example.readingapp.ui.user.update.Avatar

data class HomeUiState(
    val userAvatar: Avatar? = null,
    val displayName: String? = null,
    val readingList: List<MBook> = emptyList()
) {
    val currentReadingList: List<MBook>
        get() = readingList.filter { it.bookStatus == BookStatus.READING }
}