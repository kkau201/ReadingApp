package com.example.readingapp.ui.details

import com.example.readingapp.model.MBook

data class DetailsUiState(
    val bookId: String? = null,
    val book: MBook? = null,
    val savedBook: MBook? = null,
    val isSaved: Boolean = false,
    /**
     * True when Google Books couldn't be reached (usually offline) and [book] is the copy saved
     * on the user's shelf, which has no description, tags or cover.
     */
    val showingShelfCopy: Boolean = false
)