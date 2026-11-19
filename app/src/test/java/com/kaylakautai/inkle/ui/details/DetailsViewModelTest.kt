package com.kaylakautai.inkle.ui.details

import androidx.lifecycle.SavedStateHandle
import com.kaylakautai.inkle.R
import com.kaylakautai.inkle.auth.FakeAuthRepository
import com.kaylakautai.inkle.common.DependencyContextWrapper
import com.kaylakautai.inkle.common.ErrorType
import com.kaylakautai.inkle.common.LoadingState
import com.kaylakautai.inkle.data.RemoteResult
import com.kaylakautai.inkle.model.StoredBook
import com.kaylakautai.inkle.nav.NavigateTo
import com.kaylakautai.inkle.repo.BookRepository
import com.kaylakautai.inkle.testing.BaseViewModelTest
import com.kaylakautai.inkle.testing.FakeConnectivityObserver
import com.kaylakautai.inkle.testing.FakeFirestore
import com.kaylakautai.inkle.testing.TestData
import com.kaylakautai.inkle.testing.TestData.UID
import com.kaylakautai.inkle.testing.TestData.datedFinishedBook
import com.kaylakautai.inkle.testing.TestData.unsavedBook
import com.kaylakautai.inkle.testing.fakeContext
import com.kaylakautai.inkle.testing.str
import com.kaylakautai.inkle.ui.destinations.UpdateScreenDestination
import com.kaylakautai.inkle.ui.stamps.StampCelebrations
import com.kaylakautai.inkle.ui.model.BookStatus
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Calendar

class DetailsViewModelTest : BaseViewModelTest() {

    private val authRepository = FakeAuthRepository().apply { current = TestData.authUser }
    private val firestore = FakeFirestore(currentUserId = { authRepository.currentUserId })
    private val fireRepository = firestore.repository
    private val bookRepository = mockk<BookRepository>()
    private val connectivity = FakeConnectivityObserver()
    private val celebrations = StampCelebrations()
    private lateinit var viewModel: DetailsViewModel

    private val state get() = viewModel.uiState.value
    private val thisYear = Calendar.getInstance().get(Calendar.YEAR)

    @Before
    fun setUp() {
        firestore.books = TestData.books + datedFinishedBook
        // DetailsViewModel adds and deletes books via FirebaseFirestore.getInstance().
        firestore.interceptDirectBookWrites()
        givenGoogleBooksReachable()
    }

    /** Google Books knows every shelf book, plus one that isn't saved. */
    private fun givenGoogleBooksReachable() {
        coEvery { bookRepository.getBookInfo(any()) } answers {
            val id = firstArg<String>()
            val book = (firestore.books + unsavedBook).first { it.googleBookId == id }
            flowOf(RemoteResult.Loading, RemoteResult.Success(book))
        }
    }

    @After
    fun tearDown() = FakeFirestore.releaseDirectWrites()

    private fun createViewModel(googleBookId: String) {
        viewModel = DetailsViewModel(
            bookRepository,
            fireRepository,
            authRepository,
            connectivity,
            celebrations,
            SavedStateHandle(mapOf("bookId" to googleBookId)),
            DependencyContextWrapper(fakeContext())
        )
        viewModel.setMainViewModel(mainViewModel)
    }

    /** Opens Details for [googleBookId] and waits for the book info (loaded off the main thread). */
    private suspend fun open(googleBookId: String) {
        createViewModel(googleBookId)
        viewModel.loadBook()
        viewModel.uiState.first { it.book != null }
    }

    private fun choose(label: Int) = dialog().choices.single { it.text == str(label) }.onClick()

    private fun selectedChoice() = dialog().choices.singleOrNull { it.selected }?.text

    // region Loading

    @Test
    fun open_unsavedBook_showsInfoAndNotSaved() = runTest {
        open(unsavedBook.googleBookId!!)

        assertEquals(unsavedBook, state.book)
        assertFalse(state.isSaved)
        assertNull(state.savedBook)
        assertEquals(LoadingState.Success, mainViewModel.loadingFlow.value)
    }

    @Test
    fun open_savedBook_showsShelfCopy() = runTest {
        open(TestData.readingBook.googleBookId!!)

        assertTrue(state.isSaved)
        assertEquals(TestData.readingBook, state.savedBook)
        assertEquals(TestData.readingBook.id, state.bookId)
    }

