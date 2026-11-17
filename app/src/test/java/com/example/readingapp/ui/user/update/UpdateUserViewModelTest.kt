package com.example.readingapp.ui.user.update

import com.example.readingapp.MainViewModel
import com.example.readingapp.R
import com.example.readingapp.auth.FakeAuthRepository
import com.example.readingapp.common.DependencyContextWrapper
import com.example.readingapp.common.LoadingState
import com.example.readingapp.model.AVATAR_URL
import com.example.readingapp.model.DISPLAY_NAME
import com.example.readingapp.model.MAX_READING_GOAL
import com.example.readingapp.model.MIN_READING_GOAL
import com.example.readingapp.model.QUOTE
import com.example.readingapp.model.READING_GOAL
import com.example.readingapp.model.TextLimits
import com.example.readingapp.nav.NavigateBack
import com.example.readingapp.nav.NavigationEvent
import com.example.readingapp.testing.FakeFirestore
import com.example.readingapp.testing.MainDispatcherRule
import com.example.readingapp.testing.TestData
import com.example.readingapp.testing.TestData.UID
import com.example.readingapp.testing.collectNavigation
import com.example.readingapp.testing.fakeContext
import com.example.readingapp.testing.str
import com.example.readingapp.ui.model.Avatar
import io.mockk.every
import io.mockk.slot
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class UpdateUserViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authRepository = FakeAuthRepository().apply { current = TestData.authUser }
    private val firestore = FakeFirestore(currentUserId = { authRepository.currentUserId })
    private val fireRepository = spyk(firestore.repository)
    private val mainViewModel = MainViewModel()
    private val navigationEvents = mutableListOf<NavigationEvent?>()
    private lateinit var viewModel: UpdateUserViewModel

    private val state get() = viewModel.uiState.value

    /** What Save sent to Firestore, if anything. */
    private val savedUpdates = slot<Map<String, Any?>>()

    @Before
    fun setUp() {
        // Writing goes through FirebaseFirestore.getInstance(), so stand in for a successful save.
        every { fireRepository.updateUser(UID, capture(savedUpdates), any(), any()) } answers {
            thirdArg<(Boolean) -> Unit>().invoke(true)
        }

        viewModel = UpdateUserViewModel(
            fireRepository,
            authRepository,
            DependencyContextWrapper(fakeContext())
        )
        viewModel.setMainViewModel(mainViewModel)
    }

    private fun assertNothingSaved() =
        verify(exactly = 0) { fireRepository.updateUser(any(), any(), any(), any()) }

    // region Loading

    @Test
    fun onLoad_fillsFormFromProfile() = runTest {
        firestore.profile = TestData.profile.copy(quote = "Know your own happiness.", readingGoal = 12)

        viewModel.onLoad()

        assertEquals(UID, state.userId)
        assertEquals(TestData.DISPLAY_NAME, state.displayNameInput)
        assertEquals("Know your own happiness.", state.bioInput)
        assertEquals(12, state.readingGoal)
        assertEquals(Avatar.AVATAR_3, state.selectedAvatar)
        assertEquals(LoadingState.Success, mainViewModel.loadingFlow.value)
    }

    @Test
    fun onLoad_noAvatarChosenYet_selectsFirstAvatar() = runTest {
        firestore.profile = TestData.profile.copy(avatarUrl = "")

        viewModel.onLoad()

        assertEquals(Avatar.AVATAR_1, state.selectedAvatar)
    }

    @Test
    fun onLoad_valuesSavedUnderOlderLongerLimits_areTrimmedToFit() = runTest {
        firestore.profile = TestData.profile.copy(
            displayName = "n".repeat(TextLimits.DISPLAY_NAME + 4),
            quote = "q".repeat(TextLimits.QUOTE + 10)
        )

        viewModel.onLoad()

        assertEquals(TextLimits.DISPLAY_NAME, state.displayNameInput.length)
        assertEquals(TextLimits.QUOTE, state.bioInput.length)
    }

    @Test
    fun onLoad_signedOut_showsUnknownUserError() = runTest {
        authRepository.current = null

        viewModel.onLoad()

        assertNull(state.userId)
        assertTrue(mainViewModel.loadingFlow.value is LoadingState.Failed)
        assertEquals(str(R.string.error_unknown_user), mainViewModel.dialogFlow.value?.message)
    }

    @Test
    fun onLoad_profileReadFails_showsError() = runTest {
        firestore.userError = FakeFirestore.unavailable()

        viewModel.onLoad()

        assertNull(state.userId)
        assertEquals(LoadingState.Failed("Firestore unavailable"), mainViewModel.loadingFlow.value)
        assertEquals(str(R.string.error_unknown), mainViewModel.dialogFlow.value?.message)
    }

    // endregion

    // region Editing

    @Test
    fun displayName_isLimitedAndClearsError() = runTest {
        viewModel.onLoad()
        viewModel.onDisplayNameInputChange("   ")
        viewModel.onUpdateClick()
        assertTrue(state.displayNameError)

        viewModel.onDisplayNameInputChange("x".repeat(TextLimits.DISPLAY_NAME + 5))

        assertEquals(TextLimits.DISPLAY_NAME, state.displayNameInput.length)
        assertFalse(state.displayNameError)
    }

    @Test
    fun bio_isLimited() {
        viewModel.onBioInputChange("x".repeat(TextLimits.QUOTE + 5))

        assertEquals(TextLimits.QUOTE, state.bioInput.length)
    }

    @Test
    fun readingGoal_isKeptInAllowedRange() {
        viewModel.onReadingGoalChange(0)
        assertEquals(MIN_READING_GOAL, state.readingGoal)

        viewModel.onReadingGoalChange(MAX_READING_GOAL + 10)
        assertEquals(MAX_READING_GOAL, state.readingGoal)

        viewModel.onReadingGoalChange(10)
        assertEquals(10, state.readingGoal)
    }

    @Test
    fun avatar_canBeChanged() {
        viewModel.onAvatarChange(Avatar.AVATAR_6)

        assertEquals(Avatar.AVATAR_6, state.selectedAvatar)
    }

    // endregion

    // region Saving

    @Test
    fun save_sendsTrimmedEditsToThisUsersDocument() = runTest {
        viewModel.onLoad()
        viewModel.onDisplayNameInputChange("  Jane A.  ")
        viewModel.onBioInputChange("Know your own happiness.")
        viewModel.onReadingGoalChange(15)
        viewModel.onAvatarChange(Avatar.AVATAR_5)

        viewModel.onUpdateClick()

        assertEquals(
            mapOf(
                DISPLAY_NAME to "Jane A.",
                QUOTE to "Know your own happiness.",
                AVATAR_URL to Avatar.AVATAR_5.id,
                READING_GOAL to 15
            ),
            savedUpdates.captured
        )
    }

    @Test
    fun save_succeeds_confirmsRefreshesProfileAndGoesBack() = runTest {
        collectNavigation(mainViewModel, into = navigationEvents)
        viewModel.onLoad()
        firestore.profile = TestData.profile.copy(displayName = "Jane A.") // what Firestore now holds
        viewModel.onDisplayNameInputChange("Jane A.")

        viewModel.onUpdateClick()

        // A full-screen loader shows while the save settles.
        assertEquals(
            LoadingState.Loading(LoadingState.LoadingType.FULL_SCREEN),
            mainViewModel.loadingFlow.value
        )
        assertNull(mainViewModel.toastFlow.value)

        advanceTimeBy(3_001.milliseconds)

        assertEquals(str(R.string.update_user_saved), mainViewModel.toastFlow.value)
        assertEquals(listOf<NavigationEvent?>(NavigateBack), navigationEvents)
        // The Profile screen underneath sees the new name straight away.
        assertEquals("Jane A.", fireRepository.user.value?.displayName)
    }

    @Test
    fun save_blankName_flagsErrorAndSavesNothing() = runTest {
        viewModel.onLoad()
        viewModel.onDisplayNameInputChange("   ")

        viewModel.onUpdateClick()

        assertTrue(state.displayNameError)
        assertNothingSaved()
    }

    @Test
    fun save_beforeProfileLoaded_savesNothing() = runTest {
        viewModel.onDisplayNameInputChange("Jane")

        viewModel.onUpdateClick()

        assertNothingSaved()
    }

    // endregion
}