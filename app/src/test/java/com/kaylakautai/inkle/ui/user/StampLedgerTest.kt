package com.kaylakautai.inkle.ui.user

import com.kaylakautai.inkle.testing.FakeSharedPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StampLedgerTest {

    private val prefs = FakeSharedPreferences()

    /** A fresh ledger over the same storage, as after the app restarts. */
    private fun ledger() = StampLedger(prefs)

    @Test
    fun firstUse_existingStampsDontLand() {
        val ledger = ledger()

        assertTrue(ledger.unseen(listOf("a", "b")).isEmpty())
    }

    @Test
    fun firstUse_withNoStampsYet_theFirstOneStillLands() {
        // A new reader opens Profile before finishing anything this year.
        ledger().markSeen(emptyList())

        assertEquals(listOf("a"), ledger().unseen(listOf("a")))
    }

    @Test
    fun aBookFinishedSinceLastSeen_lands() {
        ledger().markSeen(listOf("a", "b"))

        assertEquals(listOf("c"), ledger().unseen(listOf("a", "b", "c")))
    }

    @Test
    fun severalNewBooks_landInCardOrder() {
        ledger().markSeen(listOf("a"))

        assertEquals(listOf("b", "c"), ledger().unseen(listOf("a", "b", "c")))
    }

    @Test
    fun aStampOnceSeen_doesntLandAgain_evenAfterRestart() {
        val ledger = ledger()
        ledger.markSeen(listOf("a"))
        ledger.markSeen(listOf("a", "b"))

        assertTrue(ledger().unseen(listOf("a", "b")).isEmpty())
    }

    @Test
    fun popUp_beforeProfileWasEverOpened_onlyTheJustFinishedBookLands() {
        // Finished from Session before ever opening Profile: "a" was already on the card.
        assertTrue(ledger().prepareToLand(stamped = listOf("a", "b"), landing = "b"))

        assertEquals(listOf("b"), ledger().unseen(listOf("a", "b")))
    }

    @Test
    fun popUp_aNewStamp_lands() {
        ledger().markSeen(listOf("a"))

        assertTrue(ledger().prepareToLand(stamped = listOf("a", "b"), landing = "b"))
    }

    @Test
    fun popUp_bookFinishedAgain_landsAgain() {
        // "b" landed, was moved back to Reading, then finished again on this device.
        ledger().markSeen(listOf("a", "b"))

        assertTrue(ledger().prepareToLand(stamped = listOf("a", "b"), landing = "b"))
        assertEquals(listOf("b"), ledger().unseen(listOf("a", "b")))
    }

    @Test
    fun popUp_onceLanded_profileDoesntReplayIt() {
        val ledger = ledger()
        ledger.markSeen(listOf("a", "b"))
        ledger.prepareToLand(stamped = listOf("a", "b"), landing = "b")

        // The pop-up's card shows it landing, which marks it seen again.
        ledger().markSeen(listOf("a", "b"))

        assertTrue(ledger().unseen(listOf("a", "b")).isEmpty())
    }

    @Test
    fun popUp_bookPastTheGoal_hasNoSlotToLandIn() {
        ledger().markSeen(listOf("a"))

        assertFalse(ledger().prepareToLand(stamped = listOf("a"), landing = "c"))
    }

    @Test
    fun aBookUnfinishedAndFinishedAgain_doesntLandTwice() {
        ledger().markSeen(listOf("a", "b"))
        // "b" moved back to Reading, then finished again.
        ledger().markSeen(listOf("a"))

        assertTrue(ledger().unseen(listOf("a", "b")).isEmpty())
    }
}
