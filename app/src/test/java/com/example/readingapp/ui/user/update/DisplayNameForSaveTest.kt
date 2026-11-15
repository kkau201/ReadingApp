package com.example.readingapp.ui.user.update

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DisplayNameForSaveTest {

    @Test
    fun blankNameIsRejected() {
        assertNull(displayNameForSave(""))
        assertNull(displayNameForSave("   "))
        assertNull(displayNameForSave("\n\t"))
    }

    @Test
    fun nameIsSavedTrimmed() {
        assertEquals("Jane", displayNameForSave("Jane"))
        assertEquals("Jane Austen", displayNameForSave("  Jane Austen "))
    }
}
