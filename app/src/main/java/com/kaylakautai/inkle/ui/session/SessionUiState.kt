package com.kaylakautai.inkle.ui.session

import com.kaylakautai.inkle.model.MBook
import com.kaylakautai.inkle.model.MSession

data class SessionUiState(
    val books: List<MBook> = emptyList(),
    /**
     * False until the reading books have loaded once, so an empty [books] list while loading
     * isn't mistaken for "no books" (which would flash the empty state).
     */
    val booksLoaded: Boolean = false,
    /** The last load failed, so the screen offers to try again instead of staying blank. */
    val loadFailed: Boolean = false,
    val sessions: List<MSession> = emptyList(),
    val selectedBookId: String? = null,
    val pagesInput: String = "",
    val noteInput: String = "",
    val year: Int = 0,
    val month: Int? = null,
    val day: String = "",
    val bookError: Boolean = false,
    val pagesError: Boolean = false,
    val dateError: Boolean = false,
    val logging: Boolean = false,
    val changingDate: Boolean = false,
    /** The finish form, while it's open. */
    val finishing: FinishForm? = null
)

/** How the Session screen identifies a book: its Google Books id, as sessions store. */
internal fun MBook.shelfId(): String? = googleBookId ?: id

/** Marking a book finished, with an optional rating and note for it. */
data class FinishForm(
    val title: String,
    val message: String,
    val cancelText: String,
    /** 1 to 5 stars, or 0 for none. */
    val rating: Int = 0,
    val noteInput: String = ""
)
