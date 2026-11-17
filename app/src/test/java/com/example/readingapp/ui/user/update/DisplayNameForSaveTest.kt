package com.example.readingapp.ui.user.update

import com.example.readingapp.testing.TestData.DISPLAY_NAME
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
        assertEquals(DISPLAY_NAME, displayNameForSave(DISPLAY_NAME))
        assertEquals(DISPLAY_NAME, displayNameForSave("  $DISPLAY_NAME "))
    }
}
