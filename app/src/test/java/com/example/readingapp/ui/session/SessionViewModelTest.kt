package com.example.readingapp.ui.session

import com.example.readingapp.MainViewModel
import com.example.readingapp.R
import com.example.readingapp.auth.FakeAuthRepository
import com.example.readingapp.common.DependencyContextWrapper
import com.example.readingapp.data.DataOrException
import com.example.readingapp.model.MBook
import com.example.readingapp.model.MSession
import com.example.readingapp.model.TextLimits
import com.example.readingapp.repo.FireRepository
import com.example.readingapp.testing.MainDispatcherRule
import com.example.readingapp.testing.TestData
import com.example.readingapp.testing.TestData.UID
import com.example.readingapp.testing.TestDispatcherProvider
import com.example.readingapp.testing.fakeContext
import com.example.readingapp.testing.str
import com.example.readingapp.ui.components.DialogState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class SessionViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authRepository = FakeAuthRepository().apply { current = TestData.authUser }
    private val fireRepository = mockk<FireRepository>()
    private val mainViewModel = MainViewModel()
    private lateinit var viewModel: SessionViewModel

    private val state get() = viewModel.uiState.value
    private val today = Calendar.getInstance()
    private val todayYear = today.get(Calendar.YEAR)
    private val todayMonth = today.get(Calendar.MONTH) + 1
    private val todayDay = today.get(Calendar.DAY_OF_MONTH)
    private val todayDisplay = "%02d/%02d/%d".format(Locale.UK, todayDay, todayMonth, todayYear)

    private val newSession = MSession(id = "session-2", userId = UID, pages = 12)

    @Before
    fun setUp() {
        givenSavedBooks(TestData.books + TestData.otherReadingBook)
        givenSessions(listOf(TestData.session))
        coEvery { fireRepository.addSession(any()) } just runs
        coEvery { fireRepository.deleteSession(any()) } just runs

        viewModel = SessionViewModel(
            fireRepository,
            authRepository,
            TestDispatcherProvider(mainDispatcherRule.testDispatcher),
            DependencyContextWrapper(fakeContext())
        )
        viewModel.setMainViewModel(mainViewModel)
    }

    private fun givenSavedBooks(books: List<MBook>?, error: Exception? = null) {
        coEvery { fireRepository.fetchSavedBooksByUser(UID) } returns DataOrException(data = books, e = error)
    }

    private fun givenSessions(sessions: List<MSession>) {
        coEvery { fireRepository.fetchSessions(UID) } returns sessions
    }

    /** Loads, opens the log form and fills in a valid entry for [TestData.readingBook]. */
    private fun fillValidLog(pages: String = "12", note: String = "") {
        viewModel.onLoad()
        viewModel.onStartLog()
        viewModel.onBookSelected(TestData.readingBook.googleBookId!!)
        viewModel.onPagesChanged(pages)
        viewModel.onNoteChanged(note)
    }

    private fun dialog(): DialogState = mainViewModel.dialogFlow.value ?: error("No dialog shown")

    private fun assertNothingSaved() = coVerify(exactly = 0) { fireRepository.addSession(any()) }

    // region Initial state and loading

    @Test
    fun initialState_dateIsTodayAndBooksNotYetLoaded() {
        assertEquals(todayYear, state.year)
        assertEquals(todayMonth, state.month)
        assertEquals(todayDay.toString(), state.day)
        assertFalse(state.booksLoaded)
    }

    @Test
    fun onLoad_showsOnlyReadingBooksSortedByTitleAndSelectsFirst() = runTest {
        viewModel.onLoad()

        assertEquals(listOf(TestData.otherReadingBook, TestData.readingBook), state.books)
        assertTrue(state.booksLoaded)
        assertEquals(listOf(TestData.session), state.sessions)
        assertEquals(TestData.otherReadingBook.googleBookId, state.selectedBookId)
    }

    @Test
    fun onLoad_keepsSelectedBookIfStillReading() = runTest {
        viewModel.onBookSelected(TestData.readingBook.googleBookId!!)

        viewModel.onLoad()

        assertEquals(TestData.readingBook.googleBookId, state.selectedBookId)
    }

    @Test
    fun onLoad_selectedBookNoLongerReading_selectsFirstReadingBook() = runTest {
        viewModel.onBookSelected(TestData.finishedBook.googleBookId!!)

        viewModel.onLoad()

        assertEquals(TestData.otherReadingBook.googleBookId, state.selectedBookId)
    }

    @Test
    fun onLoad_bookWithoutGoogleId_isSelectedByDocumentId() = runTest {
        val legacy = TestData.readingBook.copy(googleBookId = null)
        givenSavedBooks(listOf(legacy))

        viewModel.onLoad()

        assertEquals(legacy.id, state.selectedBookId)
    }

    @Test
    fun onLoad_noReadingBooks_loadedButEmptyWithNoSelection() = runTest {
        givenSavedBooks(listOf(TestData.libraryBook, TestData.finishedBook))

        viewModel.onLoad()

        assertTrue(state.booksLoaded)
        assertTrue(state.books.isEmpty())
        assertNull(state.selectedBookId)
    }

    @Test
    fun onLoad_signedOut_showsErrorToastAndFetchesNothing() = runTest {
        authRepository.current = null

        viewModel.onLoad()

        coVerify(exactly = 0) { fireRepository.fetchSavedBooksByUser(any<String>()) }
        assertFalse(state.booksLoaded)
        assertEquals(str(R.string.session_error), mainViewModel.toastFlow.value)
    }

    @Test
    fun onLoad_booksFetchFails_showsErrorToast() = runTest {
        givenSavedBooks(null, error = IllegalStateException("Firestore unavailable"))

        viewModel.onLoad()

        assertFalse(state.booksLoaded)
        assertEquals(str(R.string.session_error), mainViewModel.toastFlow.value)
    }

    @Test
    fun onLoad_sessionsFetchFails_showsErrorToast() = runTest {
        coEvery { fireRepository.fetchSessions(UID) } throws IllegalStateException("offline")

        viewModel.onLoad()

        assertFalse(state.booksLoaded)
        assertEquals(str(R.string.session_error), mainViewModel.toastFlow.value)
    }

    // endregion

    // region Log form

    @Test
    fun onStartLog_opensEmptyFormDatedToday() = runTest {
        viewModel.onLoad()
        viewModel.onPagesChanged("0")
        viewModel.onNoteChanged("left over")
        viewModel.onSaveSession() // rejected: leaves a pages error behind
        assertTrue(state.pagesError)
        viewModel.onYearChanged(2001)

        viewModel.onStartLog()

        assertTrue(state.logging)
        assertFalse(state.changingDate)
        assertEquals("", state.pagesInput)
        assertEquals("", state.noteInput)
        assertEquals(todayYear, state.year)
        assertFalse(state.bookError || state.pagesError || state.dateError)
    }

    @Test
    fun onCancelLog_closesAndClearsForm() = runTest {
        fillValidLog(note = "Chapter 3")
        viewModel.onChangeDay()

        viewModel.onCancelLog()

        assertFalse(state.logging)
        assertFalse(state.changingDate)
        assertEquals("", state.pagesInput)
        assertEquals("", state.noteInput)
        assertNothingSaved()
    }

    @Test
    fun onPagesChanged_keepsOnlyDigitsUpToFive() {
        viewModel.onPagesChanged("1a2-3 4567")

        assertEquals("12345", state.pagesInput)
    }

    @Test
    fun onNoteChanged_isLimitedToSessionNoteLength() {
        viewModel.onNoteChanged("x".repeat(TextLimits.SESSION_NOTE + 20))

        assertEquals(TextLimits.SESSION_NOTE, state.noteInput.length)
    }

    @Test
    fun onDayChanged_keepsOnlyTwoDigits() {
        viewModel.onDayChanged("3x15")

        assertEquals("31", state.day)
    }

    @Test
    fun onYearChanged_isClampedToAllowedRange() {
        viewModel.onYearChanged(1850)
        assertEquals(1900, state.year)

        viewModel.onYearChanged(todayYear + 5)
        assertEquals(todayYear, state.year)
    }

    @Test
    fun onMonthChanged_clearingMonthAlsoClearsDay() {
        viewModel.onMonthChanged(null)

        assertNull(state.month)
        assertEquals("", state.day)
    }

    @Test
    fun onUseToday_resetsDateAndClosesDatePicker() {
        viewModel.onChangeDay()
        viewModel.onYearChanged(2001)
        viewModel.onMonthChanged(3)
        viewModel.onDayChanged("9")

        viewModel.onUseToday()

        assertFalse(state.changingDate)
        assertEquals(todayYear, state.year)
        assertEquals(todayMonth, state.month)
        assertEquals(todayDay.toString(), state.day)
    }

    // endregion

    // region Saving

    @Test
    fun onSaveSession_valid_savesRefreshesClosesAndConfirms() = runTest {
        fillValidLog(pages = "12", note = "  Chapter 3  ")
        givenSessions(listOf(newSession, TestData.session))
        val saved = slot<MSession>()
        coEvery { fireRepository.addSession(capture(saved)) } just runs

        viewModel.onSaveSession()

        with(saved.captured) {
            assertEquals(UID, userId)
            assertEquals(TestData.readingBook.googleBookId, googleBookId)
            assertEquals(TestData.readingBook.title, title)
            assertEquals(12, pages)
            assertEquals("Chapter 3", note)
            assertEquals(todayDisplay, readOn)
            assertNotNull(readAt)
        }
        assertEquals(listOf(newSession, TestData.session), state.sessions)
        assertFalse(state.logging)
        assertEquals("", state.pagesInput)
        assertEquals(str(R.string.session_saved), mainViewModel.toastFlow.value)
    }

    @Test
    fun onSaveSession_blankNote_isSavedWithoutNote() = runTest {
        fillValidLog(note = "   ")
        val saved = slot<MSession>()
        coEvery { fireRepository.addSession(capture(saved)) } just runs

        viewModel.onSaveSession()

        assertNull(saved.captured.note)
    }

    @Test
    fun onSaveSession_pastDay_usesThatDate() = runTest {
        fillValidLog()
        viewModel.onChangeDay()
        viewModel.onYearChanged(2024)
        viewModel.onMonthChanged(2)
        viewModel.onDayChanged("29")
        val saved = slot<MSession>()
        coEvery { fireRepository.addSession(capture(saved)) } just runs

        viewModel.onSaveSession()

        assertEquals("29/02/2024", saved.captured.readOn)
    }

    @Test
    fun onSaveSession_nothingFilledIn_flagsBookAndPages() = runTest {
        givenSavedBooks(emptyList())
        viewModel.onLoad()
        viewModel.onStartLog()

        viewModel.onSaveSession()

        assertTrue(state.bookError)
        assertTrue(state.pagesError)
        assertFalse(state.dateError)
        assertTrue(state.logging)
        assertNothingSaved()
    }

    @Test
    fun onSaveSession_zeroPages_flagsPages() = runTest {
        fillValidLog(pages = "0")

        viewModel.onSaveSession()

        assertTrue(state.pagesError)
        assertFalse(state.bookError)
        assertNothingSaved()
    }

    @Test
    fun onSaveSession_dayDoesNotExist_flagsDate() = runTest {
        fillValidLog()
        viewModel.onYearChanged(2023)
        viewModel.onMonthChanged(2)
        viewModel.onDayChanged("29")

        viewModel.onSaveSession()

        assertTrue(state.dateError)
        assertNothingSaved()
    }

    @Test
    fun onSaveSession_noDay_flagsDate() = runTest {
        fillValidLog()
        viewModel.onDayChanged("")

        viewModel.onSaveSession()

        assertTrue(state.dateError)
        assertNothingSaved()
    }

    @Test
    fun editingAField_clearsItsError() = runTest {
        givenSavedBooks(emptyList())
        viewModel.onLoad()
        viewModel.onDayChanged("")
        viewModel.onSaveSession()

        viewModel.onBookSelected(TestData.readingBook.googleBookId!!)
        viewModel.onPagesChanged("5")
        viewModel.onDayChanged("1")

        assertFalse(state.bookError || state.pagesError || state.dateError)
    }

    @Test
    fun onSaveSession_signedOut_savesNothing() = runTest {
        fillValidLog()
        authRepository.current = null

        viewModel.onSaveSession()

        assertNothingSaved()
    }

    @Test
    fun onSaveSession_saveFails_showsErrorAndKeepsForm() = runTest {
        fillValidLog(pages = "12")
        coEvery { fireRepository.addSession(any()) } throws IllegalStateException("offline")

        viewModel.onSaveSession()

        assertEquals(str(R.string.session_error), mainViewModel.toastFlow.value)
        assertTrue(state.logging)
        assertEquals("12", state.pagesInput)
    }

    // endregion

    // region Removing

    @Test
    fun onRemoveSession_confirm_deletesRefreshesAndConfirms() = runTest {
        viewModel.onLoad()
        givenSessions(emptyList())

        viewModel.onRemoveSession(TestData.session)
        assertEquals(str(R.string.session_remove_title), dialog().title)
        dialog().onPrimaryClick!!()

        coVerify { fireRepository.deleteSession(TestData.session.id!!) }
        assertTrue(state.sessions.isEmpty())
        assertNull(mainViewModel.dialogFlow.value)
        assertEquals(str(R.string.session_removed), mainViewModel.toastFlow.value)
    }

    @Test
    fun onRemoveSession_cancel_deletesNothing() = runTest {
        viewModel.onRemoveSession(TestData.session)

        dialog().onSecondaryClick!!()

        coVerify(exactly = 0) { fireRepository.deleteSession(any()) }
        assertNull(mainViewModel.dialogFlow.value)
    }

    @Test
    fun onRemoveSession_unsavedSession_showsNoDialog() {
        viewModel.onRemoveSession(TestData.session.copy(id = null))

        assertNull(mainViewModel.dialogFlow.value)
    }

    @Test
    fun onRemoveSession_deleteFails_showsErrorAndKeepsSessions() = runTest {
        viewModel.onLoad()
        coEvery { fireRepository.deleteSession(any()) } throws IllegalStateException("offline")

        viewModel.onRemoveSession(TestData.session)
        dialog().onPrimaryClick!!()

        assertEquals(listOf(TestData.session), state.sessions)
        assertEquals(str(R.string.session_error), mainViewModel.toastFlow.value)
    }

    // endregion
}
