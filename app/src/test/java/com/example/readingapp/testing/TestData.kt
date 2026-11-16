package com.example.readingapp.testing

import com.example.readingapp.auth.AuthUser
import com.example.readingapp.model.MBook
import com.example.readingapp.model.MSession
import com.example.readingapp.model.MUser
import com.example.readingapp.ui.model.Avatar
import io.mockk.mockk

/** Shared sample data so tests describe the same signed-in reader and library. */
object TestData {
    const val UID = "uid-1"
    const val DISPLAY_NAME = "Jane Austen"
    const val EMAIL = "jane@example.com"

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
