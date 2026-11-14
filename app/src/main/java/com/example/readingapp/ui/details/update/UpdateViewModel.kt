package com.example.readingapp.ui.details.update

import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.readingapp.R
import com.example.readingapp.common.BaseViewModel
import com.example.readingapp.common.DependencyContextWrapper
import com.example.readingapp.common.LoadingState
import com.example.readingapp.repo.FireRepository
import com.example.readingapp.ui.components.BookStatus
import com.example.readingapp.ui.destinations.DetailsScreenDestination
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class UpdateViewModel @Inject constructor(
    private val fireRepository: FireRepository,
    savedStateHandle: SavedStateHandle,
    dependencyContextWrapper: DependencyContextWrapper
) : BaseViewModel(dependencyContextWrapper), DefaultLifecycleObserver {
    override val isNavigationDestination: Boolean = true
    private val args = DetailsScreenDestination.argsFrom(savedStateHandle)

    private val _uiState: MutableStateFlow<UpdateUiState> = MutableStateFlow(UpdateUiState())
    val uiState: StateFlow<UpdateUiState>
        get() = _uiState

    fun loadBook() = viewModelScope.launch(Dispatchers.IO) {
        try {
            updateLoadingState(LoadingState.Loading())
            val allSavedBooks = fireRepository.fetchSavedBooksByUser().getOrThrow()
            val savedBook = allSavedBooks.find { it.googleBookId == args.bookId }
            val parts = savedBook.finishedParts()
            withContext(Dispatchers.Main) {
                _uiState.update { state ->
                    state.copy(
                        bookId = savedBook?.id,
                        book = savedBook,
                        isSaved = savedBook != null,
                        noteInput = savedBook?.notes ?: "",
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
        _uiState.update { state ->
            state.copy(noteInput = input)
        }
    }

    fun onStatusChanged(status: BookStatus) {
        _uiState.update { state ->
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
        _uiState.update { it.copy(finishedYear = year, finishedOnError = false) }
    }

    fun onFinishedMonthChanged(month: Int?) {
        _uiState.update { state ->
            state.copy(
                finishedMonth = month,
                finishedDay = if (month == null) "" else state.finishedDay,
                finishedOnError = false
            )
        }
    }

    fun onFinishedDayChanged(input: String) {
        _uiState.update { state ->
            state.copy(
                finishedDay = input.filter(Char::isDigit).take(2),
                finishedOnError = false
            )
        }
    }

    fun onRatingChanged(rating: Int) {
        _uiState.update { state ->
            state.copy(selectedRating = rating)
        }
    }

    fun onSaveClick() {
        uiState.value.book?.let { book ->
            var updates = emptyMap<String, Any?>()

            // Notes
            if (book.notes != uiState.value.noteInput) {
                updates = updates.plus("notes" to uiState.value.noteInput)
            }

            val storedRating = uiState.value.selectedRating.takeIf { it in 1..5 }?.toDouble()
            if (storedRating != book.rating) {
                updates = updates.plus("rating" to storedRating)
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
                _uiState.update { it.copy(finishedOnError = true) }
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

            if (updates.isNotEmpty()) {
                updateLoadingState(LoadingState.Loading(LoadingState.LoadingType.FULL_SCREEN))
                try {
                    fireRepository.updateBook(
                        bookId = book.id.toString(),
                        bookUpdates = updates,
                        onCompleteListener = { successful ->
                            if (successful) {
                                viewModelScope.launch {
                                    delay(3000.milliseconds)
                                    loadBook()
                                    showToast(getString(R.string.update_book_success))
                                    navigateBack()
                                }
                            } else {
                                updateLoadingState(LoadingState.Idle)
                                showToast(getString(R.string.update_book_error))
                            }
                        },
                        onErrorListener = { e ->
                            Log.e("Error", "onSaveClick: Error updating book", e)
                            updateLoadingState(LoadingState.Idle)
                            showToast(getString(R.string.update_book_error))
                        }
                    )
                } catch (e: Exception) {
                    Log.e("Error", "onSaveClick: Error updating book", e)
                    updateLoadingState(LoadingState.Idle)
                    showToast(getString(R.string.update_book_error))
                }
            } else {
                showToast(getString(R.string.update_no_changes_made))
            }
        }
    }

    fun onCancelClick() {
        navigateBack()
    }
}