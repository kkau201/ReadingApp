package com.example.readingapp.ui.user

import org.junit.Assert.assertEquals
import org.junit.Test

class StampLandingOrderTest {

    /** Each slot's place in the landing sequence, null where nothing lands. */
    private fun landing(slots: Int, filled: Int, newCount: Int) =
        (0 until slots).map { stampLandingOrder(it, filled, newCount) }

    @Test
    fun oneNewStamp_landsInTheNextEmptySlot() {
        // Two already on the card, a third just finished.
        assertEquals(listOf(null, null, 0, null), landing(slots = 4, filled = 3, newCount = 1))
    }

    @Test
    fun severalNewStamps_landLeftToRight() {
        assertEquals(listOf(null, 0, 1, 2, null), landing(slots = 5, filled = 4, newCount = 3))
    }

    @Test
    fun firstEverStamps_startFromTheFirstSlot() {
        assertEquals(listOf(0, 1, null), landing(slots = 3, filled = 2, newCount = 2))
    }

    @Test
    fun nothingNew_nothingLands() {
        assertEquals(listOf(null, null, null), landing(slots = 3, filled = 2, newCount = 0))
    }

    @Test
    fun cardFull_theLastNewStampFillsTheLastSlot() {
        assertEquals(listOf(null, null, 0), landing(slots = 3, filled = 3, newCount = 1))
    }
}
