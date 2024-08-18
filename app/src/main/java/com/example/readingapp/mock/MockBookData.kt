package com.example.readingapp.mock

import com.example.readingapp.model.MBook

const val numOfBooks = 3
val titles = listOf("Beginning Flutter", "Flutter in Action", "Flutter Recipes")
val authors = listOf("Marco L. Napoli", "Eric Windmill", "Fu Cheng")
val imgUrls = listOf(
    "http://books.google.com/books/content?id=ex-tDwAAQBAJ&printsec=frontcover&img=1&zoom=1&edge=curl&source=gbs_api",
    "http://books.google.com/books/content?id=EzgzEAAAQBAJ&printsec=frontcover&img=1&zoom=1&edge=curl&source=gbs_api",
    "http://books.google.com/books/content?id=Om21DwAAQBAJ&printsec=frontcover&img=1&zoom=1&edge=curl&source=gbs_api"
)

fun generateMockData(): List<MBook> {
    val books: MutableList<MBook> = mutableListOf()

    repeat(numOfBooks) { index ->
        books.add(
            MBook(
                id = index.toString(),
                googleBookId = index.toString(),
                title = titles[index],
                authors = listOf(authors[index]),
                imgUrl = imgUrls[index]
            )
        )
    }

    return books
}