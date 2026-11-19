package com.kaylakautai.inkle.ui.components

import androidx.compose.runtime.MonotonicFrameClock
import androidx.compose.runtime.saveable.SaverScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FlipCardStateTest {

    /** Frames 16 ms apart, so animations run to the end without real waiting. */
    private class TestFrameClock : MonotonicFrameClock {
        private var nanos = 0L
        override suspend fun <R> withFrameNanos(onFrame: (Long) -> R): R {
            nanos += 16_000_000L
            return onFrame(nanos)
        }
    }

    private fun flipTest(block: suspend FlipCardState.() -> Unit) = runTest(TestFrameClock()) {
        FlipCardState(showingBack = false).block()
    }

    /** Drags by [degrees] in steps, as a finger would, then lets go at [degreesPerSecond]. */
    private suspend fun FlipCardState.swipe(degrees: Float, degreesPerSecond: Float = 0f) {
        startDrag()
        repeat(10) { dragBy(degrees / 10) }
        settle(degreesPerSecond)
    }

    @Test
    fun startsOnTheFront() = flipTest {
        assertFalse(showingBack)
    }

    // Surviving rotation: the side that's up is saved and restored.

    private fun FlipCardState.savedAndRestored(): FlipCardState {
        val saved = with(FlipCardState.Saver) { SaverScope { true }.save(this@savedAndRestored) }
        return FlipCardState.Saver.restore(saved!!)!!
    }

    @Test
    fun turnedOver_staysTurnedOverAfterRestoring() = flipTest {
        flip()

        val restored = savedAndRestored()

        assertTrue(restored.showingBack)
        assertEquals(180f, restored.angle)
    }

    @Test
    fun front_staysOnTheFrontAfterRestoring() = flipTest {
        val restored = savedAndRestored()

        assertFalse(restored.showingBack)
        assertEquals(0f, restored.angle)
    }

    @Test
    fun restoredCard_canStillTurnBack() = flipTest {
        flip()
        val restored = savedAndRestored()

        restored.flip()

        assertFalse(restored.showingBack)
    }

    @Test
    fun flip_turnsOverAndBack() = flipTest {
        flip()
        assertTrue(showingBack)
        assertEquals(180f, angle)

        flip()
        assertFalse(showingBack)
        assertEquals(0f, angle)
    }

    @Test
    fun slowDragPastHalfway_turnsOver() = flipTest {
        swipe(degrees = 120f)

        assertTrue(showingBack)
    }

    @Test
    fun slowDragNotHalfway_fallsBack() = flipTest {
        swipe(degrees = 60f)

        assertFalse(showingBack)
        assertEquals(0f, angle)
    }

    @Test
    fun quickFlick_turnsOverEvenIfShort() = flipTest {
        swipe(degrees = 20f, degreesPerSecond = 900f)

        assertTrue(showingBack)
    }

    @Test
    fun eitherDirectionTurnsOver_andBack() = flipTest {
        swipe(degrees = -120f)
        assertTrue(showingBack)

        swipe(degrees = 120f)
        assertFalse(showingBack)
    }

    @Test
    fun dragging_stopsAtAHalfTurn() = flipTest {
        startDrag()
        dragBy(500f)

        assertEquals(180f, angle)
    }

    @Test
    fun whileHeld_followsTheFinger_andSettlesOnRelease() = flipTest {
        startDrag()
        dragBy(100f)
        assertEquals(100f, angle)
        assertTrue(showingBack)

        settle(degreesPerSecond = 0f)

        assertEquals(180f, angle)
    }

    @Test
    fun showBack_whenAlreadyOnTheBack_staysThere() = flipTest {
        showBack()
        showBack()

        assertTrue(showingBack)
    }
}
