package com.example.readingapp.model

import com.example.readingapp.ui.components.BookStatus
import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class MBook(
    var id: String? = null,

    val title: String? = null,
    val subtitle: String? = null,
    val authors: List<String>? = null,
    val description: String? = null,
    val notes: String? = null,
    val genres: List<String>? = null,
    val rating: Double? = null,
    val infoLink: String? = null,

    @get: PropertyName("book_photo_url")
    val imgUrl: String? = null,

    @get: PropertyName("published_date")
    val pubDate: String? = null,

    @get: PropertyName("page_count")
    val pageCount: Int? = null,

    @get: PropertyName("started_reading")
    @set: PropertyName("started_reading")
    var startedReading: Timestamp? = null,

    @get: PropertyName("finished_reading")
    @set: PropertyName("finished_reading")
    var finishedReading: Timestamp? = null,

    @get: PropertyName("user_id")
    @set: PropertyName("user_id")
    var userId: String? = null,

    @get: PropertyName("google_book_id")
    @set: PropertyName("google_book_id")
    var googleBookId: String? = null
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
        title = this.volumeInfo.title,
        subtitle = this.volumeInfo.subtitle,
        authors = this.volumeInfo.authors,
        description = this.volumeInfo.description,
        imgUrl = this.volumeInfo.imageLinks?.thumbnail,
        rating = this.volumeInfo.averageRating,
        pubDate = this.volumeInfo.publishedDate,
        genres = this.volumeInfo.categories,
        infoLink = this.volumeInfo.infoLink,
        pageCount = this.volumeInfo.pageCount,
        googleBookId = this.id
    )
}