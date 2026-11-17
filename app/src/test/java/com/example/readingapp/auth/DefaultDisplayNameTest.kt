package com.example.readingapp.auth

import com.example.readingapp.testing.TestData.DISPLAY_NAME
import com.example.readingapp.testing.TestData.EMAIL
import org.junit.Assert.assertEquals
import org.junit.Test

class DefaultDisplayNameTest {

    @Test
    fun usesAccountNameWhenPresent() {
        assertEquals(DISPLAY_NAME, defaultDisplayName(DISPLAY_NAME, EMAIL))
    }

    @Test
    fun fallsBackToEmailPrefixWhenNameMissingOrBlank() {
        // EMAIL is jane@example.com
        assertEquals("jane", defaultDisplayName(null, EMAIL))
        assertEquals("jane", defaultDisplayName("  ", EMAIL))
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
