package com.kaylakautai.inkle.ui.user

import com.kaylakautai.inkle.R
import com.kaylakautai.inkle.auth.FakeAuthRepository
import com.kaylakautai.inkle.auth.FakeGoogleIdTokenProvider
import com.kaylakautai.inkle.common.DependencyContextWrapper
import com.kaylakautai.inkle.common.LoadingState
import com.kaylakautai.inkle.nav.NavigateTo
import com.kaylakautai.inkle.nav.NavigationEvent
import com.kaylakautai.inkle.testing.BaseViewModelTest
import com.kaylakautai.inkle.testing.FakeFirestore
import com.kaylakautai.inkle.testing.TestData
import com.kaylakautai.inkle.testing.TestData.UID
import com.kaylakautai.inkle.testing.collectNavigation
import com.kaylakautai.inkle.testing.fakeContext
import com.kaylakautai.inkle.ui.destinations.UpdateUserScreenDestination
import com.google.firebase.firestore.FieldValue
import com.kaylakautai.inkle.model.MAX_READING_GOAL
import com.kaylakautai.inkle.model.READING_GOAL
import com.kaylakautai.inkle.model.REWARD
import com.kaylakautai.inkle.model.REWARD_YEAR
import com.kaylakautai.inkle.model.TextLimits
import com.kaylakautai.inkle.testing.str
import io.mockk.verify
import java.util.Calendar
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** Loading the profile screen and keeping it in sync with the repository. */
class UserViewModelProfileTest : BaseViewModelTest() {

    private val authRepository = FakeAuthRepository().apply { current = TestData.authUser }
    private val firestore = FakeFirestore(currentUserId = { authRepository.currentUserId })
    private val fireRepository = firestore.repository
    private lateinit var viewModel: UserViewModel

    private val state get() = viewModel.uiState.value

    /** The profile as read back from Firestore (the document id is filled in). */
    private val loadedProfile = TestData.profile.copy(id = UID)

    @Before
    fun setUp() {
        viewModel = UserViewModel(
            fireRepository,
            authRepository,
            FakeGoogleIdTokenProvider(),
            DependencyContextWrapper(fakeContext())
        )
        viewModel.setMainViewModel(mainViewModel)
    }

    // region Loading

    @Test
    fun loadUser_showsProfileAndSavedBooks() = runTest {
        viewModel.loadUser()

        assertEquals(loadedProfile, state.user)
        assertEquals(TestData.books, state.books)
        assertEquals(LoadingState.Success, mainViewModel.loadingFlow.value)
        assertNull(mainViewModel.dialogFlow.value)
    }

    @Test
    fun loadUser_onlyShowsThisUsersBooks() = runTest {
        firestore.books = TestData.books + TestData.readingBook.copy(id = "theirs", userId = TestData.OTHER_UID)

        viewModel.loadUser()

        assertEquals(TestData.books, state.books)
    }

    @Test
    fun loadUser_noBooks_showsEmptyShelf() = runTest {
        firestore.books = emptyList()

        viewModel.loadUser()

        assertTrue(state.books.isEmpty())
        assertEquals(LoadingState.Success, mainViewModel.loadingFlow.value)
    }

    @Test
    fun loadUser_signedOut_showsUnknownUserErrorAndFetchesNothing() = runTest {
        authRepository.current = null

        viewModel.loadUser()

        verify(exactly = 0) { firestore.users.document(any()) }
        assertNull(state.user)
        assertTrue(mainViewModel.loadingFlow.value is LoadingState.Failed)
        assertErrorDialog(R.string.error_unknown_user)
    }

    @Test
    fun loadUser_profileReadFails_showsError() = runTest {
        firestore.userError = FakeFirestore.unavailable()

        viewModel.loadUser()

        assertNull(state.user)
        assertEquals(LoadingState.Failed("Firestore unavailable"), mainViewModel.loadingFlow.value)
        assertErrorDialog(R.string.error_unknown)
    }

    @Test
    fun loadUser_profileDocumentMissing_showsError() = runTest {
        firestore.profile = null

        viewModel.loadUser()

        assertNull(state.user)
        assertTrue(mainViewModel.loadingFlow.value is LoadingState.Failed)
        assertErrorDialog(R.string.error_unknown)
    }

    @Test
    fun loadUser_success_doesNotFlagBooks() = runTest {
        viewModel.loadUser()

        assertFalse(state.booksFailed)
    }

    @Test
    fun booksLoaded_falseUntilTheBooksHaveLoaded() = runTest {
        assertFalse(state.booksLoaded)

        viewModel.loadUser()

        assertTrue(state.booksLoaded)
    }

    @Test
    fun booksLoaded_staysFalseWhenTheBooksFailToLoad() = runTest {
        firestore.booksError = FakeFirestore.unavailable()

        viewModel.loadUser()

        assertFalse(state.booksLoaded)
    }

    @Test
    fun booksLoaded_staysTrueIfALaterRefreshFails() = runTest {
        viewModel.loadUser()
        firestore.booksError = FakeFirestore.unavailable()

        viewModel.onRetryBooksClick()

        assertTrue(state.booksLoaded)
    }

    @Test
    fun loadUser_booksReadFails_showsProfileAndFlagsBooks() = runTest {
        firestore.booksError = FakeFirestore.unavailable()

        viewModel.loadUser()

        assertEquals(loadedProfile, state.user)
        assertTrue(state.booksFailed)
        assertTrue(state.books.isEmpty())
        // The profile itself loaded, so no blocking error dialog.
        assertEquals(LoadingState.Success, mainViewModel.loadingFlow.value)
        assertNull(mainViewModel.dialogFlow.value)
    }

