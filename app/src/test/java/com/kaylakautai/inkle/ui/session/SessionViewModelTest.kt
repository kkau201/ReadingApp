package com.kaylakautai.inkle.ui.session

import com.kaylakautai.inkle.R
import com.kaylakautai.inkle.auth.FakeAuthRepository
import com.kaylakautai.inkle.common.DependencyContextWrapper
import com.kaylakautai.inkle.data.DataOrException
import com.kaylakautai.inkle.model.MBook
import com.kaylakautai.inkle.model.MSession
import com.kaylakautai.inkle.model.StoredBook
import com.kaylakautai.inkle.model.TextLimits
import com.kaylakautai.inkle.repo.FireRepository
import com.kaylakautai.inkle.repo.WriteResult
import com.kaylakautai.inkle.testing.BaseViewModelTest
import com.kaylakautai.inkle.testing.TestData
import com.kaylakautai.inkle.testing.TestData.UID
import com.kaylakautai.inkle.testing.fakeContext
import com.kaylakautai.inkle.testing.str
import com.kaylakautai.inkle.ui.stamps.StampCelebrations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Calendar
import java.util.Locale

class SessionViewModelTest : BaseViewModelTest() {

    private val authRepository = FakeAuthRepository().apply { current = TestData.authUser }
    private val liveBooks = MutableStateFlow<List<MBook>>(emptyList())
    private val liveSessions = MutableStateFlow<List<MSession>>(emptyList())
    private val fireRepository = mockk<FireRepository>().also { repo ->
        every { repo.savedBooks } returns liveBooks
        every { repo.sessions } returns liveSessions
    }
    private val celebrations = StampCelebrations()
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
        coEvery { fireRepository.addSession(any()) } returns WriteResult.SAVED
        coEvery { fireRepository.deleteSession(any()) } returns WriteResult.SAVED

