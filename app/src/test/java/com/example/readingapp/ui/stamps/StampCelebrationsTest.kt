package com.example.readingapp.ui.stamps

import com.google.firebase.Timestamp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Calendar

class StampCelebrationsTest {

    private val celebrations = StampCelebrations()

    @Test
    fun finishedThisYear_isCelebrated() {
        celebrations.bookFinished("gb-persuasion", now())

        assertEquals("gb-persuasion", celebrations.pending.value)
    }

    @Test
    fun finishedInAnEarlierYear_isNot() {
        celebrations.bookFinished("gb-persuasion", lastYear())

        assertNull(celebrations.pending.value)
    }

    @Test
    fun noFinishDateOrBook_isNot() {
        celebrations.bookFinished("gb-persuasion", null)
        celebrations.bookFinished(null, now())
        celebrations.bookFinished(" ", now())

        assertNull(celebrations.pending.value)
    }

    @Test
    fun done_clearsIt() {
        celebrations.bookFinished("gb-persuasion", now())

        celebrations.done()

        assertNull(celebrations.pending.value)
    }

    companion object {
        fun now() = Timestamp.now()

        fun lastYear(): Timestamp =
            Timestamp(Calendar.getInstance().apply { add(Calendar.YEAR, -1) }.time)
    }
}
