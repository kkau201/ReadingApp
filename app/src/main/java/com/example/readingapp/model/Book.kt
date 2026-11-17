package com.example.readingapp.model

/**
 * Google Books API response for a search (`volumes?q=`).
 *
 * The API models here declare only the fields the app reads; Gson skips everything else in the
 * JSON. Fields are nullable because the API leaves many of them out for some books, and Gson
 * doesn't enforce Kotlin's non-null types.
 */
data class Book(
    /** Missing when nothing matches. */
    val items: List<Item>?
)