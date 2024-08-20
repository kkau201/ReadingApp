package com.example.readingapp.ui.details.update

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.readingapp.common.BaseViewModel
import com.example.readingapp.common.DependencyContextWrapper
import com.example.readingapp.common.LoadingState
import com.example.readingapp.repo.BookRepository
import com.example.readingapp.repo.FireRepository
import com.example.readingapp.ui.components.BookStatus
import com.example.readingapp.ui.destinations.DetailsScreenDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UpdateViewModel @Inject constructor(
    private val bookRepository: BookRepository,
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
            fireRepository.savedBooks.collect { allSavedBooks ->
                val savedBook = allSavedBooks.find { it.googleBookId == args.bookId }
                _uiState.update { state ->
                    state.copy(
                        loadingState = savedBook?.let { LoadingState.SUCCESS } ?: LoadingState.FAILED,
                        bookId = savedBook?.id,
                        book = savedBook,
                        isSaved = savedBook != null,
                        selectedStatus = savedBook?.bookStatus ?: BookStatus.LIBRARY
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

    fun onRatingChanged(rating: Double) {
        _uiState.update { state ->
            state.copy(book = state.book?.copy(rating = rating))
        }
    }

    fun onSaveClick() {
        /* TODO */
    }

    fun onCancelClick() {
        navigateBack()
    }
}