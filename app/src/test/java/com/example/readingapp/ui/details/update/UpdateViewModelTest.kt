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
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Calendar

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
            dispatcherProvider,
            DependencyContextWrapper(fakeContext())
        )
        viewModel.setMainViewModel(mainViewModel)
    }

    private fun open(googleBookId: String) {
        createViewModel(googleBookId)
        viewModel.loadBook()
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
    fun save_bookWithNoNotes_unchanged_saysNoChanges() = runTest {
        // No note is stored as missing; the empty field mustn't count as a change.
        open(readingBook.googleBookId!!)

        viewModel.onSaveClick()

        assertEquals(str(R.string.update_no_changes_made), mainViewModel.toastFlow.value)
        assertTrue(firestore.bookUpdates.isEmpty())
    }

    @Test
    fun save_bookWithNoNotes_addingOne_savesIt() = runTest {
        open(readingBook.googleBookId!!)
        viewModel.onNoteInputChanged("Captain Wentworth!")

        viewModel.onSaveClick()

        assertEquals(mapOf(StoredBook.NOTES to "Captain Wentworth!"), lastUpdate())
    }

    @Test
    fun save_notesAndRating_writesOnlyThoseThenConfirmsAndGoesBack() = runTest {
        collectNavigation()
        open(datedFinishedBook.googleBookId!!)
        viewModel.onNoteInputChanged("Better on a second read.")
        viewModel.onRatingChanged(5)

        viewModel.onSaveClick()

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
    fun save_inProgress_showsFullScreenLoaderAndIgnoresSecondTap() = runTest {
        collectNavigation()
        firestore.holdBookUpdates = true
        open(datedFinishedBook.googleBookId!!)
        viewModel.onRatingChanged(5)

        viewModel.onSaveClick()
        viewModel.onSaveClick()

        assertEquals(1, firestore.bookUpdates.size)
        assertEquals(
            LoadingState.Loading(LoadingState.LoadingType.FULL_SCREEN),
            mainViewModel.loadingFlow.value
        )
        assertNull(mainViewModel.toastFlow.value)
        assertTrue(navigationEvents.isEmpty())

        firestore.finishHeldBookUpdates()

        // No fixed wait: done as soon as Firestore confirms.
        assertEquals(str(R.string.update_book_success), mainViewModel.toastFlow.value)
        assertEquals(LoadingState.Idle, mainViewModel.loadingFlow.value)
        assertEquals(listOf<NavigationEvent?>(NavigateBack), navigationEvents)
    }

    @Test
    fun save_refreshesShelfSoDetailsShowsTheChange() = runTest {
        open(readingBook.googleBookId!!)
        viewModel.onStatusChanged(BookStatus.FINISHED)

        viewModel.onSaveClick()

        val shelfCopy = firestore.repository.savedBooks.value.single { it.id == readingBook.id }
        assertEquals(BookStatus.FINISHED, shelfCopy.bookStatus)
    }

    @Test
    fun save_ratingCleared_removesIt() = runTest {
        open(datedFinishedBook.googleBookId!!)
        viewModel.onRatingChanged(0)

        viewModel.onSaveClick()

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

        viewModel.onSaveClick()

        val updates = lastUpdate()
        assertFalse(StoredBook.STARTED_READING in updates)
        assertNotNull(updates[StoredBook.FINISHED_READING])
        assertEquals("14/06/2025", updates[StoredBook.FINISHED_ON])
        assertEquals(
            BookStatus.FINISHED,
            firestore.books.single { it.id == readingBook.id }.bookStatus
        )
    }

    @Test
    fun save_finishedWithYearOnly_isAllowed() = runTest {
        open(readingBook.googleBookId!!)
        viewModel.onStatusChanged(BookStatus.FINISHED)
        viewModel.onFinishedYearChanged(2024)

        viewModel.onSaveClick()

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

        viewModel.onSaveClick()

        val updates = lastUpdate()
        assertNull(updates[StoredBook.STARTED_READING])
        assertNull(updates[StoredBook.FINISHED_READING])
        assertNull(updates[StoredBook.FINISHED_ON])
        assertTrue(
            listOf(StoredBook.STARTED_READING, StoredBook.FINISHED_READING, StoredBook.FINISHED_ON)
                .all { it in updates })
    }

    @Test
    fun save_wantToReadToReading_setsStartDate() = runTest {
        firestore.books = listOf(TestData.libraryBook.copy(notes = "Recommended by Anne"))
        open(TestData.libraryBook.googleBookId!!)
        viewModel.onStatusChanged(BookStatus.READING)

        viewModel.onSaveClick()

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

        assertEquals(str(R.string.update_book_error), mainViewModel.toastFlow.value)
        assertEquals(LoadingState.Idle, mainViewModel.loadingFlow.value)
        assertTrue(navigationEvents.isEmpty())
        assertEquals(5, state.selectedRating)
    }

    @Test
    fun save_failsThenRetried_saves() = runTest {
        collectNavigation()
        firestore.bookUpdateError = FakeFirestore.unavailable()
        open(datedFinishedBook.googleBookId!!)
        viewModel.onRatingChanged(5)
        viewModel.onSaveClick()

        firestore.bookUpdateError = null
        viewModel.onSaveClick()

        assertEquals(2, firestore.bookUpdates.size)
        assertEquals(str(R.string.update_book_success), mainViewModel.toastFlow.value)
        assertEquals(listOf<NavigationEvent?>(NavigateBack), navigationEvents)
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