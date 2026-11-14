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
import com.example.readingapp.ui.components.DialogState
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
        val current = saved?.bookStatus
        showDialog(
            DialogState(
                title = getString(
                    if (saved == null) R.string.details_add_shelf else R.string.details_update_status
                ),
                choices = listOf(
                    statusChoice(BookStatus.LIBRARY, R.string.home_filter_want_to_read, current),
                    statusChoice(BookStatus.READING, R.string.home_filter_reading, current),
                    statusChoice(BookStatus.FINISHED, R.string.home_filter_finished, current)
                ),
                secondaryButtonText = getString(R.string.update_cancel),
                onSecondaryClick = { dismissDialog() },
                onDismissRequest = { dismissDialog() }
            )
        )
    }

    private fun statusChoice(status: BookStatus, label: Int, current: BookStatus?): DialogChoice {
        return DialogChoice(
            text = getString(label),
            selected = status == current,
            onClick = { applyStatus(status) }
        )
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

    private fun saveBook(status: BookStatus) {
        val book = uiState.value.book ?: return
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        if (fireRepository.savedBooks.value.any { it.googleBookId == args.bookId }) return

        val db = FirebaseFirestore.getInstance()
        val (started, finished) = readingTimes(saved = null, status = status)
        val bookWithId = book.copy(
            userId = userId,
            googleBookId = book.googleBookId ?: args.bookId,
            startedReading = started,
            finishedReading = finished
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

    private fun updateStatus(saved: MBook, status: BookStatus) {
        val bookId = saved.id ?: return
        val (started, finished) = readingTimes(saved, status)
        fireRepository.updateBook(
            bookId = bookId,
            bookUpdates = mapOf(
                "started_reading" to started,
                "finished_reading" to finished
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

    private fun readingTimes(saved: MBook?, status: BookStatus): Pair<Timestamp?, Timestamp?> {
        val now = Timestamp.now()
        val started = when {
            status == BookStatus.LIBRARY -> null
            saved?.startedReading != null && status != BookStatus.LIBRARY -> saved.startedReading
            else -> now
        }
        val finished = if (status == BookStatus.FINISHED) {
            saved?.finishedReading ?: now
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