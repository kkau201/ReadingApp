package com.kaylakautai.inkle.model

/** One Google Books volume (a search result, or `volumes/{id}`). See [Book]. */
data class Item(
    val id: String?,
    val volumeInfo: VolumeInfo?
)
