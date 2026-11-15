package com.example.readingapp.ui.login

import org.junit.Assert.assertEquals
import org.junit.Test

class DefaultDisplayNameTest {

    @Test
    fun usesAccountNameWhenPresent() {
        assertEquals("Jane Austen", defaultDisplayName("Jane Austen", "jane@example.com"))
    }

    @Test
    fun fallsBackToEmailPrefixWhenNameMissingOrBlank() {
        assertEquals("jane", defaultDisplayName(null, "jane@example.com"))
        assertEquals("jane", defaultDisplayName("  ", "jane@example.com"))
    }

    @Test
    fun fallsBackToReaderWhenNoNameOrEmail() {
        assertEquals("Reader", defaultDisplayName(null, null))
        assertEquals("Reader", defaultDisplayName("", "@example.com"))
    }
}