    @Test
    fun open_infoFails_bookNotOnShelf_showsError() = runTest {
        givenGoogleBooksUnreachable()
        createViewModel(unsavedBook.googleBookId!!)

        viewModel.loadBook()
        mainViewModel.dialogFlow.first { it != null }

        assertErrorDialog(R.string.error_network)
        assertTrue(mainViewModel.loadingFlow.value is LoadingState.Failed)
        assertFalse(state.showingShelfCopy)
    }

    @Test
    fun open_infoFails_bookOnShelf_showsShelfCopyInstead() = runTest {
        givenGoogleBooksUnreachable()
        createViewModel(TestData.readingBook.googleBookId!!)

        viewModel.loadBook()
        viewModel.uiState.first { it.book != null }

        assertEquals(TestData.readingBook, state.book)
        assertTrue(state.showingShelfCopy)
        assertTrue(state.isSaved)
        assertEquals(LoadingState.Success, mainViewModel.loadingFlow.first { it == LoadingState.Success })
        assertNull(mainViewModel.dialogFlow.value)
    }

    @Test
    fun reopen_infoFailsAfterItLoaded_keepsTheFullDetails() = runTest {
        open(TestData.readingBook.googleBookId!!)
        val loaded = state.book
        givenGoogleBooksUnreachable()
        mainViewModel.updateLoadingState(LoadingState.Idle)

        // e.g. back from Update book after going offline.
        viewModel.loadBook()
        mainViewModel.loadingFlow.first { it == LoadingState.Success }

        assertEquals(loaded, state.book)
        assertFalse(state.showingShelfCopy)
        assertNull(mainViewModel.dialogFlow.value)
    }

    @Test
    fun backOnline_whileShowingShelfCopy_loadsTheFullBook() = runTest {
        connectivity.online.value = false
        givenGoogleBooksUnreachable()
        createViewModel(TestData.readingBook.googleBookId!!)
        viewModel.loadBook()
        viewModel.uiState.first { it.showingShelfCopy }

        givenGoogleBooksReachable()
        connectivity.online.value = true

        viewModel.uiState.first { !it.showingShelfCopy }
        assertEquals(TestData.readingBook, state.book)
    }

    @Test
    fun backOnline_withFullDetailsShown_doesNotReload() = runTest {
        open(TestData.readingBook.googleBookId!!)
        connectivity.online.value = false

        connectivity.online.value = true

        // Only the first open asked Google Books.
        coVerify(exactly = 1) { bookRepository.getBookInfo(any()) }
    }

    private fun givenGoogleBooksUnreachable() {
        coEvery { bookRepository.getBookInfo(any()) } returns
            flowOf(RemoteResult.Loading, RemoteResult.Error(ErrorType.NetworkException(null)))
    }

    @Test
    fun savedFromAnotherScreen_updatesWithoutReopening() = runTest {
        open(unsavedBook.googleBookId!!)

        firestore.books += unsavedBook.copy(id = "elsewhere", userId = UID)
        fireRepository.fetchSavedBooksByUser()

        assertTrue(state.isSaved)
        assertEquals("elsewhere", state.bookId)
    }

    @Test
    fun editNotes_navigatesToUpdateScreen() = runTest {
        collectNavigation()
        open(TestData.readingBook.googleBookId!!)

        viewModel.navToUpdateScreen()

        val event = navigationEvents.filterIsInstance<NavigateTo>().single()
        assertEquals(UpdateScreenDestination(TestData.readingBook.googleBookId!!).route, event.to.route)
    }

    // endregion

    // region Adding to the shelf

    @Test
    fun addToShelf_asksForStatusWithNothingSelected() = runTest {
        open(unsavedBook.googleBookId!!)

        viewModel.toggleSavedBook()

        assertEquals(str(R.string.details_add_shelf), dialog().title)
        assertEquals(
            listOf(R.string.home_filter_want_to_read, R.string.home_filter_reading, R.string.home_filter_finished)
                .map(::str),
            dialog().choices.map { it.text }
        )
        assertNull(selectedChoice())
        assertNull(dialog().finishedDate)
    }

    @Test
    fun addAsReading_savesStartedBookAndConfirms() = runTest {
        open(unsavedBook.googleBookId!!)
        viewModel.toggleSavedBook()

        choose(R.string.home_filter_reading)

        val doc = firestore.addedBooks.single()
        assertEquals(UID, doc[StoredBook.USER_ID])
        assertEquals(unsavedBook.googleBookId, doc[StoredBook.GOOGLE_BOOK_ID])
        assertEquals(unsavedBook.title, doc[StoredBook.TITLE])
        assertNotNull(doc[StoredBook.STARTED_READING])
        assertFalse(StoredBook.FINISHED_READING in doc)
        assertEquals(str(R.string.book_saved_toast), mainViewModel.toastFlow.value)
        assertNull(mainViewModel.dialogFlow.value)
        assertTrue(state.isSaved)
        assertEquals(BookStatus.READING, state.savedBook?.bookStatus)
    }

