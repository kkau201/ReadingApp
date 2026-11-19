package com.kaylakautai.inkle.ui.user

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

/** Stamps sit slightly off-true, as if pressed by hand, but never move once they're down. */
class StampWobbleTest {

    private val slots = 0 until 24

    @Test
    fun sameSlot_alwaysSitsTheSameWay() {
        slots.forEach { slot -> assertEquals(stampWobble(slot), stampWobble(slot)) }
    }

    @Test
    fun staysSubtle() {
        slots.map(::stampWobble).forEach { wobble ->
            assertTrue("turned ${wobble.degrees}", abs(wobble.degrees) <= 14f)
            assertTrue("nudged ${wobble.dx}", abs(wobble.dx) <= 0.07f)
            assertTrue("nudged ${wobble.dy}", abs(wobble.dy) <= 0.07f)
        }
    }

    @Test
    fun stampsDontAllSitTheSameWay() {
        val turns = slots.map { stampWobble(it).degrees }
        assertTrue(turns.any { it > 3f })
        assertTrue(turns.any { it < -3f })
    }
}
