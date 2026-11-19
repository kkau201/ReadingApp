package com.kaylakautai.inkle.ui.home

import com.kaylakautai.inkle.model.MBook
import com.kaylakautai.inkle.ui.model.BookStatus
import com.kaylakautai.inkle.ui.model.Avatar

data class HomeUiState(
    val userAvatar: Avatar? = null,
    val displayName: String? = null,
    val readingList: List<MBook> = emptyList()
) {
    val currentReadingList: List<MBook>
        get() = readingList.filter { it.bookStatus == BookStatus.READING }
}