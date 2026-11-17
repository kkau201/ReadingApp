package com.example.readingapp.ui.user

import com.example.readingapp.MainViewModel
import com.example.readingapp.R
import com.example.readingapp.auth.FakeAuthRepository
import com.example.readingapp.auth.FakeGoogleIdTokenProvider
import com.example.readingapp.common.DependencyContextWrapper
import com.example.readingapp.common.LoadingState
import com.example.readingapp.nav.NavigateTo
import com.example.readingapp.nav.NavigationEvent
import com.example.readingapp.testing.FakeFirestore
import com.example.readingapp.testing.MainDispatcherRule
import com.example.readingapp.testing.TestData
import com.example.readingapp.testing.TestData.UID
import com.example.readingapp.testing.collectNavigation
import com.example.readingapp.testing.fakeContext
import com.example.readingapp.testing.str
import com.example.readingapp.ui.destinations.UpdateUserScreenDestination
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** Loading the profile screen and keeping it in sync with the repository. */
@OptIn(ExperimentalCoroutinesApi::class)
class UserViewModelProfileTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authRepository = FakeAuthRepository().apply { current = TestData.authUser }
    private val firestore = FakeFirestore(currentUserId = { authRepository.currentUserId })
    private val fireRepository = firestore.repository
    private val mainViewModel = MainViewModel()
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

    private fun assertErrorDialog(@Suppress("SameParameterValue") bodyRes: Int) {
        val dialog = mainViewModel.dialogFlow.value ?: error("No dialog shown")
        assertEquals(str(R.string.error_title), dialog.title)
        assertEquals(str(bodyRes), dialog.message)
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
        firestore.books = TestData.books + TestData.readingBook.copy(id = "theirs", userId = "uid-2")

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
        firestore.profile = TestData.profile.copy(displayName = "Jane A.")
        firestore.books = TestData.books + TestData.otherReadingBook
        viewModel.loadUser()

        assertEquals("Jane A.", state.user?.displayName)
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

    @Test
    fun editProfile_navigatesToUpdateUser() = runTest {
        val events = mutableListOf<NavigationEvent?>()
        collectNavigation(mainViewModel, into = events)

        viewModel.navigateToUpdateUser()

        assertEquals(listOf<NavigationEvent?>(NavigateTo(UpdateUserScreenDestination)), events)
    }
}
