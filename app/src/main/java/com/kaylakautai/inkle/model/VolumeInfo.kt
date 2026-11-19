package com.kaylakautai.inkle.model

/** A volume's details from Google Books. See [Book]. */
data class VolumeInfo(
    val title: String?,
    val authors: List<String>?,
    val description: String?,
    val imageLinks: ImageLinks?,
    val publishedDate: String?,
    val pageCount: Int?,
    val categories: List<String>?
)
