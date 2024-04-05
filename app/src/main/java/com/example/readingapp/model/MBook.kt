package com.example.readingapp.model

data class MBook(
    var id: String,
    var title: String? = null,
    var authors: List<String>? = null,
    var notes: String? = null,
    var imgUrl: String? = null,
    var rating: Double? = null,
    var pubDate: String? = null,
    var genres: List<String>? = null,
)

fun List<Item>.toModels(): List<MBook> {
    return this.map { it.toModel() }
}

fun Item.toModel(): MBook {
    return MBook(
        id = this.id,
        title = this.volumeInfo.title,
        authors = this.volumeInfo.authors,
        imgUrl = this.volumeInfo.imageLinks?.thumbnail,
        rating = this.volumeInfo.averageRating,
        pubDate = this.volumeInfo.publishedDate,
        genres = this.volumeInfo.categories
    )
}