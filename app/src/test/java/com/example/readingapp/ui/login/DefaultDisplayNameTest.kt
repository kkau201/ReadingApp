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

    @Test
    fun cutsLongNamesToTheDisplayNameLimit() {
        // 16 chars max; a space left at the cut is dropped.
        assertEquals("Elizabeth Bennet", defaultDisplayName("Elizabeth Bennet-Darcy", null))
        assertEquals("elizabeth.bennet", defaultDisplayName(null, "elizabeth.bennet.darcy@gmail.com"))
        // Cut lands inside a run of spaces: trailing spaces are removed.
        assertEquals("Anne", defaultDisplayName("Anne" + " ".repeat(12) + "Elliot", null))
    }
}
