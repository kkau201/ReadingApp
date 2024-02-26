package com.example.readingapp.model

data class MBook(
    var id: String? = null,
    var title: String? = null,
    var authors: List<String>? = null,
    var notes: String? = null,
    var imgUrl: String? = null
)

fun List<Item>.toModels(): List<MBook> {
    return this.map { it.toModel() }
}

fun Item.toModel(): MBook {
    return MBook(
        id = this.id,
        title = this.volumeInfo.title,
        authors = this.volumeInfo.authors,
        imgUrl = this.volumeInfo.imageLinks.thumbnail
    )
}