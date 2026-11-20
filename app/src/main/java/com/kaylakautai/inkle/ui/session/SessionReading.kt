package com.kaylakautai.inkle.ui.session

import com.kaylakautai.inkle.model.MBook
import com.kaylakautai.inkle.model.MSession
import com.kaylakautai.inkle.model.StoredBook
import com.kaylakautai.inkle.model.StoredSession
import com.kaylakautai.inkle.repo.PageUpdate
import com.kaylakautai.inkle.ui.model.BookStatus
import kotlin.math.roundToInt

/**
 * How the reader records progress, as in most reading apps: pages read this sitting, the page
 * they're on now, or how far through they are (for books with a known length).
 */
enum class SessionLogBy { PAGES, REACHED, PERCENT }

/** Why a progress entry can't be saved. */
enum class PagesError {
    /** Nothing (or 0) entered. */
    MISSING,

    /** A page or percentage that isn't past where the reader already is. */
    NOT_AFTER,

    /** More than can be stored, or more than 100%. */
    TOO_MANY
}

/** A progress entry, worked out against where the reader was. */
internal sealed interface ReadingEntry {
    /** [pages] read this sitting, taking the reader to [pageReached]. */
    data class Valid(val pages: Int, val pageReached: Int) : ReadingEntry

    data class Invalid(val error: PagesError) : ReadingEntry
}

/** The most digits each way of recording can need. */
internal fun SessionLogBy.maxDigits(): Int = when (this) {
    SessionLogBy.PAGES -> StoredSession.MAX_PAGES.toString().length
    SessionLogBy.REACHED -> StoredBook.MAX_PAGE.toString().length
    SessionLogBy.PERCENT -> 3
}

/**
 * Turns what was typed into a sitting. [place] is the page the reader was on; [pageCount] is
 * needed for [SessionLogBy.PERCENT]. A book started partway through has its starting page
 * saved on the book (see [readingPlace]), so every entry here is reading done.
 */
internal fun readingEntry(
    by: SessionLogBy,
    input: String,
    place: Int,
    pageCount: Int?
): ReadingEntry {
    val amount = input.toIntOrNull()?.takeIf { it > 0 } ?: return ReadingEntry.Invalid(PagesError.MISSING)
    val reached = when (by) {
        SessionLogBy.PAGES -> {
            if (amount > StoredSession.MAX_PAGES) return ReadingEntry.Invalid(PagesError.TOO_MANY)
            place + amount
        }
        SessionLogBy.REACHED -> amount
        SessionLogBy.PERCENT -> {
            val total = pageCount?.takeIf { it > 0 } ?: return ReadingEntry.Invalid(PagesError.MISSING)
            if (amount > 100) return ReadingEntry.Invalid(PagesError.TOO_MANY)
            (amount * total / 100.0).roundToInt().coerceIn(1, total)
        }
    }
    if (reached > StoredBook.MAX_PAGE) return ReadingEntry.Invalid(PagesError.TOO_MANY)
    val pages = reached - place
    return when {
        pages <= 0 -> ReadingEntry.Invalid(PagesError.NOT_AFTER)
        pages > StoredSession.MAX_PAGES -> ReadingEntry.Invalid(PagesError.TOO_MANY)
        else -> ReadingEntry.Valid(pages = pages, pageReached = reached)
    }
}

/**
 * Where the reader is in the book: the saved page, or for books tracked before pages were
 * saved, the pages logged so far. Nothing for a book they haven't started.
 */
internal fun MBook.readingPlace(sessions: List<MSession>): Int {
    if (bookStatus == BookStatus.LIBRARY) return 0
    currentPage?.takeIf { it > 0 }?.let { return it }
    val id = shelfId() ?: return 0
    return sessions.filter { it.googleBookId == id }.sumOf { it.pages ?: 0 }
}

/**
 * How removing [session] moves its book back. Only the session that brought the reader to
 * their current page undoes it (back to where that session began); removing an older one
 * leaves them where they are, as that page is still true. Null when nothing changes.
 */
internal fun MBook.pageAfterRemoving(session: MSession): PageUpdate? {
    val bookId = id ?: return null
    val end = session.endPage ?: return null
    if (end != currentPage) return null
    val start = end - (session.pages ?: 0)
    return PageUpdate(bookId = bookId, page = start.takeIf { it > 0 })
}

/** A percentage through the book for [page], if its length is known. */
internal fun MBook.percentAt(page: Int): Int? {
    val total = pageCount?.takeIf { it > 0 } ?: return null
    return (page * 100 / total).coerceIn(0, 100)
}
