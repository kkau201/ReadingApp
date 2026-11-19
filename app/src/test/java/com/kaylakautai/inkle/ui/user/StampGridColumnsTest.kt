package com.kaylakautai.inkle.ui.user

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Fitting the goal's stars onto a business-card-sized card, as big as they'll go. */
class StampGridColumnsTest {

    // Roughly the stamp area on a phone: a bit wider than tall.
    private val width = 180f
    private val height = 170f

    @Test
    fun oneStar_oneColumn() {
        assertEquals(1, stampGridColumns(1, width, height))
    }

    @Test
    fun sixStars_threeByTwo() {
        assertEquals(3, stampGridColumns(6, width, height))
    }

    @Test
    fun eightStars_threeAcross_biggerThanFourByTwo() {
        // 3 across (3 rows) gives 56-wide cells; 4 across (2 rows) only 45.
        assertEquals(3, stampGridColumns(8, width, height))
    }

    @Test
    fun maxGoal_fiveAcross() {
        assertEquals(5, stampGridColumns(24, width, height))
    }

    @Test
    fun wideShortSpace_oneRow() {
        assertEquals(4, stampGridColumns(4, width = 400f, height = 100f))
    }

    @Test
    fun tallNarrowSpace_oneColumn() {
        assertEquals(1, stampGridColumns(4, width = 50f, height = 400f))
    }

    @Test
    fun sixOnPhone_sideBySide() {
        // Inner size after the card's 16dp padding on a ~380dp-wide phone.
        assertTrue(useSideBySideStampLayout(6, width = 316f, height = 167f))
    }

    @Test
    fun nineOnPhone_sideBySide() {
        assertTrue(useSideBySideStampLayout(9, width = 316f, height = 167f))
    }

    @Test
    fun tenOnPhone_stacks() {
        assertFalse(useSideBySideStampLayout(10, width = 316f, height = 167f))
    }

    @Test
    fun fourteenOnPhone_stacks() {
        assertFalse(useSideBySideStampLayout(14, width = 316f, height = 167f))
    }

    @Test
    fun twentyFourOnPhone_stacks() {
        assertFalse(useSideBySideStampLayout(24, width = 316f, height = 167f))
    }

    @Test
    fun tinyStampArea_stacksEvenForSmallGoal() {
        assertFalse(useSideBySideStampLayout(6, width = 80f, height = 40f))
    }
}