    @Test
    fun addAsWantToRead_savesUnstartedBook() = runTest {
        open(unsavedBook.googleBookId!!)
        viewModel.toggleSavedBook()

        choose(R.string.home_filter_want_to_read)

        val doc = firestore.addedBooks.single()
        assertFalse(StoredBook.STARTED_READING in doc)
        assertFalse(StoredBook.FINISHED_READING in doc)
        assertEquals(BookStatus.LIBRARY, state.savedBook?.bookStatus)
    }

    @Test
    fun addAsFinished_asksForDateThenSavesIt() = runTest {
        open(unsavedBook.googleBookId!!)
        viewModel.toggleSavedBook()

        choose(R.string.home_filter_finished)
        // Nothing saved until the date is confirmed.
        assertTrue(firestore.addedBooks.isEmpty())
        with(dialog().finishedDate!!) {
            assertEquals(thisYear, year)
            assertNull(month)
            assertEquals("", day)
        }
        assertEquals(str(R.string.update_save), dialog().primaryButtonText)
        assertEquals(str(R.string.home_filter_finished), selectedChoice())

        dialog().finishedDate!!.onYearChanged(2025)
        dialog().finishedDate!!.onMonthChanged(3)
        dialog().finishedDate!!.onDayChanged("5")
        dialog().onPrimaryClick!!()

        val doc = firestore.addedBooks.single()
        assertEquals("05/03/2025", doc[StoredBook.FINISHED_ON])
        assertNotNull(doc[StoredBook.STARTED_READING])
        assertNotNull(doc[StoredBook.FINISHED_READING])
        assertEquals(BookStatus.FINISHED, state.savedBook?.bookStatus)
    }

    @Test
    fun addAsFinished_thisYear_popsUpTheStampCard() = runTest {
        open(unsavedBook.googleBookId!!)
        viewModel.toggleSavedBook()
        choose(R.string.home_filter_finished)

        dialog().onPrimaryClick!!() // the year defaults to this one

        assertEquals(unsavedBook.googleBookId, celebrations.pending.value)
    }

    @Test
    fun addAsFinished_inAnEarlierYear_noStampCard() = runTest {
        open(unsavedBook.googleBookId!!)
        viewModel.toggleSavedBook()
        choose(R.string.home_filter_finished)

        dialog().finishedDate!!.onYearChanged(thisYear - 1)
        dialog().onPrimaryClick!!()

        assertNull(celebrations.pending.value)
    }

    @Test
    fun addAsReading_noStampCard() = runTest {
        open(unsavedBook.googleBookId!!)
        viewModel.toggleSavedBook()

        choose(R.string.home_filter_reading)

        assertNull(celebrations.pending.value)
    }

    @Test
    fun changeReadingToFinished_thisYear_popsUpTheStampCard() = runTest {
        open(TestData.readingBook.googleBookId!!)
        viewModel.onChooseStatus()
        choose(R.string.home_filter_finished)

        dialog().onPrimaryClick!!()

        assertEquals(TestData.readingBook.googleBookId, celebrations.pending.value)
    }

    @Test
    fun addAsFinished_yearOnly_isAllowed() = runTest {
        open(unsavedBook.googleBookId!!)
        viewModel.toggleSavedBook()
        choose(R.string.home_filter_finished)

        dialog().finishedDate!!.onYearChanged(2019)
        dialog().onPrimaryClick!!()

        assertEquals("2019", firestore.addedBooks.single()[StoredBook.FINISHED_ON])
    }

    @Test
    fun addAsFinished_dayThatDoesNotExist_flagsDayAndSavesNothing() = runTest {
        open(unsavedBook.googleBookId!!)
        viewModel.toggleSavedBook()
        choose(R.string.home_filter_finished)

        dialog().finishedDate!!.onYearChanged(2023)
        dialog().finishedDate!!.onMonthChanged(2)
        dialog().finishedDate!!.onDayChanged("29")
        dialog().onPrimaryClick!!()

        assertTrue(dialog().finishedDate!!.dayError)
        assertTrue(firestore.addedBooks.isEmpty())

        // Fixing the day clears the error.
        dialog().finishedDate!!.onDayChanged("28")
        assertFalse(dialog().finishedDate!!.dayError)
    }