    @Test
    fun retryBooks_succeeds_showsBooksAndClearsFlag() = runTest {
        firestore.booksError = FakeFirestore.unavailable()
        viewModel.loadUser()

        firestore.booksError = null
        viewModel.onRetryBooksClick()

        assertFalse(state.booksFailed)
        assertEquals(TestData.books, state.books)
        assertEquals(LoadingState.Success, mainViewModel.loadingFlow.value)
    }

    @Test
    fun retryBooks_failsAgain_keepsFlag() = runTest {
        firestore.booksError = FakeFirestore.unavailable()
        viewModel.loadUser()

        viewModel.onRetryBooksClick()

        assertTrue(state.booksFailed)
        assertNull(mainViewModel.dialogFlow.value)
    }

    @Test
    fun loadUser_onResumeAfterBooksFailed_clearsFlagWhenTheyLoad() = runTest {
        firestore.booksError = FakeFirestore.unavailable()
        viewModel.loadUser()

        // e.g. connection came back while the app was in the background.
        firestore.booksError = null
        viewModel.loadUser()

        assertFalse(state.booksFailed)
        assertEquals(TestData.books, state.books)
    }

    @Test
    fun loadUser_onResume_picksUpChangesMadeElsewhere() = runTest {
        viewModel.loadUser()

        // e.g. edited on another device, or a book saved from Details.
        firestore.profile = TestData.profile.copy(displayName = TestData.EDITED_DISPLAY_NAME)
        firestore.books = TestData.books + TestData.otherReadingBook
        viewModel.loadUser()

        assertEquals(TestData.EDITED_DISPLAY_NAME, state.user?.displayName)
        assertEquals(TestData.books + TestData.otherReadingBook, state.books)
    }

    // endregion

    // region Staying in sync

    @Test
    fun repositoryUpdates_showWithoutReloading() = runTest {
        viewModel.loadUser()

        // Another screen (e.g. Edit profile) refreshes the shared repository.
        firestore.profile = TestData.profile.copy(quote = "Know your own happiness.")
        fireRepository.fetchUser(UID)

        assertEquals("Know your own happiness.", state.user?.quote)
    }

    @Test
    fun repositoryCleared_clearsTheScreen() = runTest {
        viewModel.loadUser()

        fireRepository.clearUserData()

        assertNull(state.user)
        assertTrue(state.books.isEmpty())
    }

    // endregion

    // region Goal and reward (back of the stamp card)

    private val thisYear = Calendar.getInstance().get(Calendar.YEAR)

    @Test
    fun saveGoalAndReward_savesBothForThisYearAndShowsThem() = runTest {
        viewModel.loadUser()

        viewModel.onSaveGoalAndReward(goal = 6, reward = "  a spa day ")

        assertEquals(
            mapOf(READING_GOAL to 6, REWARD to "a spa day", REWARD_YEAR to thisYear),
            firestore.userUpdates.single()
        )
        assertEquals(6, state.user?.readingGoal)
        assertEquals("a spa day", state.user?.rewardFor(thisYear))
        assertEquals(str(R.string.stamp_card_saved), mainViewModel.toastFlow.value)
    }

    @Test
    fun saveGoalAndReward_blankReward_removesIt() = runTest {
        firestore.profile = TestData.profile.copy(reward = "a spa day", rewardYear = thisYear)
        viewModel.loadUser()

        viewModel.onSaveGoalAndReward(goal = 8, reward = "  ")

        val saved = firestore.userUpdates.single()
        assertTrue(saved[REWARD] is FieldValue)
        assertTrue(saved[REWARD_YEAR] is FieldValue)
        assertNull(state.user?.rewardFor(thisYear))
    }

    @Test
    fun saveGoalAndReward_keepsTheGoalInRangeAndLimitsTheReward() = runTest {
        viewModel.loadUser()

        viewModel.onSaveGoalAndReward(goal = MAX_READING_GOAL + 5, reward = "x".repeat(TextLimits.REWARD + 20))

        val saved = firestore.userUpdates.single()
        assertEquals(MAX_READING_GOAL, saved[READING_GOAL])
        assertEquals(TextLimits.REWARD, (saved[REWARD] as String).length)
    }

    @Test
    fun saveGoalAndReward_writeFails_saysSo() = runTest {
        viewModel.loadUser()
        firestore.userWriteError = FakeFirestore.unavailable()

        viewModel.onSaveGoalAndReward(goal = 6, reward = "a spa day")

        assertEquals(str(R.string.stamp_card_save_error), mainViewModel.toastFlow.value)
        assertEquals(TestData.profile.readingGoal, state.user?.readingGoal)
    }

    @Test
    fun saveGoalAndReward_signedOut_savesNothing() = runTest {
        authRepository.current = null

        viewModel.onSaveGoalAndReward(goal = 6, reward = "a spa day")

        assertTrue(firestore.userUpdates.isEmpty())
    }

    // endregion

    @Test
    fun editProfile_navigatesToUpdateUser() = runTest {
        val events = mutableListOf<NavigationEvent?>()
        collectNavigation(mainViewModel, into = events)

        viewModel.navigateToUpdateUser()

        assertEquals(listOf<NavigationEvent?>(NavigateTo(UpdateUserScreenDestination)), events)
    }
}
