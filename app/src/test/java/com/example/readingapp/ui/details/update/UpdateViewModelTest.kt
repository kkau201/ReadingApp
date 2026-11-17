package com.example.readingapp.ui.details.update

import androidx.lifecycle.SavedStateHandle
import com.example.readingapp.R
import com.example.readingapp.common.DependencyContextWrapper
import com.example.readingapp.common.LoadingState
import com.example.readingapp.model.StoredBook
import com.example.readingapp.model.TextLimits
import com.example.readingapp.nav.NavigateBack
import com.example.readingapp.nav.NavigationEvent
import com.example.readingapp.testing.BaseViewModelTest
import com.example.readingapp.testing.FakeFirestore
import com.example.readingapp.testing.TestData
import com.example.readingapp.testing.TestData.datedFinishedBook
import com.example.readingapp.testing.TestData.readingBook
import com.example.readingapp.testing.TestData.unsavedBook
import com.example.readingapp.testing.fakeContext
import com.example.readingapp.testing.str
import com.example.readingapp.ui.model.BookStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Calendar
import kotlin.time.Duration.Companion.milliseconds

/** The Update book screen: notes, rating, reading status and finished date. */
class UpdateViewModelTest : BaseViewModelTest() {

    private val firestore = FakeFirestore()
    private lateinit var viewModel: UpdateViewModel

    private val state get() = viewModel.uiState.value
    private val thisYear = Calendar.getInstance().get(Calendar.YEAR)

    @Before
    fun setUp() {
        firestore.books = TestData.books + datedFinishedBook
    }

    private fun createViewModel(googleBookId: String) {
        viewModel = UpdateViewModel(
            firestore.repository,
            SavedStateHandle(mapOf("bookId" to googleBookId)),
            DependencyContextWrapper(fakeContext())
        )
        viewModel.setMainViewModel(mainViewModel)
    }

    /** Opens the screen and waits for the book to load (it loads off the main thread). */
    private suspend fun open(googleBookId: String) {
        createViewModel(googleBookId)
        viewModel.loadBook()
        mainViewModel.loadingFlow.first { it == LoadingState.Success || it is LoadingState.Failed }
    }

