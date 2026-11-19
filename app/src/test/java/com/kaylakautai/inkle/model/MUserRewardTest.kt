package com.kaylakautai.inkle.model

import com.kaylakautai.inkle.ui.user.update.rewardUpdates
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MUserRewardTest {

    private val user = MUser(userId = "uid", displayName = "Kay")

    @Test
    fun rewardFor_theYearItWasSetFor() {
        assertEquals("A spa day", user.copy(reward = "A spa day", rewardYear = 2026).rewardFor(2026))
    }

    @Test
    fun rewardFor_doesntCarryIntoAnotherYear() {
        assertNull(user.copy(reward = "A spa day", rewardYear = 2025).rewardFor(2026))
    }

    @Test
    fun rewardFor_noneSet() {
        assertNull(user.rewardFor(2026))
        assertNull(user.copy(reward = "  ", rewardYear = 2026).rewardFor(2026))
        assertNull(user.copy(reward = "A spa day", rewardYear = null).rewardFor(2026))
    }

    @Test
    fun rewardUpdates_savesTheTrimmedRewardWithItsYear() {
        assertEquals(
            mapOf(REWARD to "A spa day", REWARD_YEAR to 2026),
            rewardUpdates("  A spa day ", year = 2026)
        )
    }

    @Test
    fun rewardUpdates_blank_removesBoth() {
        assertEquals(mapOf(REWARD to null, REWARD_YEAR to null), rewardUpdates("   ", year = 2026))
    }
}
