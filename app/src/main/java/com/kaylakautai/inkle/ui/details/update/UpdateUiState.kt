package com.kaylakautai.inkle.ui.details.update

import com.kaylakautai.inkle.model.MBook
import com.kaylakautai.inkle.ui.model.BookStatus

data class UpdateUiState(
    val bookId: String? = null,
    val book: MBook? = null,
    val isSaved: Boolean = false,
    val noteInput: String = "",
    val selectedStatus: BookStatus = BookStatus.LIBRARY,
    val selectedRating: Int = 0,
    val finishedYear: Int = 0,
    val finishedMonth: Int? = null,
    val finishedDay: String = "",
    val finishedOnError: Boolean = false,
    /**
     * The page the reader is on, while the book is Reading. Blank means none is saved, so
     * progress comes from the logged sessions.
     */
    val currentPageInput: String = "",
    val currentPageError: Boolean = false
)