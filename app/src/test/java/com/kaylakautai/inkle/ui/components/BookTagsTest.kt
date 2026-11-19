package com.kaylakautai.inkle.ui.components

import com.kaylakautai.inkle.model.MBook
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BookTagsTest {

    // region Which tags a book has

    @Test
    fun combinedCategories_areSplitAndTrimmed() {
        assertEquals(
            listOf("Fiction", "Romance", "Historical"),
            listOf("Fiction / Romance", "Historical").toBookTags()
        )
    }

    @Test
    fun blanksAndRepeats_areDropped_ignoringCase() {
        assertEquals(
            listOf("History", "World"),
            listOf("History", " ", "History / World", "history", "//").toBookTags()
        )
    }

    @Test
    fun bookWithNoGenres_hasNoTags() {
        assertTrue(MBook(title = "Untagged").tags().isEmpty())
        assertTrue(MBook(genres = emptyList()).tags().isEmpty())
    }

    // endregion

    // region Fitting chips on one line

    @Test
    fun chipsThatFit_keepTheirWidths() {
        assertEquals(listOf(60, 80, 40), fitChipWidths(listOf(60, 80, 40), available = 200))
    }

    @Test
    fun oneLongChip_isShortened_shortOnesStayWhole() {
        // 50 + 40 + 300 > 200: the long tag gets what's left (110).
        assertEquals(listOf(50, 40, 110), fitChipWidths(listOf(50, 40, 300), available = 200))
    }

    @Test
    fun severalLongChips_shareTheSpaceEvenly() {
        // 30 is whole; the two long ones split the remaining 170 (85 each).
        assertEquals(listOf(85, 30, 85), fitChipWidths(listOf(200, 30, 250), available = 200))
    }

    @Test
    fun result_neverExceedsAvailableSpace() {
        val widths = listOf(97, 13, 220, 61)
        listOf(0, 1, 50, 150, 333).forEach { available ->
            val fitted = fitChipWidths(widths, available)
            assertTrue("available=$available fitted=$fitted", fitted.sum() <= available)
            fitted.zip(widths).forEach { (fit, natural) -> assertTrue(fit <= natural) }
        }
    }

    // endregion
}