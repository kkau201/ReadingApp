package com.example.readingapp.ui.home

import com.example.readingapp.MainViewModel
import com.example.readingapp.R
import com.example.readingapp.auth.FakeAuthRepository
import com.example.readingapp.common.DependencyContextWrapper
import com.example.readingapp.common.LoadingState
import com.example.readingapp.data.DataOrException
import com.example.readingapp.model.MBook
import com.example.readingapp.model.MUser
import com.example.readingapp.nav.NavigateTo
import com.example.readingapp.nav.NavigationEvent
import com.example.readingapp.repo.FireRepository
import com.example.readingapp.testing.MainDispatcherRule
import com.example.readingapp.testing.TestData
import com.example.readingapp.testing.TestData.UID
import com.example.readingapp.testing.TestDispatcherProvider
import com.example.readingapp.testing.collectNavigation
import com.example.readingapp.testing.fakeContext
import com.example.readingapp.testing.str
import com.example.readingapp.ui.destinations.DetailsScreenDestination
import com.example.readingapp.ui.model.Avatar
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authRepository = FakeAuthRepository().apply { current = TestData.authUser }
    private val cachedUser = MutableStateFlow<MUser?>(null)
    private val fireRepository = mockk<FireRepository>().also { repo ->
        every { repo.user } returns cachedUser
    }
    private val mainViewModel = MainViewModel()
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setUp() {
        viewModel = HomeViewModel(
            fireRepository,
            authRepository,
            TestDispatcherProvider(mainDispatcherRule.testDispatcher),
            DependencyContextWrapper(fakeContext())
        )
        viewModel.setMainViewModel(mainViewModel)
    }

    private fun givenUserFetch(user: MUser?, error: Exception? = null) {
        coEvery { fireRepository.fetchUser(UID) } returns DataOrException(data = user, e = error)
    }

    private fun givenBooksFetch(books: List<MBook>?, error: Exception? = null) {
        coEvery { fireRepository.fetchSavedBooksByUser() } returns DataOrException(data = books, e = error)
    }

    private fun assertErrorDialog(@Suppress("SameParameterValue") bodyRes: Int) {
        val dialog = mainViewModel.dialogFlow.value ?: error("No dialog shown")
        assertEquals(str(R.string.error_title), dialog.title)
        assertEquals(str(bodyRes), dialog.message)
    }

    // region onLoad

    @Test
    fun onLoad_fetchesUserAndBooksIntoUiState() = runTest {
        givenUserFetch(TestData.profile)
        givenBooksFetch(TestData.books)

        viewModel.onLoad()

        assertEquals(
            HomeUiState(
                userAvatar = Avatar.AVATAR_3,
                displayName = TestData.DISPLAY_NAME,
                readingList = TestData.books
            ),
            viewModel.uiState.value
        )
        assertEquals(LoadingState.Success, mainViewModel.loadingFlow.value)
        assertNull(mainViewModel.dialogFlow.value)
    }

    @Test
    fun onLoad_userAlreadyCached_doesNotRefetchUser() = runTest {
        cachedUser.value = TestData.profile
        givenBooksFetch(TestData.books)

        viewModel.onLoad()

        coVerify(exactly = 0) { fireRepository.fetchUser(any()) }
        assertEquals(TestData.DISPLAY_NAME, viewModel.uiState.value?.displayName)
    }

    @Test
    fun onLoad_noBooks_showsEmptyReadingList() = runTest {
        givenUserFetch(TestData.profile)
        givenBooksFetch(emptyList())

        viewModel.onLoad()

        assertTrue(viewModel.uiState.value!!.readingList.isEmpty())
        assertEquals(LoadingState.Success, mainViewModel.loadingFlow.value)
    }

    @Test
    fun onLoad_signedOut_showsUnknownUserErrorAndFetchesNothing() = runTest {
        authRepository.current = null

        viewModel.onLoad()

        coVerify(exactly = 0) { fireRepository.fetchUser(any()) }
        coVerify(exactly = 0) { fireRepository.fetchSavedBooksByUser() }
        assertNull(viewModel.uiState.value)
        assertTrue(mainViewModel.loadingFlow.value is LoadingState.Failed)
        assertErrorDialog(R.string.error_unknown_user)
    }

    @Test
    fun onLoad_userFetchFails_showsErrorAndSkipsBooks() = runTest {
        givenUserFetch(null, error = IllegalStateException("Firestore unavailable"))

        viewModel.onLoad()

        coVerify(exactly = 0) { fireRepository.fetchSavedBooksByUser() }
        assertNull(viewModel.uiState.value)
        assertEquals(LoadingState.Failed("Firestore unavailable"), mainViewModel.loadingFlow.value)
        assertErrorDialog(R.string.error_unknown)
    }

    @Test
    fun onLoad_booksFetchFails_showsErrorAndLeavesStateEmpty() = runTest {
        givenUserFetch(TestData.profile)
        givenBooksFetch(null, error = IllegalStateException("Firestore unavailable"))

        viewModel.onLoad()

        assertNull(viewModel.uiState.value)
        assertEquals(LoadingState.Failed("Firestore unavailable"), mainViewModel.loadingFlow.value)
        assertErrorDialog(R.string.error_unknown)
    }

    @Test
    fun errorDialog_okDismissesIt() = runTest {
        authRepository.current = null
        viewModel.onLoad()

        mainViewModel.dialogFlow.value!!.onPrimaryClick!!()

        assertNull(mainViewModel.dialogFlow.value)
    }

    // endregion

    @Test
    fun onBookClick_navigatesToThatBooksDetails() = runTest {
        val events = mutableListOf<NavigationEvent?>()
        collectNavigation(mainViewModel, into = events)

        viewModel.onBookClick(TestData.readingBook.id!!)

        val event = events.filterIsInstance<NavigateTo>().single()
        assertEquals(DetailsScreenDestination(TestData.readingBook.id!!).route, event.to.route)
    }

    // region HomeUiState

    @Test
    fun currentReadingList_onlyBooksStartedButNotFinished() {
        val state = HomeUiState(readingList = TestData.books)

        assertEquals(listOf(TestData.readingBook), state.currentReadingList)
    }

    // endregion
}
