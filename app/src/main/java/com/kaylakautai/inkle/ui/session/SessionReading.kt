package com.kaylakautai.inkle.ui.session

import com.kaylakautai.inkle.model.MBook
import com.kaylakautai.inkle.model.MSession

/** What the reader is recording: pages in this sitting, or the page they have reached. */
enum class SessionLogBy { PAGES, REACHED }

/**
 * A log entry turned into a sitting and a new place in the book.
 * [sessionPages] is null when this only records where they already are.
 */
internal data class LoggedReading(
    val sessionPages: Int?,
    val currentPage: Int
)

/** Where the reader is: the saved page, or the pages logged so far when none is saved. */
internal fun MBook.readingPlace(sessions: List<MSession>): Int {
    currentPage?.takeIf { it > 0 }?.let { return it }
    val id = shelfId()
    return sessions.filter { it.googleBookId == id }.sumOf { it.pages ?: 0 }
}

/**
 * How [amount] moves the reader on from [place].
 * Null when the number is missing, or a reached page that isn't further on.
 */
internal fun loggedReading(by: SessionLogBy, amount: Int?, place: Int): LoggedReading? {
    val value = amount?.takeIf { it > 0 } ?: return null
    return when (by) {
        SessionLogBy.PAGES -> value.takeIf { it <= MAX_SESSION_PAGES }?.let {
            LoggedReading(sessionPages = it, currentPage = place + it)
        }
        SessionLogBy.REACHED -> when {
            value > MAX_PAGE -> null
            place <= 0 -> LoggedReading(sessionPages = null, currentPage = value)
            value <= place -> null
            value - place > MAX_SESSION_PAGES -> null
            else -> LoggedReading(sessionPages = value - place, currentPage = value)
        }
    }
}

/** Matches the session page limit in firestore.rules. */
private const val MAX_SESSION_PAGES = 99_999

/** Matches the current_page limit in firestore.rules. */
private const val MAX_PAGE = 100_000