    @Test
    fun finishedDate_yearKeptInRangeAndClearingMonthClearsDay() = runTest {
        open(unsavedBook.googleBookId!!)
        viewModel.toggleSavedBook()
        choose(R.string.home_filter_finished)

        dialog().finishedDate!!.onYearChanged(thisYear + 3)
        assertEquals(thisYear, dialog().finishedDate!!.year)
        dialog().finishedDate!!.onMonthChanged(4)
        dialog().finishedDate!!.onDayChanged("1x2")
        assertEquals("12", dialog().finishedDate!!.day)

        dialog().finishedDate!!.onMonthChanged(null)

        assertEquals("", dialog().finishedDate!!.day)
    }

    @Test
    fun addToShelf_offline_savesLocallyAndSaysItWillSync() = runTest {
        open(unsavedBook.googleBookId!!)
        firestore.online = false
        viewModel.toggleSavedBook()

        choose(R.string.home_filter_reading)

        assertEquals(str(R.string.saved_offline_toast), mainViewModel.toastFlow.value)
        assertTrue(state.isSaved)
    }

    @Test
    fun chooseStatus_offline_updatesLocallyAndSaysItWillSync() = runTest {
        open(TestData.readingBook.googleBookId!!)
        firestore.online = false
        viewModel.onChooseStatus()

        choose(R.string.home_filter_want_to_read)

        assertEquals(str(R.string.saved_offline_toast), mainViewModel.toastFlow.value)
        assertEquals(BookStatus.LIBRARY, state.savedBook?.bookStatus)
    }

    @Test
    fun remove_offline_removesLocallyAndSaysItWillSync() = runTest {
        open(TestData.readingBook.googleBookId!!)
        firestore.online = false

        viewModel.toggleSavedBook()
        dialog().onPrimaryClick!!()

        assertEquals(str(R.string.removed_offline_toast), mainViewModel.toastFlow.value)
        assertFalse(state.isSaved)
    }

    @Test
    fun addToShelf_saveFails_saysSoAndStaysUnsaved() = runTest {
        firestore.bookAddError = FakeFirestore.unavailable()
        open(unsavedBook.googleBookId!!)
        viewModel.toggleSavedBook()

        choose(R.string.home_filter_reading)

        assertEquals(str(R.string.book_save_error), mainViewModel.toastFlow.value)
        assertFalse(state.isSaved)
    }

    @Test
    fun addToShelf_cancelled_savesNothing() = runTest {
        open(unsavedBook.googleBookId!!)
        viewModel.toggleSavedBook()

        dialog().onSecondaryClick!!()

        assertTrue(firestore.addedBooks.isEmpty())
        assertNull(mainViewModel.dialogFlow.value)
    }

    @Test
    fun addToShelf_signedOut_savesNothing() = runTest {
        open(unsavedBook.googleBookId!!)
        viewModel.toggleSavedBook()
        authRepository.current = null

        choose(R.string.home_filter_reading)

        assertTrue(firestore.addedBooks.isEmpty())
    }

    @Test
    fun chooseStatus_onUnsavedBook_doesNothing() = runTest {
        open(unsavedBook.googleBookId!!)

        viewModel.onChooseStatus()

        assertNull(mainViewModel.dialogFlow.value)
    }

    // endregion

    // region Removing from the shelf

    @Test
    fun remove_confirmed_deletesAndConfirms() = runTest {
        open(TestData.readingBook.googleBookId!!)

        viewModel.toggleSavedBook()
        assertEquals(str(R.string.remove_book_title), dialog().title)
        dialog().onPrimaryClick!!()

        assertEquals(listOf(TestData.readingBook.id), firestore.deletedBookIds)
        assertEquals(str(R.string.book_removed_toast), mainViewModel.toastFlow.value)
        assertFalse(state.isSaved)
        assertNull(state.savedBook)
    }

    @Test
    fun remove_duplicateCopies_removesThemAll() = runTest {
        firestore.books += TestData.readingBook.copy(id = "reading-duplicate")
        open(TestData.readingBook.googleBookId!!)

        viewModel.toggleSavedBook()
        dialog().onPrimaryClick!!()

        assertEquals(setOf(TestData.readingBook.id, "reading-duplicate"), firestore.deletedBookIds.toSet())
        assertFalse(state.isSaved)
    }

