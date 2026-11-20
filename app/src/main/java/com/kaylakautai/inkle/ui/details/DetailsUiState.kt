package com.kaylakautai.inkle.ui.details

import com.kaylakautai.inkle.model.MBook

data class DetailsUiState(
    val bookId: String? = null,
    val book: MBook? = null,
    val savedBook: MBook? = null,
    val isSaved: Boolean = false,
    /** Page reached on [savedBook]: the saved page, or logged session pages when none is saved. */
    val readingPlace: Int = 0,
    /**
     * True when Google Books couldn't be reached (usually offline) and [book] is the copy saved
     * on the user's shelf, which has no description, tags or cover.
     */
    val showingShelfCopy: Boolean = false
)