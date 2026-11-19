package com.kaylakautai.inkle.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot

data class MSession(
    val id: String? = null,
    val userId: String? = null,
    val googleBookId: String? = null,
    val title: String? = null,
    val pages: Int? = null,
    val note: String? = null,
    val readOn: String? = null,
    val readAt: Timestamp? = null
)

internal object StoredSession {
    const val USER_ID = "user_id"
    const val GOOGLE_BOOK_ID = "google_book_id"
    const val TITLE = "title"
    const val PAGES = "pages"
    const val NOTE = "note"
    const val READ_ON = "read_on"
    const val READ_AT = "read_at"

    val fields = listOf(USER_ID, GOOGLE_BOOK_ID, TITLE, PAGES, NOTE, READ_ON, READ_AT)
}

internal fun MSession.toStoredDocument(): Map<String, Any> = buildMap {
    userId?.takeIf { it.isNotBlank() }?.let { put(StoredSession.USER_ID, it) }
    googleBookId?.takeIf { it.isNotBlank() }?.let { put(StoredSession.GOOGLE_BOOK_ID, it) }
    title?.takeIf { it.isNotBlank() }?.let { put(StoredSession.TITLE, it) }
    pages?.takeIf { it > 0 }?.let { put(StoredSession.PAGES, it) }
    note?.takeIf { it.isNotBlank() }?.let { put(StoredSession.NOTE, it) }
    readOn?.takeIf { it.isNotBlank() }?.let { put(StoredSession.READ_ON, it) }
    readAt?.let { put(StoredSession.READ_AT, it) }
}

internal fun DocumentSnapshot.toSession(): MSession {
    return MSession(
        id = id,
        userId = getString(StoredSession.USER_ID),
        googleBookId = getString(StoredSession.GOOGLE_BOOK_ID),
        title = getString(StoredSession.TITLE),
        pages = getLong(StoredSession.PAGES)?.toInt(),
        note = getString(StoredSession.NOTE),
        readOn = getString(StoredSession.READ_ON),
        readAt = getTimestamp(StoredSession.READ_AT)
    )
}
