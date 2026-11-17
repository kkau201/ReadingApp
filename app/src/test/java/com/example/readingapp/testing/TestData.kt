package com.example.readingapp.testing

import com.example.readingapp.auth.AuthUser
import com.example.readingapp.model.MBook
import com.example.readingapp.model.MSession
import com.example.readingapp.model.MUser
import com.example.readingapp.ui.model.Avatar
import com.google.firebase.Timestamp
import io.mockk.mockk
import java.util.Calendar
import java.util.GregorianCalendar

/** Shared sample data so tests describe the same signed-in reader and library. */
object TestData {
    const val UID = "uid-1"
    const val DISPLAY_NAME = "Jane Austen"
    const val EMAIL = "jane@example.com"

    /** A different account, e.g. to check one user never sees another's data. */
    const val OTHER_UID = "uid-2"

    /** What Jane changes her name to when tests edit the profile. */
    const val EDITED_DISPLAY_NAME = "Jane A."

    /** The signed-in Firebase Auth account. */
    val authUser = AuthUser(uid = UID, displayName = DISPLAY_NAME, email = EMAIL)

    /** Jane's Firestore profile (users/{uid}). */
    val profile = MUser(userId = UID, displayName = DISPLAY_NAME, avatarUrl = Avatar.AVATAR_3.id)

    /** Saved but not started. */
    val libraryBook = MBook(id = "library", googleBookId = "gb-emma", title = "Emma", userId = UID)

    /** Started, not finished. */
    val readingBook = MBook(
        id = "reading",
        googleBookId = "gb-persuasion",
        title = "Persuasion",
        userId = UID,
        startedReading = mockk()
    )

    /** A second book in progress; sorts before [readingBook] by title. */
    val otherReadingBook = MBook(
        id = "reading-2",
        googleBookId = "gb-mansfield",
        title = "Mansfield Park",
        userId = UID,
        startedReading = mockk()
    )

    val finishedBook = MBook(
        id = "finished",
        googleBookId = "gb-sanditon",
        title = "Sanditon",
        userId = UID,
        startedReading = mockk(),
        finishedReading = mockk()
    )

    /** One book in each status. */
    val books = listOf(libraryBook, readingBook, finishedBook)

    /** Noon local time on a date, as the app stores picked dates. */
    private fun localNoon(year: Int, month: Int, day: Int) =
        Timestamp(GregorianCalendar(year, month, day, 12, 0).time)

    /** Finished with a date picked in the app, and real timestamps (for date maths). */
    val datedFinishedBook = MBook(
        id = "finished-dated",
        googleBookId = "gb-lady-susan",
        title = "Lady Susan",
        userId = UID,
        startedReading = localNoon(2025, Calendar.FEBRUARY, 1),
        finishedReading = localNoon(2025, Calendar.MARCH, 5),
        finishedOn = "05/03/2025",
        notes = "Wicked and wonderful.",
        rating = 4.0
    )

    /** A Google Books result that isn't on Jane's shelf (no Firestore id or owner). */
    val unsavedBook = MBook(
        googleBookId = "gb-northanger",
        title = "Northanger Abbey",
        authors = listOf(DISPLAY_NAME),
        pageCount = 251
    )

    /** A logged reading session for [readingBook]. */
    val session = MSession(
        id = "session-1",
        userId = UID,
        googleBookId = readingBook.googleBookId,
        title = readingBook.title,
        pages = 20,
        readOn = "01/11/2026"
    )
}
