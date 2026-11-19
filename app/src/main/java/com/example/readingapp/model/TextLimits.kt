package com.example.readingapp.model

/**
 * Maximum lengths for user-entered text. These must match the limits in firestore.rules,
 * which reject longer values.
 */
object TextLimits {
    const val DISPLAY_NAME = 16
    const val QUOTE = 100
    const val REWARD = 100
    const val BOOK_NOTES = 1000
    const val SESSION_NOTE = 500
}

/**
 * Cuts this string to at most [max] chars. Never ends on half of a surrogate pair (e.g. an
 * emoji), so the result is always valid text.
 */
fun String.limitTo(max: Int): String {
    if (length <= max) return this
    val cut = take(max)
    return if (cut.last().isHighSurrogate()) cut.dropLast(1) else cut
}
