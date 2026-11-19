package com.kaylakautai.inkle.ui.details

import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.kaylakautai.inkle.R
import com.kaylakautai.inkle.common.BaseViewModel
import com.kaylakautai.inkle.common.DependencyContextWrapper
import com.kaylakautai.inkle.common.LoadingState
import com.kaylakautai.inkle.data.RemoteResult
import com.kaylakautai.inkle.model.MBook
import com.kaylakautai.inkle.model.toStoredDocument
import com.kaylakautai.inkle.nav.NavigateTo
import com.kaylakautai.inkle.network.ConnectivityObserver
import com.kaylakautai.inkle.repo.BookRepository
import com.kaylakautai.inkle.auth.AuthRepository
import com.kaylakautai.inkle.repo.FireRepository
import com.kaylakautai.inkle.repo.FireRepository.Companion.BOOKS_COLLECTION
import com.kaylakautai.inkle.repo.WriteResult
import com.kaylakautai.inkle.repo.onWriteDone
import com.kaylakautai.inkle.ui.model.BookStatus
import com.kaylakautai.inkle.ui.components.DialogChoice
import com.kaylakautai.inkle.ui.components.DialogFinishedDate
import com.kaylakautai.inkle.ui.components.DialogState
import com.kaylakautai.inkle.ui.components.shelfKey
import com.kaylakautai.inkle.ui.stamps.StampCelebrations
import com.kaylakautai.inkle.ui.details.update.FinishedOn
import com.kaylakautai.inkle.ui.details.update.FinishedParts
import com.kaylakautai.inkle.ui.details.update.MIN_FINISHED_YEAR
import com.kaylakautai.inkle.ui.details.update.currentFinishedParts
import com.kaylakautai.inkle.ui.details.update.finishedParts
import com.kaylakautai.inkle.ui.details.update.maxFinishedYear
import com.kaylakautai.inkle.ui.details.update.toFinishedOn
import com.kaylakautai.inkle.ui.destinations.DetailsScreenDestination
import com.kaylakautai.inkle.ui.destinations.UpdateScreenDestination
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailsViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val fireRepository: FireRepository,
    private val authRepository: AuthRepository,
    private val connectivity: ConnectivityObserver,
    private val celebrations: StampCelebrations,
    savedStateHandle: SavedStateHandle,
    dependencyContextWrapper: DependencyContextWrapper
) : BaseViewModel(dependencyContextWrapper), DefaultLifecycleObserver {
    override val isNavigationDestination: Boolean = true
    private val args = DetailsScreenDestination.argsFrom(savedStateHandle)

    val uiState: StateFlow<DetailsUiState>
        field: MutableStateFlow<DetailsUiState> = MutableStateFlow(DetailsUiState())

    private var isWatchingSavedBooks = false
    private var shelfYear: Int = 0
    private var shelfMonth: Int? = null
    private var shelfDay: String = ""
    private var shelfDayError: Boolean = false
    private var shelfPickingFinished: Boolean = false

    fun loadBook() {
        if (!isWatchingSavedBooks) {
            isWatchingSavedBooks = true
            viewModelScope.launch {
                fireRepository.fetchSavedBooksByUser()
                fireRepository.savedBooks.collect { allSavedBooks ->
                    val savedBook = allSavedBooks.find { it.googleBookId == args.bookId }
                    uiState.update { state ->
                        state.copy(
                            bookId = savedBook?.id,
                            savedBook = savedBook,
                            isSaved = savedBook != null
                        )
                    }
                }
            }
            // Showing the saved copy (offline)? Load the full book as soon as we're back online,
            // as the callout promises.
            viewModelScope.launch {
                connectivity.isOnline
                    .drop(1) // the current state, not a change
                    .filter { online -> online }
                    .collect { if (uiState.value.showingShelfCopy) loadBook() }
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                bookRepository.getBookInfo(args.bookId).collect { result ->
                    when (result) {
                        is RemoteResult.Loading -> updateLoadingState(LoadingState.Loading())
                        is RemoteResult.Success -> {
                            updateLoadingState(LoadingState.Success)
                            uiState.update { state ->
                                state.copy(book = result.data, showingShelfCopy = false)
                            }
                        }
                        is RemoteResult.Error -> showWhatWeHaveOrError(result.exception)
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                showWhatWeHaveOrError(e)
            }
        }
    }

    /**
     * Google Books couldn't be reached (usually offline). Rather than a dead end, keep showing
     * the book if it already loaded, or fall back to the copy on the user's shelf. Only a book
     * that's neither shows the error.
     */
    private suspend fun showWhatWeHaveOrError(error: Throwable?) {
        if (uiState.value.book != null) {
            updateLoadingState(LoadingState.Success)
            return
        }
        // Read from the device cache when offline, so this is quick.
        val shelfCopy = try {
            fireRepository.fetchSavedBooksByUser().data?.find { it.googleBookId == args.bookId }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.d("DetailsViewModel", "No shelf copy to fall back to: $e")
            null
        }
        if (shelfCopy != null) {
            uiState.update { it.copy(book = shelfCopy, showingShelfCopy = true) }
            updateLoadingState(LoadingState.Success)
        } else {
            updateLoadingState(LoadingState.Failed(error?.message))
            showErrorDialog(error)
        }
    }

    fun toggleSavedBook() = viewModelScope.launch {
        val savedCopies = fireRepository.fetchSavedBooksByUser().data
            ?.filter { it.googleBookId == args.bookId && !it.id.isNullOrBlank() }
            .orEmpty()
        if (savedCopies.isNotEmpty()) {
            showDialog(
                DialogState(
                    title = getString(R.string.remove_book_title),
                    message = getString(R.string.remove_book_message),
                    primaryButtonText = getString(R.string.remove_book_primary_button),
                    secondaryButtonText = getString(R.string.remove_book_secondary_button),
                    onPrimaryClick = {
                        dismissDialog()
                        removeBooks(savedCopies.mapNotNull { it.id })
                    },
                    onSecondaryClick = { dismissDialog() }
                )
            )
        } else {
            showStatusDialog()
        }
    }

    fun onChooseStatus() {
        if (uiState.value.isSaved) showStatusDialog()
    }

    private fun showStatusDialog() {
        val saved = uiState.value.savedBook
        val alreadyFinished = saved?.bookStatus == BookStatus.FINISHED
        val parts = saved?.takeIf { it.bookStatus == BookStatus.FINISHED }?.finishedParts()
            ?: currentFinishedParts()
        shelfYear = parts.year
        shelfMonth = if (alreadyFinished) parts.month else null
        shelfDay = if (alreadyFinished) parts.day else ""
        shelfDayError = false
        shelfPickingFinished = alreadyFinished
        publishStatusDialog()
    }

    private fun publishStatusDialog() {
        val saved = uiState.value.savedBook
        val selected = if (shelfPickingFinished) BookStatus.FINISHED else saved?.bookStatus
        showDialog(
            DialogState(
                title = getString(
                    if (saved == null) R.string.details_add_shelf else R.string.details_update_status
                ),
                choices = listOf(
                    statusChoice(BookStatus.LIBRARY, R.string.home_filter_want_to_read, selected),
                    statusChoice(BookStatus.READING, R.string.home_filter_reading, selected),
                    statusChoice(BookStatus.FINISHED, R.string.home_filter_finished, selected)
                ),
                finishedDate = if (shelfPickingFinished) shelfFinishedDate() else null,
                primaryButtonText = if (shelfPickingFinished) getString(R.string.update_save) else null,
                onPrimaryClick = { confirmFinished() },
                secondaryButtonText = getString(R.string.update_cancel),
                onSecondaryClick = { dismissDialog() },
                onDismissRequest = { dismissDialog() }
            )
        )
    }

    private fun shelfFinishedDate(): DialogFinishedDate {
        return DialogFinishedDate(
            year = shelfYear,
            month = shelfMonth,
            day = shelfDay,
            dayError = shelfDayError,
            onYearChanged = ::onShelfYearChanged,
            onMonthChanged = ::onShelfMonthChanged,
            onDayChanged = ::onShelfDayChanged
        )
    }

    private fun statusChoice(status: BookStatus, label: Int, current: BookStatus?): DialogChoice {
        return DialogChoice(
            text = getString(label),
            selected = status == current,
            onClick = {
                if (status == BookStatus.FINISHED) {
                    selectFinished()
                } else {
                    applyStatus(status)
                }
            }
        )
    }

    private fun selectFinished() {
        if (shelfYear == 0) shelfYear = maxFinishedYear()
        shelfPickingFinished = true
        shelfDayError = false
        publishStatusDialog()
    }

    private fun onShelfYearChanged(year: Int) {
        shelfYear = year.coerceIn(MIN_FINISHED_YEAR, maxFinishedYear())
        shelfDayError = false
        publishStatusDialog()
    }

    private fun onShelfMonthChanged(month: Int?) {
        shelfMonth = month
        if (month == null) shelfDay = ""
        shelfDayError = false
        publishStatusDialog()
    }

    private fun onShelfDayChanged(input: String) {
        shelfDay = input.filter(Char::isDigit).take(2)
        shelfDayError = false
        publishStatusDialog()
    }

    private fun confirmFinished() {
        val finished = FinishedParts(
            year = shelfYear,
            month = shelfMonth,
            day = shelfDay
        ).toFinishedOn()
        if (finished == null) {
            shelfDayError = true
            publishStatusDialog()
            return
        }
        dismissDialog()
        val saved = uiState.value.savedBook
        if (saved == null) {
            saveBook(BookStatus.FINISHED, finished)
        } else {
            updateStatus(saved, BookStatus.FINISHED, finished)
        }
    }

    private fun applyStatus(status: BookStatus) {
        dismissDialog()
        val saved = uiState.value.savedBook
        if (saved == null) {
            saveBook(status)
        } else if (saved.bookStatus != status) {
            updateStatus(saved, status)
        }
    }

    private fun saveBook(status: BookStatus, finishedOn: FinishedOn? = null) {
        val book = uiState.value.book ?: return
        val userId = authRepository.currentUserId ?: return
        if (fireRepository.savedBooks.value.any { it.googleBookId == args.bookId }) return

        val db = FirebaseFirestore.getInstance()
        val (started, finished) = readingTimes(saved = null, status = status, finishedOn = finishedOn)
        val storedBook = book.copy(
            userId = userId,
            googleBookId = book.googleBookId ?: args.bookId,
            startedReading = started,
            finishedReading = finished,
            finishedOn = finishedOn?.display,
            rating = null,
            notes = null
        )
        db.collection(BOOKS_COLLECTION)
            .add(storedBook.toStoredDocument())
            .onWriteDone(
                online = fireRepository.isOnline(),
                onSaved = { result ->
                    showWriteToast(result, savedRes = R.string.book_saved_toast)
                    if (status == BookStatus.FINISHED) {
                        celebrations.bookFinished(storedBook.shelfKey(), finished)
                    }
                    fetchSavedBooks()
                },
                onError = {
                    Log.e("Error", "saveBook: Error saving book", it)
                    showToast(getString(R.string.book_save_error))
                }
            )
    }

    private fun updateStatus(saved: MBook, status: BookStatus, finishedOn: FinishedOn? = null) {
        val bookId = saved.id ?: return
        val (started, finished) = readingTimes(saved, status, finishedOn)
        fireRepository.updateBook(
            bookId = bookId,
            bookUpdates = mapOf(
                "started_reading" to started,
                "finished_reading" to finished,
                "finished_on" to finishedOn?.display
            ),
            onSaved = { result ->
                showWriteToast(result, savedRes = R.string.update_book_success)
                if (status == BookStatus.FINISHED && saved.bookStatus != BookStatus.FINISHED) {
                    celebrations.bookFinished(saved.shelfKey(), finished)
                }
                fetchSavedBooks()
            },
            onError = {
                Log.e("Error", "updateStatus: Error updating book", it)
                showToast(getString(R.string.update_book_error))
            }
        )
    }

    private fun readingTimes(
        saved: MBook?,
        status: BookStatus,
        finishedOn: FinishedOn? = null
    ): Pair<Timestamp?, Timestamp?> {
        val now = Timestamp.now()
        val finishedTime = finishedOn?.toTimestamp()
        val started = when {
            status == BookStatus.LIBRARY -> null
            saved?.startedReading != null && status != BookStatus.LIBRARY -> saved.startedReading
            else -> finishedTime ?: now
        }
        val finished = if (status == BookStatus.FINISHED) {
            finishedTime ?: saved?.finishedReading ?: now
        } else {
            null
        }
        return started to finished
    }

    private fun removeBooks(bookIds: List<String>) {
        val ids = bookIds.distinct()
        if (ids.isEmpty()) return
        val db = FirebaseFirestore.getInstance().collection(BOOKS_COLLECTION)
        val online = fireRepository.isOnline()
        var remaining = ids.size
        var anyFailed = false
        var anyOffline = false
        fun onOneDone() {
            remaining -= 1
            if (remaining > 0) return
            when {
                anyFailed -> showToast(getString(R.string.book_remove_error))
                anyOffline -> showToast(getString(R.string.removed_offline_toast))
                else -> showToast(getString(R.string.book_removed_toast))
            }
            // Refresh either way: some copies may have been removed.
            fetchSavedBooks()
        }
        ids.forEach { bookId ->
            db.document(bookId)
                .delete()
                .onWriteDone(
                    online = online,
                    onSaved = { result ->
                        if (result == WriteResult.SAVED_OFFLINE) anyOffline = true
                        onOneDone()
                    },
                    onError = {
                        anyFailed = true
                        Log.e("Error", "removeBook: Error deleting doc", it)
                        onOneDone()
                    }
                )
        }
    }

    private fun fetchSavedBooks() = viewModelScope.launch {
        fireRepository.fetchSavedBooksByUser()
    }

    fun navToUpdateScreen() {
        navigate(NavigateTo(UpdateScreenDestination(args.bookId)))
    }
}