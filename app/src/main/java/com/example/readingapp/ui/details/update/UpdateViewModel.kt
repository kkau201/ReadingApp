package com.example.readingapp.ui.details.update

import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.readingapp.R
import com.example.readingapp.common.BaseViewModel
import com.example.readingapp.common.DependencyContextWrapper
import com.example.readingapp.common.LoadingState
import com.example.readingapp.coroutines.CoroutineDispatcherProvider
import com.example.readingapp.model.TextLimits
import com.example.readingapp.model.limitTo
import com.example.readingapp.repo.FireRepository
import com.example.readingapp.repo.WriteResult
import com.example.readingapp.ui.destinations.DetailsScreenDestination
import com.example.readingapp.ui.model.BookStatus
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class UpdateViewModel @Inject constructor(
    private val fireRepository: FireRepository,
    savedStateHandle: SavedStateHandle,
    private val dispatchers: CoroutineDispatcherProvider,
    dependencyContextWrapper: DependencyContextWrapper
) : BaseViewModel(dependencyContextWrapper), DefaultLifecycleObserver {
    override val isNavigationDestination: Boolean = true
    private val args = DetailsScreenDestination.argsFrom(savedStateHandle)

    val uiState: StateFlow<UpdateUiState>
        field: MutableStateFlow<UpdateUiState> = MutableStateFlow(UpdateUiState())

    /** True from sending a save until it succeeds or fails, so a second tap is ignored. */
    private var saving = false

    fun loadBook() = viewModelScope.launch(dispatchers.getIo()) {
        try {
            updateLoadingState(LoadingState.Loading())
            val allSavedBooks = fireRepository.fetchSavedBooksByUser().getOrThrow()
            val savedBook = allSavedBooks.find { it.googleBookId == args.bookId }
            val parts = savedBook.finishedParts()
            withContext(dispatchers.getMain()) {
                uiState.update { state ->
                    state.copy(
                        bookId = savedBook?.id,
                        book = savedBook,
                        isSaved = savedBook != null,
                        noteInput = (savedBook?.notes ?: ""),
                        selectedStatus = savedBook?.bookStatus ?: BookStatus.LIBRARY,
                        selectedRating = savedBook?.rating?.toInt() ?: 0,
                        finishedYear = parts.year,
                        finishedMonth = parts.month,
                        finishedDay = parts.day,
                        finishedOnError = false
                    )
                }
                updateLoadingState(LoadingState.Success)
            }
        } catch (e: Exception) {
            updateLoadingState(LoadingState.Failed(e.message))
            showErrorDialog(e)
        }
    }

    fun onNoteInputChanged(input: String) {
        uiState.update { state ->
            state.copy(noteInput = input.limitTo(TextLimits.BOOK_NOTES))
        }
    }

    fun onStatusChanged(status: BookStatus) {
        uiState.update { state ->
            val year = if (status == BookStatus.FINISHED && state.finishedYear == 0) {
                maxFinishedYear()
            } else {
                state.finishedYear
            }
            state.copy(selectedStatus = status, finishedYear = year, finishedOnError = false)
        }
    }

    fun onFinishedYearChanged(year: Int) {
        if (year !in MIN_FINISHED_YEAR..maxFinishedYear()) return
        uiState.update { it.copy(finishedYear = year, finishedOnError = false) }
    }

    fun onFinishedMonthChanged(month: Int?) {
        uiState.update { state ->
            state.copy(
                finishedMonth = month,
                finishedDay = if (month == null) "" else state.finishedDay,
                finishedOnError = false
            )
        }
    }

    fun onFinishedDayChanged(input: String) {
        uiState.update { state ->
            state.copy(
                finishedDay = input.filter(Char::isDigit).take(2),
                finishedOnError = false
            )
        }
    }

    fun onRatingChanged(rating: Int) {
        uiState.update { state ->
            state.copy(selectedRating = rating)
        }
    }

    fun onSaveClick() {
        if (saving) return
        uiState.value.book?.let { book ->
            var updates = emptyMap<String, Any?>()

            // Notes: no note is stored as missing, but shown as an empty field.
            if ((book.notes ?: "") != uiState.value.noteInput) {
                updates = updates.plus("notes" to uiState.value.noteInput)
            }

            val selectedStatus: BookStatus = uiState.value.selectedStatus
            val finished = if (selectedStatus == BookStatus.FINISHED) {
                FinishedParts(
                    year = uiState.value.finishedYear,
                    month = uiState.value.finishedMonth,
                    day = uiState.value.finishedDay
                ).toFinishedOn()
            } else {
                null
            }
            if (selectedStatus == BookStatus.FINISHED && finished == null) {
                uiState.update { it.copy(finishedOnError = true) }
                return@let
            }
            val finishedTimeStamp = finished?.toTimestamp()
            val startedTimeStamp: Timestamp? = when {
                selectedStatus == BookStatus.LIBRARY -> null
                book.startedReading != null -> book.startedReading
                else -> finishedTimeStamp ?: Timestamp.now()
            }

            if (startedTimeStamp != book.startedReading) {
                updates = updates.plus("started_reading" to startedTimeStamp)
            }
            if (!sameLocalDay(finishedTimeStamp, book.finishedReading)) {
                updates = updates.plus("finished_reading" to finishedTimeStamp)
            }
            if (finished?.display != book.finishedOn) {
                updates = updates.plus("finished_on" to finished?.display)
            }

            // If book is not finished then ensure the rating is removed (so its reset if prev. set)
            val storedRating = if (selectedStatus != BookStatus.FINISHED) null
            else uiState.value.selectedRating.takeIf { it in 1..5 }?.toDouble()
            if (storedRating != book.rating) {
                updates = updates.plus("rating" to storedRating)
            }

            if (updates.isNotEmpty()) {
                saving = true
                updateLoadingState(LoadingState.Loading(LoadingState.LoadingType.FULL_SCREEN))
                try {
                    fireRepository.updateBook(
                        bookId = book.id.toString(),
                        bookUpdates = updates,
                        onSaved = ::onSaved,
                        onError = { e ->
                            Log.e("Error", "onSaveClick: Error updating book", e)
                            onSaveFailed()
                        }
                    )
                } catch (e: Exception) {
                    Log.e("Error", "onSaveClick: Error updating book", e)
                    onSaveFailed()
                }
            } else {
                showToast(getString(R.string.update_no_changes_made))
            }
        }
    }

    private fun onSaved(result: WriteResult) {
        viewModelScope.launch {
            // Refresh the shared shelf so Details (underneath) shows the change straight away.
            // The save already succeeded, so a failed refresh doesn't change the outcome.
            try {
                fireRepository.fetchSavedBooksByUser()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("Error", "onSaved: Error refreshing books", e)
            }
            saving = false
            updateLoadingState(LoadingState.Idle)
            showWriteToast(result, savedRes = R.string.update_book_success)
            navigateBack()
        }
    }

    private fun onSaveFailed() {
        saving = false
        updateLoadingState(LoadingState.Idle)
        showToast(getString(R.string.update_book_error))
    }

    fun onCancelClick() {
        navigateBack()
    }
}