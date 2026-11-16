package com.example.readingapp.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class UrlExtensionsTest {

    @Test
    fun toHttps_upgradesGoogleBooksCoverLink() {
        assertEquals(
            "https://books.google.com/books/content?id=ex-tDwAAQBAJ&img=1",
            "http://books.google.com/books/content?id=ex-tDwAAQBAJ&img=1".toHttps()
        )
    }

    @Test
    fun toHttps_ignoresSchemeCase() {
        assertEquals("https://books.google.com/x", "HTTP://books.google.com/x".toHttps())
    }

    @Test
    fun toHttps_leavesHttpsAndOtherValuesAlone() {
        assertEquals("https://books.google.com/x", "https://books.google.com/x".toHttps())
        assertEquals("", "".toHttps())
        assertEquals("avatar_1", "avatar_1".toHttps())
    }
}
