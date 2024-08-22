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
import com.example.readingapp.nav.NavigateTo
import com.example.readingapp.repo.BookRepository
import com.example.readingapp.repo.FireRepository
import com.example.readingapp.repo.FireRepository.Companion.BOOKS_COLLECTION
import com.example.readingapp.ui.components.DialogState
import com.example.readingapp.ui.destinations.DetailsScreenDestination
import com.example.readingapp.ui.destinations.UpdateScreenDestination
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
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

    private val _uiState: MutableStateFlow<DetailsUiState> = MutableStateFlow(DetailsUiState())
    val uiState: StateFlow<DetailsUiState>
        get() = _uiState

    fun loadBook() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                bookRepository.getBookInfo(args.bookId).collect { result ->
                    when (result) {
                        is RemoteResult.Loading -> updateLoadingState(LoadingState.Loading())
                        is RemoteResult.Success -> {
                            updateLoadingState(LoadingState.Success)
                            _uiState.update { state ->
                                state.copy(book = result.data)
                            }
                        }
                        is RemoteResult.Error -> {
                            updateLoadingState(LoadingState.Failed(result.exception?.message))
                            showErrorDialog(result.exception)
                        }
                    }
                }
            } catch (e: Exception) {
                showErrorDialog(e)
            }

            fireRepository.savedBooks.collect { allSavedBooks ->
                val savedBook = allSavedBooks.find { it.googleBookId == args.bookId }
                _uiState.update { state ->
                    state.copy(
                        bookId = savedBook?.id,
                        isSaved = savedBook != null
                    )
                }
            }
        }
    }

    fun toggleSavedBook() = viewModelScope.launch {
        if (uiState.value.isSaved) showDialog(
            DialogState(
                title = getString(R.string.remove_book_title),
                message = getString(R.string.remove_book_message),
                primaryButtonText = getString(R.string.remove_book_primary_button),
                secondaryButtonText = getString(R.string.remove_book_secondary_button),
                onPrimaryClick = {
                    dismissDialog()
                    removeBook()
                },
                onSecondaryClick = { dismissDialog() }
            )
        )
        else saveBook()
    }

    private fun saveBook() {
        uiState.value.book?.let { book ->
            val db = FirebaseFirestore.getInstance()
            val bookWithId = book.copy(
                userId = FirebaseAuth.getInstance().currentUser?.uid.toString()
            )
            val dbCollection = db.collection(BOOKS_COLLECTION)
            if (bookWithId.toString().isNotEmpty()) {
                dbCollection.add(bookWithId)
                    .addOnSuccessListener { documentRef ->
                        val docId = documentRef.id
                        dbCollection.document(docId)
                            .update(hashMapOf("id" to docId) as Map<String, Any>)
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
        }
    }

    private fun removeBook() {
        uiState.value.bookId?.let { bookId ->
            val db = FirebaseFirestore.getInstance()
            db.collection(BOOKS_COLLECTION).document(bookId)
                .delete()
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        showToast(getString(R.string.book_removed_toast))
                        fetchSavedBooks()
                    }
                }
                .addOnFailureListener {
                    Log.e("Error", "saveBook: Error deleting doc", it)
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