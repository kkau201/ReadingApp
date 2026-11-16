package com.example.readingapp.utils

/**
 * Upgrades an `http://` URL to `https://`. Google Books returns cover links over plain HTTP,
 * which Android blocks because the app disallows cleartext traffic.
 */
fun String.toHttps(): String =
    if (startsWith("http://", ignoreCase = true)) "https://" + substring("http://".length) else this
