package com.example.readingapp.model

internal object StoredBook {
    const val TITLE = "title"
    const val AUTHORS = "authors"
    const val PHOTO = "book_photo_url"
    const val PAGE_COUNT = "page_count"
    const val NOTES = "notes"
    const val RATING = "rating"
    const val STARTED_READING = "started_reading"
    const val FINISHED_READING = "finished_reading"
    const val FINISHED_ON = "finished_on"
    const val USER_ID = "user_id"
    const val GOOGLE_BOOK_ID = "google_book_id"

    val fields = listOf(
        TITLE,
        AUTHORS,
        PHOTO,
        PAGE_COUNT,
        NOTES,
        RATING,
        STARTED_READING,
        FINISHED_READING,
        FINISHED_ON,
        USER_ID,
        GOOGLE_BOOK_ID
    )
}

internal fun MBook.toStoredDocument(): Map<String, Any> = buildMap {
        putText(StoredBook.TITLE, title)
        putText(StoredBook.PHOTO, imgUrl)
        authors
        ?.map { it.trim() }
        ?.filter { it.isNotEmpty() }
        ?.takeIf { it.isNotEmpty() }
        ?.let { put(StoredBook.AUTHORS, it) }
    pageCount?.takeIf { it > 0 }?.let { put(StoredBook.PAGE_COUNT, it) }
    putText(StoredBook.GOOGLE_BOOK_ID, googleBookId)
    putText(StoredBook.USER_ID, userId)
    startedReading?.let { put(StoredBook.STARTED_READING, it) }
    finishedReading?.let { put(StoredBook.FINISHED_READING, it) }
    putText(StoredBook.FINISHED_ON, finishedOn)
    putText(StoredBook.NOTES, notes)
    rating?.takeIf { it in 1.0..5.0 }?.let { put(StoredBook.RATING, it) }
}

private fun MutableMap<String, Any>.putText(key: String, value: String?) {
    value?.takeIf { it.isNotBlank() }?.let { put(key, it) }
}
