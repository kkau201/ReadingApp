package com.kaylakautai.inkle.model

import com.kaylakautai.inkle.ui.model.BookStatus
import com.kaylakautai.inkle.utils.toHttps
import com.google.firebase.Timestamp

/**
 * A book in the app: a Google Books volume ([Item.toModel]), and/or a copy saved on the user's
 * shelf in Firestore (mapped by hand in FireRepository; written via [toStoredDocument]).
 */
data class MBook(
    /** Firestore document id; null for a book that isn't saved. */
    val id: String? = null,
    val googleBookId: String? = null,
    val userId: String? = null,

    // From Google Books
    val title: String? = null,
    val authors: List<String>? = null,
    val description: String? = null,
    val genres: List<String>? = null,
    val imgUrl: String? = null,
    val pubDate: String? = null,
    val pageCount: Int? = null,

    // The user's own shelf data
    val notes: String? = null,
    /** The user's rating, 1-5 (finished books only). */
    val rating: Double? = null,
    val startedReading: Timestamp? = null,
    /** The page the reader has reached. Ahead of logged sessions when a book was already underway. */
    val currentPage: Int? = null,
    val finishedReading: Timestamp? = null,
    /** The finished date as picked: "yyyy", "MM/yyyy" or "dd/MM/yyyy". */
    val finishedOn: String? = null
) {
    val bookStatus: BookStatus
        get() = if (startedReading == null) BookStatus.LIBRARY
        else if (finishedReading == null) BookStatus.READING
        else BookStatus.FINISHED
}

fun List<Item>.toModels(): List<MBook> {
    return this.map { it.toModel() }
}

fun Item.toModel(): MBook {
    return MBook(
        googleBookId = id,
        title = volumeInfo?.title,
        authors = volumeInfo?.authors,
        description = volumeInfo?.description,
        imgUrl = volumeInfo?.imageLinks?.thumbnail?.toHttps(),
        pubDate = volumeInfo?.publishedDate,
        genres = volumeInfo?.categories,
        // Google sends 0 for some books with an unknown length.
        pageCount = volumeInfo?.pageCount?.takeIf { it > 0 }
    )
}
