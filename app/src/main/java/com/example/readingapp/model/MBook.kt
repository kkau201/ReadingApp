package com.example.readingapp.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class MBook(
    var id: String? = null,

    var title: String? = null,
    var subtitle: String? = null,
    var authors: List<String>? = null,
    var description: String? = null,
    var notes: String? = null,
    var genres: List<String>? = null,
    var rating: Double? = null,
    var infoLink: String? = null,

    @get: PropertyName("book_photo_url")
    @set: PropertyName("book_photo_url")
    var imgUrl: String? = null,

    @get: PropertyName("published_date")
    @set: PropertyName("published_date")
    var pubDate: String? = null,

    @get: PropertyName("page_count")
    @set: PropertyName("page_count")
    var pageCount: Int? = null,

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
)

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