        viewModel = SessionViewModel(
            fireRepository,
            authRepository,
            dispatcherProvider,
            celebrations,
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
    fun sessionsChangeAfterLoad_updatesJournal() = runTest {
        viewModel.onLoad()

        liveSessions.value = listOf(newSession, TestData.session)

        assertEquals(listOf(newSession, TestData.session), state.sessions)
    }

    @Test
    fun booksChangeAfterLoad_updatesBooksAndMovesSelectionOffFinishedBook() = runTest {
        viewModel.onLoad()

        // otherReadingBook (selected) was finished on another device.
        liveBooks.value = listOf(TestData.readingBook)

        assertEquals(listOf(TestData.readingBook), state.books)
        assertEquals(TestData.readingBook.googleBookId, state.selectedBookId)
    }

    @Test
    fun changesAfterFailedLoad_areIgnored() = runTest {
        givenSavedBooks(null, error = IllegalStateException("Firestore unavailable"))
        viewModel.onLoad()

        liveSessions.value = listOf(newSession)
        liveBooks.value = listOf(TestData.readingBook)

        assertFalse(state.booksLoaded)
        assertTrue(state.sessions.isEmpty())
        assertTrue(state.books.isEmpty())
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
    fun onLoad_signedOut_showsLoadErrorAndFetchesNothing() = runTest {
        authRepository.current = null

        viewModel.onLoad()

        coVerify(exactly = 0) { fireRepository.fetchSavedBooksByUser(any<String>()) }
        assertFalse(state.booksLoaded)
        assertTrue(state.loadFailed)
    }

    @Test
    fun onLoad_booksFetchFails_showsLoadErrorNotSaveToast() = runTest {
        givenSavedBooks(null, error = IllegalStateException("Firestore unavailable"))

        viewModel.onLoad()

        assertFalse(state.booksLoaded)
        assertTrue(state.loadFailed)
        assertNull(mainViewModel.toastFlow.value)
    }

    @Test
    fun onLoad_sessionsFetchFails_showsLoadError() = runTest {
        coEvery { fireRepository.fetchSessions(UID) } throws IllegalStateException("offline")

        viewModel.onLoad()

        assertFalse(state.booksLoaded)
        assertTrue(state.loadFailed)
    }

    @Test
    fun onRetryClick_afterFailedLoad_loadsAndClearsError() = runTest {
        givenSavedBooks(null, error = IllegalStateException("Firestore unavailable"))
        viewModel.onLoad()
        givenSavedBooks(TestData.books + TestData.otherReadingBook)

        viewModel.onRetryClick()

        assertTrue(state.booksLoaded)
        assertFalse(state.loadFailed)
        assertEquals(listOf(TestData.otherReadingBook, TestData.readingBook), state.books)
        assertEquals(listOf(TestData.session), state.sessions)
    }

    @Test
    fun onRetryClick_failsAgain_keepsShowingError() = runTest {
        givenSavedBooks(null, error = IllegalStateException("Firestore unavailable"))
        viewModel.onLoad()

        viewModel.onRetryClick()

        assertFalse(state.booksLoaded)
        assertTrue(state.loadFailed)
    }

    @Test
    fun onLoad_succeeds_hasNoLoadError() = runTest {
        viewModel.onLoad()

        assertFalse(state.loadFailed)
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
        coEvery { fireRepository.addSession(capture(saved)) } returns WriteResult.SAVED

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
        coEvery { fireRepository.addSession(capture(saved)) } returns WriteResult.SAVED

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
        coEvery { fireRepository.addSession(capture(saved)) } returns WriteResult.SAVED

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
    fun onSaveSession_offline_savesAndSaysItWillSync() = runTest {
        fillValidLog(pages = "12")
        coEvery { fireRepository.addSession(any()) } returns WriteResult.SAVED_OFFLINE
        givenSessions(listOf(newSession, TestData.session)) // read from the device cache

        viewModel.onSaveSession()

        assertEquals(str(R.string.saved_offline_toast), mainViewModel.toastFlow.value)
        assertFalse(state.logging)
        assertEquals(listOf(newSession, TestData.session), state.sessions)
    }

    @Test
    fun onSaveSession_tappedAgainWhileSaving_logsOnce() = runTest {
        fillValidLog(pages = "12")
        val confirmed = CompletableDeferred<WriteResult>()
        coEvery { fireRepository.addSession(any()) } coAnswers { confirmed.await() }

        viewModel.onSaveSession()
        viewModel.onSaveSession()
        confirmed.complete(WriteResult.SAVED)

        coVerify(exactly = 1) { fireRepository.addSession(any()) }
        assertEquals(str(R.string.session_saved), mainViewModel.toastFlow.value)
    }

    @Test
    fun onSaveSession_saveFails_showsErrorAndKeepsForm() = runTest {
        fillValidLog(pages = "12")
        coEvery { fireRepository.addSession(any()) } throws IllegalStateException("rejected")

        viewModel.onSaveSession()

        assertEquals(str(R.string.session_error), mainViewModel.toastFlow.value)
        assertTrue(state.logging)
        assertEquals("12", state.pagesInput)
    }

    // endregion

    // region Finishing on the last page

    /**
     * [TestData.readingBook] with [pageCount] pages and [TestData.session]'s 20 already logged.
     * Logging [pages] more and saving leaves 20 + [pages] logged in all.
     */
    private fun logPagesOfBook(pageCount: Int?, pages: Int) {
        givenSavedBooks(listOf(TestData.readingBook.copy(pageCount = pageCount)))
        val added = MSession(
            id = "session-2",
            userId = UID,
            googleBookId = TestData.readingBook.googleBookId,
            pages = pages
        )
        // Another book's pages don't count towards this one.
        val otherBook = MSession(id = "session-3", userId = UID, googleBookId = "gb-other", pages = 500)
        fillValidLog(pages = pages.toString())
        givenSessions(listOf(added, TestData.session, otherBook))
        viewModel.onSaveSession()
    }

    private fun givenBookUpdate(result: WriteResult = WriteResult.SAVED, error: Exception? = null) {
        every { fireRepository.updateBook(any(), any(), any(), any()) } answers {
            if (error != null) arg<(Exception) -> Unit>(3)(error)
            else thirdArg<(WriteResult) -> Unit>()(result)
        }
    }

    private val form get() = state.finishing ?: error("Finish form not open")

    /** Captures what the next book update writes, and reports it saved. */
    private fun captureBookUpdate(): io.mockk.CapturingSlot<Map<String, Any?>> {
        val updates = slot<Map<String, Any?>>()
        every { fireRepository.updateBook(any(), capture(updates), any(), any()) } answers {
            thirdArg<(WriteResult) -> Unit>()(WriteResult.SAVED)
        }
        return updates
    }

    @Test
    fun onSaveSession_reachesPageCount_offersToFinish() = runTest {
        logPagesOfBook(pageCount = 32, pages = 12)

        assertEquals(str(R.string.session_finish_title), form.title)
        assertEquals(str(R.string.session_finish_not_yet), form.cancelText)
        assertEquals(0, form.rating)
        assertEquals("", form.noteInput)
    }

    @Test
    fun onSaveSession_goesPastPageCount_offersToFinish() = runTest {
        logPagesOfBook(pageCount = 30, pages = 12)

        assertEquals(str(R.string.session_finish_title), form.title)
    }

    @Test
    fun onSaveSession_belowPageCount_doesNotOffer() = runTest {
        logPagesOfBook(pageCount = 33, pages = 12)

        assertNull(state.finishing)
    }

    @Test
    fun onSaveSession_unknownPageCount_doesNotOffer() = runTest {
        logPagesOfBook(pageCount = null, pages = 12)

        assertNull(state.finishing)
    }

    @Test
    fun finishOffer_confirm_marksFinishedOnTheSessionDay() = runTest {
        logPagesOfBook(pageCount = 32, pages = 12)
        val updates = captureBookUpdate()

        viewModel.onConfirmFinish()

        verify { fireRepository.updateBook(TestData.readingBook.id!!, any(), any(), any()) }
        assertEquals(todayDisplay, updates.captured[StoredBook.FINISHED_ON])
        assertNotNull(updates.captured[StoredBook.FINISHED_READING])
        assertNull(state.finishing)
        assertEquals(str(R.string.session_marked_finished), mainViewModel.toastFlow.value)
    }

    @Test
    fun finishOffer_confirm_popsUpTheStampCard() = runTest {
        givenBookUpdate()
        logPagesOfBook(pageCount = 32, pages = 12)

        viewModel.onConfirmFinish()

        // Finished today, so this year: its stamp lands.
        assertEquals(TestData.readingBook.googleBookId, celebrations.pending.value)
    }

    @Test
    fun finishOffer_updateFails_noStampCard() = runTest {
        givenBookUpdate(error = IllegalStateException("rejected"))
        logPagesOfBook(pageCount = 32, pages = 12)

        viewModel.onConfirmFinish()

        assertNull(celebrations.pending.value)
    }

    @Test
    fun finishOffer_confirmOffline_saysItWillSync() = runTest {
        givenBookUpdate(result = WriteResult.SAVED_OFFLINE)
        logPagesOfBook(pageCount = 32, pages = 12)

        viewModel.onConfirmFinish()

        assertEquals(str(R.string.saved_offline_toast), mainViewModel.toastFlow.value)
    }

    @Test
    fun finishOffer_updateFails_showsError() = runTest {
        givenBookUpdate(error = IllegalStateException("rejected"))
        logPagesOfBook(pageCount = 32, pages = 12)

        viewModel.onConfirmFinish()

        assertEquals(str(R.string.update_book_error), mainViewModel.toastFlow.value)
    }

    @Test
    fun finishOffer_notYet_leavesBookAsReading() = runTest {
        givenBookUpdate()
        logPagesOfBook(pageCount = 32, pages = 12)

        viewModel.onCancelFinish()

        assertNull(state.finishing)
        verify(exactly = 0) { fireRepository.updateBook(any(), any(), any(), any()) }
        assertEquals(str(R.string.session_saved), mainViewModel.toastFlow.value)
    }

    @Test
    fun onMarkFinishedClick_asksAboutTheSelectedBook() = runTest {
        viewModel.onLoad()
        viewModel.onBookSelected(TestData.readingBook.googleBookId!!)

        viewModel.onMarkFinishedClick()

        assertEquals(str(R.string.session_mark_finished_title), form.title)
        assertEquals(str(R.string.update_cancel), form.cancelText)

        // ...and finishing applies to that book, not the first one in the carousel.
        givenBookUpdate()
        viewModel.onConfirmFinish()
        verify { fireRepository.updateBook(TestData.readingBook.id!!, any(), any(), any()) }
    }

    @Test
    fun onMarkFinishedClick_confirm_marksSelectedBookFinishedToday() = runTest {
        viewModel.onLoad()
        viewModel.onBookSelected(TestData.readingBook.googleBookId!!)
        val updates = captureBookUpdate()
        viewModel.onMarkFinishedClick()

        viewModel.onConfirmFinish()

        assertEquals(todayDisplay, updates.captured[StoredBook.FINISHED_ON])
        assertEquals(str(R.string.session_marked_finished), mainViewModel.toastFlow.value)
    }

    @Test
    fun onMarkFinishedClick_cancel_changesNothing() = runTest {
        givenBookUpdate()
        viewModel.onLoad()
        viewModel.onMarkFinishedClick()

        viewModel.onCancelFinish()

        assertNull(state.finishing)
        verify(exactly = 0) { fireRepository.updateBook(any(), any(), any(), any()) }
    }

    @Test
    fun onMarkFinishedClick_noBooksBeingRead_doesNothing() = runTest {
        givenSavedBooks(emptyList())
        viewModel.onLoad()

        viewModel.onMarkFinishedClick()

        assertNull(state.finishing)
    }

    // Rating and note

    /** Opens the finish form for [TestData.readingBook] as stored with [rating] and [notes]. */
    private fun openFinishFormFor(rating: Double? = null, notes: String? = null) {
        givenSavedBooks(listOf(TestData.readingBook.copy(rating = rating, notes = notes)))
        viewModel.onLoad()
        viewModel.onMarkFinishedClick()
    }

    @Test
    fun finishForm_startsFromTheBooksRatingAndNote() = runTest {
        openFinishFormFor(rating = 3.0, notes = "Loved Anne.")

        assertEquals(3, form.rating)
        assertEquals("Loved Anne.", form.noteInput)
    }

    @Test
    fun finishForm_ratingAndNote_areSavedWithIt() = runTest {
        openFinishFormFor()
        val updates = captureBookUpdate()
        viewModel.onFinishRatingChanged(4)
        viewModel.onFinishNoteChanged("  Wonderful ending.  ")

        viewModel.onConfirmFinish()

        assertEquals(4.0, updates.captured[StoredBook.RATING])
        assertEquals("Wonderful ending.", updates.captured[StoredBook.NOTES])
    }

    @Test
    fun finishForm_tappingTheChosenStarAgain_clearsTheRating() = runTest {
        openFinishFormFor()
        viewModel.onFinishRatingChanged(4)

        viewModel.onFinishRatingChanged(4)

        assertEquals(0, form.rating)
    }

    @Test
    fun finishForm_noRating_clearsAnyOldRating() = runTest {
        openFinishFormFor(rating = 2.0)
        val updates = captureBookUpdate()
        viewModel.onFinishRatingChanged(2) // un-chosen

        viewModel.onConfirmFinish()

        assertTrue(updates.captured.containsKey(StoredBook.RATING))
        assertNull(updates.captured[StoredBook.RATING])
    }

    @Test
    fun finishForm_noteUnchanged_isNotRewritten() = runTest {
        openFinishFormFor(notes = "Loved Anne.")
        val updates = captureBookUpdate()

        viewModel.onConfirmFinish()

        assertFalse(updates.captured.containsKey(StoredBook.NOTES))
    }

    @Test
    fun finishForm_noteCleared_removesTheOldNote() = runTest {
        openFinishFormFor(notes = "Loved Anne.")
        val updates = captureBookUpdate()
        viewModel.onFinishNoteChanged("")

        viewModel.onConfirmFinish()

        assertEquals("", updates.captured[StoredBook.NOTES])
    }

    @Test
    fun finishForm_noteTooLong_isCutToTheLimit() = runTest {
        openFinishFormFor()

        viewModel.onFinishNoteChanged("a".repeat(TextLimits.BOOK_NOTES + 10))

        assertEquals(TextLimits.BOOK_NOTES, form.noteInput.length)
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
    fun onRemoveSession_offline_removesAndSaysItWillSync() = runTest {
        viewModel.onLoad()
        coEvery { fireRepository.deleteSession(any()) } returns WriteResult.SAVED_OFFLINE
        givenSessions(emptyList())

        viewModel.onRemoveSession(TestData.session)
        dialog().onPrimaryClick!!()

        assertTrue(state.sessions.isEmpty())
        assertEquals(str(R.string.removed_offline_toast), mainViewModel.toastFlow.value)
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
        coEvery { fireRepository.deleteSession(any()) } throws IllegalStateException("rejected")

        viewModel.onRemoveSession(TestData.session)
        dialog().onPrimaryClick!!()

        assertEquals(listOf(TestData.session), state.sessions)
        assertEquals(str(R.string.session_error), mainViewModel.toastFlow.value)
    }

    // endregion
}
