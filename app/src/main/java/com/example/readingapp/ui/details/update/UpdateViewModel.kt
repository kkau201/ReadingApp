package com.example.readingapp.ui.details.update

import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.readingapp.R
import com.example.readingapp.common.BaseViewModel
import com.example.readingapp.common.DependencyContextWrapper
import com.example.readingapp.common.ErrorType
import com.example.readingapp.common.LoadingState
import com.example.readingapp.repo.FireRepository
import com.example.readingapp.ui.components.BookStatus
import com.example.readingapp.ui.components.DialogState
import com.example.readingapp.ui.destinations.DetailsScreenDestination
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

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

    fun loadBook() {
        viewModelScope.launch(Dispatchers.IO) {
            fireRepository.fetchSavedBooksByUser()
            fireRepository.savedBooks.collect { allSavedBooks ->
                val savedBook = allSavedBooks.find { it.googleBookId == args.bookId }
                _uiState.update { state ->
                    state.copy(
                        loadingState = savedBook?.let { LoadingState.SUCCESS } ?: LoadingState.FAILED,
                        bookId = savedBook?.id,
                        book = savedBook,
                        isSaved = savedBook != null,
                        noteInput = savedBook?.notes ?: "",
                        selectedStatus = savedBook?.bookStatus ?: BookStatus.LIBRARY,
                        selectedRating = savedBook?.rating?.toInt() ?: 0
                    )
                }
            }
        }
    }

    fun onNoteInputChanged(input: String) {
        _uiState.update { state ->
            state.copy(noteInput = input)
        }
    }

    fun onStatusChanged(status: BookStatus) {
        _uiState.update { state ->
            state.copy(selectedStatus = status)
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

            // Rating
            if (uiState.value.selectedRating.toDouble() != book.rating) {
                updates = updates.plus("rating" to uiState.value.selectedRating.toDouble())
            }

            // Start and Finished timestamps
            val selectedStatus: BookStatus = uiState.value.selectedStatus

            // Check if need to reset timestamps due to user reverting the status to a previous stage
            val resetStartedReading = book.startedReading != null && selectedStatus == BookStatus.LIBRARY
            val resetFinishedReading = book.finishedReading != null && selectedStatus != BookStatus.FINISHED
            // Check if user has progressed to next stage which requires a timestamp of now
            val hasStartedReading = book.startedReading == null && uiState.value.selectedStatus == BookStatus.READING
            val hasFinishedReading = book.finishedReading == null && uiState.value.selectedStatus == BookStatus.FINISHED

            val startedTimeStamp: Timestamp? =
                if (resetStartedReading) null
                else if (hasStartedReading || (hasFinishedReading && book.startedReading == null)) Timestamp.now()
                else book.startedReading
            val finishedTimeStamp: Timestamp? =
                if (resetFinishedReading) null
                else if (hasFinishedReading) Timestamp.now()
                else book.finishedReading

            if (startedTimeStamp != book.startedReading) {
                updates = updates.plus("started_reading" to startedTimeStamp)
            }
            if (finishedTimeStamp != book.finishedReading) {
                updates = updates.plus("finished_reading" to finishedTimeStamp)
            }

            if (updates.isNotEmpty()) {
                viewModelScope.launch {
                    try {
                        fireRepository.updateBook(
                            bookId = book.id.toString(),
                            bookUpdates = updates,
                            onCompleteListener = { successful ->
                                if (successful) {
                                    showToast(getString(R.string.update_book_success))
                                    loadBook()
                                }
                            },
                            onErrorListener = { e ->
                                Log.e("Error", "onSaveClick: Error updating book", e)
                                showToast("Error updating book")
                            }
                        )
                    } catch (e: Exception) {
                        Log.e("Error", "onSaveClick: Error updating book", e)
                        showToast(getString(R.string.update_book_error))
                    }
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