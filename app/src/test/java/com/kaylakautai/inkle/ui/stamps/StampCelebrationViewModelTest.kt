package com.kaylakautai.inkle.ui.stamps

import com.kaylakautai.inkle.model.DEFAULT_READING_GOAL
import com.kaylakautai.inkle.model.MBook
import com.kaylakautai.inkle.model.MUser
import com.kaylakautai.inkle.repo.FireRepository
import com.kaylakautai.inkle.testing.MainDispatcherRule
import com.kaylakautai.inkle.testing.TestData
import com.kaylakautai.inkle.ui.stamps.StampCelebrationsTest.Companion.lastYear
import com.kaylakautai.inkle.ui.stamps.StampCelebrationsTest.Companion.now
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class StampCelebrationViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val shelf = MutableStateFlow<List<MBook>>(emptyList())
    private val user = MutableStateFlow<MUser?>(TestData.profile)
    private val fireRepository = mockk<FireRepository>().also { repo ->
        every { repo.savedBooks } returns shelf
        every { repo.user } returns user
    }
    private val celebrations = StampCelebrations()
    private lateinit var viewModel: StampCelebrationViewModel

    @Before
    fun setUp() {
        // After the rule has swapped in the test Main dispatcher.
        viewModel = StampCelebrationViewModel(celebrations, fireRepository)
    }

    private val reading = TestData.readingBook.copy(startedReading = now())
    private fun MBook.finished(at: com.google.firebase.Timestamp = now()) = copy(finishedReading = at)

    @Test
    fun bookFinishedAndOnTheShelf_cardShowsItLanding() = runTest(mainDispatcherRule.testDispatcher) {
        shelf.value = listOf(reading.finished())

        celebrations.bookFinished(reading.googleBookId, now())

        assertEquals(reading.id, viewModel.landingBookId.value)
    }

    @Test
    fun waitsForTheShelfToShowTheBookFinished() = runTest(mainDispatcherRule.testDispatcher) {
        shelf.value = listOf(reading)
        celebrations.bookFinished(reading.googleBookId, now())
        assertNull(viewModel.landingBookId.value)

        // The save reaches the shelf a moment later.
        shelf.value = listOf(reading.finished())

        assertEquals(reading.id, viewModel.landingBookId.value)
    }

    @Test
    fun shelfNeverShowsIt_givesUpQuietly() = runTest(mainDispatcherRule.testDispatcher) {
        shelf.value = listOf(reading)
        celebrations.bookFinished(reading.googleBookId, now())

        advanceTimeBy(6.seconds)

        assertNull(viewModel.landingBookId.value)
        assertNull(celebrations.pending.value)
    }

    @Test
    fun shelfShowsItFinishedInAnotherYear_doesntShow() = runTest(mainDispatcherRule.testDispatcher) {
        shelf.value = listOf(reading.finished(at = lastYear()))
        celebrations.bookFinished(reading.googleBookId, now())

        advanceTimeBy(6.seconds)

        assertNull(viewModel.landingBookId.value)
    }

    @Test
    fun dismiss_closesTheCard() = runTest(mainDispatcherRule.testDispatcher) {
        shelf.value = listOf(reading.finished())
        celebrations.bookFinished(reading.googleBookId, now())

        viewModel.onDismiss()

        assertNull(viewModel.landingBookId.value)
        assertNull(celebrations.pending.value)
    }

    @Test
    fun reward_isThisYearsOnly() = runTest(mainDispatcherRule.testDispatcher) {
        val year = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        user.value = TestData.profile.copy(reward = "A spa day", rewardYear = year)
        assertEquals("A spa day", viewModel.reward.value)

        user.value = TestData.profile.copy(reward = "A spa day", rewardYear = year - 1)
        assertNull(viewModel.reward.value)
    }

    @Test
    fun readingGoal_followsTheProfile() = runTest(mainDispatcherRule.testDispatcher) {
        user.value = TestData.profile.copy(readingGoal = 20)
        assertEquals(20, viewModel.readingGoal.value)

        user.value = null
        assertEquals(DEFAULT_READING_GOAL, viewModel.readingGoal.value)
    }
}