    /**
     * Saves, then waits out the app's 3s pause and the reload that follows, so nothing is still
     * running off the main thread when the test ends.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun TestScope.saveAndWait() {
        viewModel.onSaveClick()
        advanceTimeBy(3_001.milliseconds)
        mainViewModel.loadingFlow.first { it == LoadingState.Success }
    }

    private fun lastUpdate(): Map<String, Any?> = firestore.bookUpdates.single().second

    // region Loading

    @Test
    fun open_readingBook_fillsFormFromShelf() = runTest {
        open(readingBook.googleBookId!!)

        assertTrue(state.isSaved)
        assertEquals(readingBook.id, state.bookId)
        assertEquals(readingBook, state.book)
        assertEquals("", state.noteInput)
        assertEquals(BookStatus.READING, state.selectedStatus)
        assertEquals(0, state.selectedRating)
        assertFalse(state.finishedOnError)
    }

    @Test
    fun open_notFinishedYet_finishedDateDefaultsToThisYear() = runTest {
        open(readingBook.googleBookId!!)

        assertEquals(thisYear, state.finishedYear)
        assertNull(state.finishedMonth)
        assertEquals("", state.finishedDay)
    }

    @Test
    fun open_finishedBook_showsNotesRatingAndFinishedDate() = runTest {
        open(datedFinishedBook.googleBookId!!)

        assertEquals(datedFinishedBook.notes, state.noteInput)
        assertEquals(4, state.selectedRating)
        assertEquals(BookStatus.FINISHED, state.selectedStatus)
        assertEquals(2025, state.finishedYear)
        assertEquals(3, state.finishedMonth)
        assertEquals("5", state.finishedDay)
    }

    @Test
    fun open_notesSavedUnderOlderLongerLimit_areTrimmedToFit() = runTest {
        firestore.books = listOf(readingBook.copy(notes = "n".repeat(TextLimits.BOOK_NOTES + 50)))

        open(readingBook.googleBookId!!)

        assertEquals(TextLimits.BOOK_NOTES, state.noteInput.length)
    }

    @Test
    fun open_bookNotOnShelf_hasNothingToEdit() = runTest {
        open(unsavedBook.googleBookId!!)

        assertFalse(state.isSaved)
        assertNull(state.book)
        assertEquals(BookStatus.LIBRARY, state.selectedStatus)
    }

    @Test
    fun open_shelfReadFails_showsError() = runTest {
        firestore.booksError = FakeFirestore.unavailable()

        open(readingBook.googleBookId!!)
        // The dialog follows the failed state from the background thread.
        mainViewModel.dialogFlow.first { it != null }

        assertNull(state.book)
        assertEquals(LoadingState.Failed("Firestore unavailable"), mainViewModel.loadingFlow.value)
        assertErrorDialog(R.string.error_unknown)
    }

    // endregion

    // region Editing

    @Test
    fun notes_areLimited() {
        createViewModel(readingBook.googleBookId!!)

        viewModel.onNoteInputChanged("x".repeat(TextLimits.BOOK_NOTES + 5))

        assertEquals(TextLimits.BOOK_NOTES, state.noteInput.length)
    }

    @Test
    fun rating_canBeChanged() {
        createViewModel(readingBook.googleBookId!!)

        viewModel.onRatingChanged(3)

        assertEquals(3, state.selectedRating)
    }

    @Test
    fun choosingFinished_withNoYearYet_startsAtThisYear() {
        createViewModel(readingBook.googleBookId!!)

        viewModel.onStatusChanged(BookStatus.FINISHED)

        assertEquals(BookStatus.FINISHED, state.selectedStatus)
        assertEquals(thisYear, state.finishedYear)
    }

    @Test
    fun finishedYear_outsideAllowedRange_isIgnored() = runTest {
        open(datedFinishedBook.googleBookId!!)

        viewModel.onFinishedYearChanged(MIN_FINISHED_YEAR - 1)
        viewModel.onFinishedYearChanged(thisYear + 1)

        assertEquals(2025, state.finishedYear)
    }

    @Test
    fun finishedDay_keepsTwoDigitsAndClearingMonthClearsDay() = runTest {
        open(datedFinishedBook.googleBookId!!)

        viewModel.onFinishedDayChanged("1x23")
        assertEquals("12", state.finishedDay)

        viewModel.onFinishedMonthChanged(null)

        assertNull(state.finishedMonth)
        assertEquals("", state.finishedDay)
    }

    // endregion

    // region Saving

    @Test
    fun save_nothingChanged_saysSoAndWritesNothing() = runTest {
        open(datedFinishedBook.googleBookId!!)

        viewModel.onSaveClick()

        assertEquals(str(R.string.update_no_changes_made), mainViewModel.toastFlow.value)
        assertTrue(firestore.bookUpdates.isEmpty())
    }

    @Test
    fun save_notesAndRating_writesOnlyThoseThenConfirmsAndGoesBack() = runTest {
        collectNavigation()
        open(datedFinishedBook.googleBookId!!)
        viewModel.onNoteInputChanged("Better on a second read.")
        viewModel.onRatingChanged(5)

        saveAndWait()

        val (id, updates) = firestore.bookUpdates.single()
        assertEquals(datedFinishedBook.id, id)
        assertEquals(
            mapOf(StoredBook.NOTES to "Better on a second read.", StoredBook.RATING to 5.0),
            updates
        )
        assertEquals(str(R.string.update_book_success), mainViewModel.toastFlow.value)
        assertEquals(listOf<NavigationEvent?>(NavigateBack), navigationEvents)
    }

    @Test
    fun save_showsFullScreenLoaderUntilConfirmed() = runTest {
        open(datedFinishedBook.googleBookId!!)
        viewModel.onRatingChanged(5)

        viewModel.onSaveClick()

        assertEquals(
            LoadingState.Loading(LoadingState.LoadingType.FULL_SCREEN),
            mainViewModel.loadingFlow.value
        )
        assertNull(mainViewModel.toastFlow.value)

        advanceTimeBy(3_001.milliseconds)
        mainViewModel.loadingFlow.first { it == LoadingState.Success }
        assertEquals(str(R.string.update_book_success), mainViewModel.toastFlow.value)
    }

    @Test
    fun save_ratingCleared_removesIt() = runTest {
        open(datedFinishedBook.googleBookId!!)
        viewModel.onRatingChanged(0)

        saveAndWait()

        val updates = lastUpdate()
        assertTrue(StoredBook.RATING in updates)
        assertNull(updates[StoredBook.RATING])
    }

    @Test
    fun save_readingToFinished_keepsStartDateAndSavesFinishedDate() = runTest {
        open(readingBook.googleBookId!!)
        viewModel.onStatusChanged(BookStatus.FINISHED)
        viewModel.onFinishedYearChanged(2025)
        viewModel.onFinishedMonthChanged(6)
        viewModel.onFinishedDayChanged("14")

        saveAndWait()

        val updates = lastUpdate()
        assertFalse(StoredBook.STARTED_READING in updates)
        assertNotNull(updates[StoredBook.FINISHED_READING])
        assertEquals("14/06/2025", updates[StoredBook.FINISHED_ON])
        assertEquals(BookStatus.FINISHED, firestore.books.single { it.id == readingBook.id }.bookStatus)
    }

    @Test
    fun save_finishedWithYearOnly_isAllowed() = runTest {
        open(readingBook.googleBookId!!)
        viewModel.onStatusChanged(BookStatus.FINISHED)
        viewModel.onFinishedYearChanged(2024)

        saveAndWait()

        assertEquals("2024", lastUpdate()[StoredBook.FINISHED_ON])
    }

    @Test
    fun save_finishedOnDayThatDoesNotExist_flagsDateAndWritesNothing() = runTest {
        open(readingBook.googleBookId!!)
        viewModel.onStatusChanged(BookStatus.FINISHED)
        viewModel.onFinishedYearChanged(2023)
        viewModel.onFinishedMonthChanged(2)
        viewModel.onFinishedDayChanged("29")

        viewModel.onSaveClick()

        assertTrue(state.finishedOnError)
        assertTrue(firestore.bookUpdates.isEmpty())

        // Editing the date clears the error.
        viewModel.onFinishedDayChanged("28")
        assertFalse(state.finishedOnError)
    }

    @Test
    fun save_finishedToWantToRead_clearsAllDates() = runTest {
        open(datedFinishedBook.googleBookId!!)
        viewModel.onStatusChanged(BookStatus.LIBRARY)

        saveAndWait()

        val updates = lastUpdate()
        assertNull(updates[StoredBook.STARTED_READING])
        assertNull(updates[StoredBook.FINISHED_READING])
        assertNull(updates[StoredBook.FINISHED_ON])
        assertTrue(listOf(StoredBook.STARTED_READING, StoredBook.FINISHED_READING, StoredBook.FINISHED_ON)
            .all { it in updates })
    }

    @Test
    fun save_wantToReadToReading_setsStartDate() = runTest {
        firestore.books = listOf(TestData.libraryBook.copy(notes = "Recommended by Anne"))
        open(TestData.libraryBook.googleBookId!!)
        viewModel.onStatusChanged(BookStatus.READING)

        saveAndWait()

        val updates = lastUpdate()
        assertNotNull(updates[StoredBook.STARTED_READING])
        assertFalse(StoredBook.FINISHED_READING in updates)
    }

    @Test
    fun save_fails_showsErrorAndStays() = runTest {
        collectNavigation()
        firestore.bookUpdateError = FakeFirestore.unavailable()
        open(datedFinishedBook.googleBookId!!)
        viewModel.onRatingChanged(5)

        viewModel.onSaveClick()
        advanceTimeBy(3_001.milliseconds)

        assertEquals(str(R.string.update_book_error), mainViewModel.toastFlow.value)
        assertEquals(LoadingState.Idle, mainViewModel.loadingFlow.value)
        assertTrue(navigationEvents.isEmpty())
        assertEquals(5, state.selectedRating)
    }

    @Test
    fun save_beforeBookLoaded_writesNothing() {
        createViewModel(readingBook.googleBookId!!)
        viewModel.onRatingChanged(5)

        viewModel.onSaveClick()

        assertTrue(firestore.bookUpdates.isEmpty())
    }

    // endregion

    @Test
    fun cancel_goesBack() = runTest {
        collectNavigation()
        createViewModel(readingBook.googleBookId!!)

        viewModel.onCancelClick()

        assertEquals(listOf<NavigationEvent?>(NavigateBack), navigationEvents)
    }
}