package com.example.readingapp.model

data class MBookDetails(
    var id: String,
    var title: String? = null,
    var subtitle: String? = null,
    var authors: List<String>? = null,
    var description: String? = null,
    var notes: String? = null,
    var imgUrl: String? = null,
    var rating: Double? = null,
    var pubDate: String? = null,
    var genres: List<String>? = null,
    var infoLink: String? = null,
)

fun Item.toDetailsModel(): MBookDetails {
    return MBookDetails(
        id = this.id,
        title = this.volumeInfo.title,
        subtitle = this.volumeInfo.subtitle,
        authors = this.volumeInfo.authors,
        description = this.volumeInfo.description,
        imgUrl = this.volumeInfo.imageLinks?.thumbnail,
        rating = this.volumeInfo.averageRating,
        pubDate = this.volumeInfo.publishedDate,
        genres = this.volumeInfo.categories,
        infoLink = this.volumeInfo.infoLink
    )
}