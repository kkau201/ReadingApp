package com.example.readingapp.ui.user.update

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
import com.example.readingapp.testing.BaseViewModelTest
import com.example.readingapp.testing.FakeFirestore
import com.example.readingapp.testing.TestData
import com.example.readingapp.testing.TestData.UID
import com.example.readingapp.testing.fakeContext
import com.example.readingapp.testing.str
import com.example.readingapp.ui.model.Avatar
import com.google.android.gms.tasks.TaskCompletionSource
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UpdateUserViewModelTest : BaseViewModelTest() {

    private val authRepository = FakeAuthRepository().apply { current = TestData.authUser }
    private val firestore = FakeFirestore(currentUserId = { authRepository.currentUserId })
    private val fireRepository = firestore.repository
    private lateinit var viewModel: UpdateUserViewModel

    private val state get() = viewModel.uiState.value

    @Before
    fun setUp() {
        viewModel = UpdateUserViewModel(
            fireRepository,
            authRepository,
            DependencyContextWrapper(fakeContext())
        )
        viewModel.setMainViewModel(mainViewModel)
    }

    private fun assertNothingSaved() = assertTrue(firestore.userUpdates.isEmpty())

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
        assertErrorDialog(R.string.error_unknown_user)
    }

    @Test
    fun onLoad_profileReadFails_showsError() = runTest {
        firestore.userError = FakeFirestore.unavailable()

        viewModel.onLoad()

        assertNull(state.userId)
        assertEquals(LoadingState.Failed("Firestore unavailable"), mainViewModel.loadingFlow.value)
        assertErrorDialog(R.string.error_unknown)
    }

    @Test
    fun onLoad_afterFailedLoad_triesAgain() = runTest {
        firestore.userError = FakeFirestore.unavailable()
        viewModel.onLoad()

        firestore.userError = null
        viewModel.onLoad()

        assertEquals(UID, state.userId)
        assertEquals(TestData.DISPLAY_NAME, state.displayNameInput)
    }

    @Test
    fun leavingAndReturningMidEdit_keepsTheEdits() = runTest {
        viewModel.onLoad()
        viewModel.onDisplayNameInputChange(TestData.EDITED_DISPLAY_NAME)
        viewModel.onBioInputChange("Half-written bio")
        viewModel.onAvatarChange(Avatar.AVATAR_6)

        // Check another app, come back: the screen starts again.
        viewModel.onLoad()

        assertEquals(TestData.EDITED_DISPLAY_NAME, state.displayNameInput)
        assertEquals("Half-written bio", state.bioInput)
        assertEquals(Avatar.AVATAR_6, state.selectedAvatar)
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
        viewModel.onDisplayNameInputChange("  ${TestData.EDITED_DISPLAY_NAME}  ")
        viewModel.onBioInputChange("Know your own happiness.")
        viewModel.onReadingGoalChange(15)
        viewModel.onAvatarChange(Avatar.AVATAR_5)

        viewModel.onUpdateClick()

        assertEquals(
            mapOf(
                DISPLAY_NAME to TestData.EDITED_DISPLAY_NAME,
                QUOTE to "Know your own happiness.",
                AVATAR_URL to Avatar.AVATAR_5.id,
                READING_GOAL to 15
            ),
            firestore.userUpdates.single()
        )
    }

    @Test
    fun save_succeeds_confirmsRefreshesProfileAndGoesBackStraightAway() = runTest {
        collectNavigation()
        viewModel.onLoad()
        viewModel.onDisplayNameInputChange(TestData.EDITED_DISPLAY_NAME)

        viewModel.onUpdateClick()

        // No fixed wait: done as soon as Firestore confirms the write.
        assertEquals(str(R.string.update_user_saved), mainViewModel.toastFlow.value)
        assertEquals(listOf<NavigationEvent?>(NavigateBack), navigationEvents)
        assertEquals(LoadingState.Idle, mainViewModel.loadingFlow.value)
        // The Profile screen underneath sees the new name straight away.
        assertEquals(TestData.EDITED_DISPLAY_NAME, fireRepository.user.value?.displayName)
    }

    @Test
    fun save_inProgress_showsFullScreenLoaderAndIgnoresSecondTap() = runTest {
        collectNavigation()
        val write = TaskCompletionSource<Void>()
        firestore.pendingUserWrite = write
        viewModel.onLoad()

        viewModel.onUpdateClick()
        viewModel.onUpdateClick()

        assertEquals(1, firestore.userUpdates.size)
        assertEquals(
            LoadingState.Loading(LoadingState.LoadingType.FULL_SCREEN),
            mainViewModel.loadingFlow.value
        )
        assertTrue(navigationEvents.isEmpty())

        write.setResult(null)
        runCurrent()

        assertEquals(str(R.string.update_user_saved), mainViewModel.toastFlow.value)
        assertEquals(listOf<NavigationEvent?>(NavigateBack), navigationEvents)
    }

    @Test
    fun save_fails_showsErrorKeepsEditsAndStays() = runTest {
        collectNavigation()
        firestore.userWriteError = FakeFirestore.unavailable()
        viewModel.onLoad()
        viewModel.onDisplayNameInputChange(TestData.EDITED_DISPLAY_NAME)

        viewModel.onUpdateClick()
        advanceUntilIdle()

        // Only the error: never followed by "saved" or leaving the screen.
        assertEquals(str(R.string.update_user_error), mainViewModel.toastFlow.value)
        assertTrue(navigationEvents.isEmpty())
        assertEquals(TestData.EDITED_DISPLAY_NAME, state.displayNameInput)
        assertEquals(LoadingState.Idle, mainViewModel.loadingFlow.value)
        assertEquals(TestData.DISPLAY_NAME, fireRepository.user.value?.displayName)
    }

    @Test
    fun save_failsThenRetried_saves() = runTest {
        collectNavigation()
        firestore.userWriteError = FakeFirestore.unavailable()
        viewModel.onLoad()
        viewModel.onDisplayNameInputChange(TestData.EDITED_DISPLAY_NAME)
        viewModel.onUpdateClick()

        firestore.userWriteError = null
        viewModel.onUpdateClick()

        assertEquals(2, firestore.userUpdates.size)
        assertEquals(str(R.string.update_user_saved), mainViewModel.toastFlow.value)
        assertEquals(listOf<NavigationEvent?>(NavigateBack), navigationEvents)
    }

    @Test
    fun save_offline_savesLocallyGoesBackAndSaysItWillSync() = runTest {
        collectNavigation()
        firestore.online = false
        viewModel.onLoad()
        viewModel.onDisplayNameInputChange(TestData.EDITED_DISPLAY_NAME)

        viewModel.onUpdateClick()

        // No waiting for a server that isn't there.
        assertEquals(str(R.string.saved_offline_toast), mainViewModel.toastFlow.value)
        assertEquals(listOf<NavigationEvent?>(NavigateBack), navigationEvents)
        assertEquals(LoadingState.Idle, mainViewModel.loadingFlow.value)
        // Profile shows the edit straight away, from the device cache.
        assertEquals(TestData.EDITED_DISPLAY_NAME, fireRepository.user.value?.displayName)
    }

    @Test
    fun save_refreshAfterSaveFails_stillConfirmsAndGoesBack() = runTest {
        collectNavigation()
        viewModel.onLoad()
        firestore.userError = FakeFirestore.unavailable() // the read after the write fails

        viewModel.onUpdateClick()

        assertEquals(str(R.string.update_user_saved), mainViewModel.toastFlow.value)
        assertEquals(listOf<NavigationEvent?>(NavigateBack), navigationEvents)
        // Profile keeps showing the last known details (not blank) until it reloads.
        assertEquals(TestData.DISPLAY_NAME, fireRepository.user.value?.displayName)
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
        viewModel.onDisplayNameInputChange(TestData.EDITED_DISPLAY_NAME)

        viewModel.onUpdateClick()

        assertNothingSaved()
    }

    // endregion
}