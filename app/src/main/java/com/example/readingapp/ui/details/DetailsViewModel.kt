package com.example.readingapp.ui.details

import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.readingapp.R
import com.example.readingapp.common.BaseViewModel
import com.example.readingapp.common.DependencyContextWrapper
import com.example.readingapp.common.LoadingState
import com.example.readingapp.data.RemoteResult
import com.example.readingapp.model.MBook
import com.example.readingapp.nav.NavigateTo
import com.example.readingapp.repo.BookRepository
import com.example.readingapp.repo.FireRepository
import com.example.readingapp.repo.FireRepository.Companion.BOOKS_COLLECTION
import com.example.readingapp.ui.components.BookStatus
import com.example.readingapp.ui.components.DialogChoice
import com.example.readingapp.ui.components.DialogFinishedDate
import com.example.readingapp.ui.components.DialogState
import com.example.readingapp.ui.details.update.FinishedOn
import com.example.readingapp.ui.details.update.FinishedParts
import com.example.readingapp.ui.details.update.MIN_FINISHED_YEAR
import com.example.readingapp.ui.details.update.currentFinishedParts
import com.example.readingapp.ui.details.update.finishedParts
import com.example.readingapp.ui.details.update.maxFinishedYear
import com.example.readingapp.ui.details.update.toFinishedOn
import com.example.readingapp.ui.destinations.DetailsScreenDestination
import com.example.readingapp.ui.destinations.UpdateScreenDestination
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailsViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val fireRepository: FireRepository,
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
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                bookRepository.getBookInfo(args.bookId).collect { result ->
                    when (result) {
                        is RemoteResult.Loading -> updateLoadingState(LoadingState.Loading())
                        is RemoteResult.Success -> {
                            updateLoadingState(LoadingState.Success)
                            uiState.update { state ->
                                state.copy(book = result.data)
                            }
                        }
                        is RemoteResult.Error -> {
                            updateLoadingState(LoadingState.Failed(result.exception?.message))
                            showErrorDialog(result.exception)
                        }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                showErrorDialog(e)
            }
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
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        if (fireRepository.savedBooks.value.any { it.googleBookId == args.bookId }) return

        val db = FirebaseFirestore.getInstance()
        val (started, finished) = readingTimes(saved = null, status = status, finishedOn = finishedOn)
        val bookWithId = book.copy(
            userId = userId,
            googleBookId = book.googleBookId ?: args.bookId,
            startedReading = started,
            finishedReading = finished,
            finishedOn = finishedOn?.display,
            rating = null
        )
        val dbCollection = db.collection(BOOKS_COLLECTION)
        dbCollection.add(bookWithId)
            .addOnSuccessListener { documentRef ->
                dbCollection.document(documentRef.id)
                    .update(mapOf("id" to documentRef.id))
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            showToast(getString(R.string.book_saved_toast))
                            fetchSavedBooks()
                        }
                    }
                    .addOnFailureListener {
                        Log.e("Error", "saveBook: Error updating doc", it)
                    }
            }
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
            onCompleteListener = { successful ->
                if (successful) {
                    showToast(getString(R.string.update_book_success))
                    fetchSavedBooks()
                }
            },
            onErrorListener = {
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
        if (bookIds.isEmpty()) return
        val db = FirebaseFirestore.getInstance().collection(BOOKS_COLLECTION)
        var remaining = bookIds.size
        bookIds.distinct().forEach { bookId ->
            db.document(bookId)
                .delete()
                .addOnCompleteListener {
                    remaining -= 1
                    if (remaining == 0) {
                        showToast(getString(R.string.book_removed_toast))
                        fetchSavedBooks()
                    }
                }
                .addOnFailureListener {
                    Log.e("Error", "removeBook: Error deleting doc", it)
                }
        }
    }

    private fun fetchSavedBooks() = viewModelScope.launch {
        fireRepository.fetchSavedBooksByUser()
    }

    fun navToUpdateScreen() {
        navigate(NavigateTo(UpdateScreenDestination(args.bookId)))
    }
}