    @Test
    fun remove_fails_saysSoAndKeepsBook() = runTest {
        firestore.bookDeleteErrors[TestData.readingBook.id!!] = FakeFirestore.unavailable()
        open(TestData.readingBook.googleBookId!!)

        viewModel.toggleSavedBook()
        dialog().onPrimaryClick!!()

        assertEquals(str(R.string.book_remove_error), mainViewModel.toastFlow.value)
        assertTrue(state.isSaved)
        assertEquals(TestData.readingBook, state.savedBook)
    }

    @Test
    fun remove_oneDuplicateCopyFails_saysSoAndShowsTheCopyLeft() = runTest {
        val duplicate = TestData.readingBook.copy(id = "reading-duplicate")
        firestore.books += duplicate
        firestore.bookDeleteErrors[duplicate.id!!] = FakeFirestore.unavailable()
        open(TestData.readingBook.googleBookId!!)

        viewModel.toggleSavedBook()
        dialog().onPrimaryClick!!()

        assertEquals(str(R.string.book_remove_error), mainViewModel.toastFlow.value)
        // The other copy did go, so the screen refreshes to what's left.
        assertTrue(state.isSaved)
        assertEquals(duplicate.id, state.bookId)
    }

    @Test
    fun remove_cancelled_keepsBook() = runTest {
        open(TestData.readingBook.googleBookId!!)

        viewModel.toggleSavedBook()
        dialog().onSecondaryClick!!()

        assertTrue(firestore.deletedBookIds.isEmpty())
        assertTrue(state.isSaved)
        assertNull(mainViewModel.dialogFlow.value)
    }

    // endregion

    // region Changing status

    @Test
    fun chooseStatus_savedBook_showsCurrentStatus() = runTest {
        open(TestData.readingBook.googleBookId!!)

        viewModel.onChooseStatus()

        assertEquals(str(R.string.details_update_status), dialog().title)
        assertEquals(str(R.string.home_filter_reading), selectedChoice())
    }

    @Test
    fun chooseStatus_sameStatus_changesNothing() = runTest {
        open(TestData.readingBook.googleBookId!!)
        viewModel.onChooseStatus()

        choose(R.string.home_filter_reading)

        assertTrue(firestore.bookUpdates.isEmpty())
        assertNull(mainViewModel.dialogFlow.value)
    }

    @Test
    fun chooseStatus_readingToWantToRead_clearsDatesAndConfirms() = runTest {
        open(TestData.readingBook.googleBookId!!)
        viewModel.onChooseStatus()

        choose(R.string.home_filter_want_to_read)

        val (id, updates) = firestore.bookUpdates.single()
        assertEquals(TestData.readingBook.id, id)
        assertNull(updates[StoredBook.STARTED_READING])
        assertNull(updates[StoredBook.FINISHED_READING])
        assertNull(updates[StoredBook.FINISHED_ON])
        assertEquals(str(R.string.update_book_success), mainViewModel.toastFlow.value)
        assertEquals(BookStatus.LIBRARY, state.savedBook?.bookStatus)
    }

    @Test
    fun chooseStatus_readingToFinished_keepsStartDate() = runTest {
        open(TestData.readingBook.googleBookId!!)
        viewModel.onChooseStatus()
        choose(R.string.home_filter_finished)

        dialog().onPrimaryClick!!()

        val updates = firestore.bookUpdates.single().second
        assertSame(TestData.readingBook.startedReading, updates[StoredBook.STARTED_READING])
        assertNotNull(updates[StoredBook.FINISHED_READING])
        assertEquals(thisYear.toString(), updates[StoredBook.FINISHED_ON])
        assertEquals(BookStatus.FINISHED, state.savedBook?.bookStatus)
    }

    @Test
    fun chooseStatus_finishedBook_opensWithItsFinishedDate() = runTest {
        open(datedFinishedBook.googleBookId!!)

        viewModel.onChooseStatus()

        assertEquals(str(R.string.home_filter_finished), selectedChoice())
        with(dialog().finishedDate!!) {
            assertEquals(2025, year)
            assertEquals(3, month)
            assertEquals("5", day)
        }
    }

    @Test
    fun chooseStatus_updateFails_showsErrorAndKeepsStatus() = runTest {
        firestore.bookUpdateError = IllegalStateException("offline")
        open(TestData.readingBook.googleBookId!!)
        viewModel.onChooseStatus()

        choose(R.string.home_filter_want_to_read)

        assertEquals(str(R.string.update_book_error), mainViewModel.toastFlow.value)
        assertEquals(BookStatus.READING, state.savedBook?.bookStatus)
    }

    // endregion
}