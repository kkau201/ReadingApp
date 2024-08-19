package com.example.readingapp.ui.details

import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.readingapp.common.BaseViewModel
import com.example.readingapp.common.DependencyContextWrapper
import com.example.readingapp.common.ErrorType
import com.example.readingapp.common.LoadingState
import com.example.readingapp.data.RemoteResult
import com.example.readingapp.nav.NavigateTo
import com.example.readingapp.repo.BookRepo
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
    private val bookRepo: BookRepo,
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
                bookRepo.getBookInfo(args.bookId).collect { result ->
                    when (result) {
                        is RemoteResult.Loading -> updateLoadingState(LoadingState.LOADING)
                        is RemoteResult.Success -> {
                            updateLoadingState(LoadingState.SUCCESS)
                            _uiState.update { it.copy(book = result.data) }
                        }
                        is RemoteResult.Error -> {
                            updateLoadingState(LoadingState.FAILED)
                            showErrorDialog(result.exception)
                        }
                    }
                }
            } catch (e: Exception) {
                showErrorDialog(e)
            }
        }
    }

    private fun showErrorDialog(e: Throwable?) {
        val error = if (e is ErrorType) e else ErrorType.UnknownNetworkException(e)

        showDialog(
            DialogState(
                title = getString(error.title),
                message = getString(error.body),
                primaryButtonText = getString(error.primaryBtn),
                onPrimaryClick = {
                    dismissDialog()
                    navigateBack()
                }
            )
        )
    }

    fun saveBook() {
        uiState.value.book?.let { book ->
            val db = FirebaseFirestore.getInstance()
            val bookWithId = book.copy(
                userId = FirebaseAuth.getInstance().currentUser?.uid.toString()
            )
            val dbCollection = db.collection("books")
            if (bookWithId.toString().isNotEmpty()) {
                dbCollection.add(bookWithId)
                    .addOnSuccessListener { documentRef ->
                        val docId = documentRef.id
                        dbCollection.document(docId)
                            .update(hashMapOf("id" to docId) as Map<String, Any>)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    showDialog(
                                        state = DialogState(
                                            title = "Success",
                                            message = "Book saved successfully",
                                            primaryButtonText = "OK",
                                            onPrimaryClick = { dismissDialog() }
                                        )
                                    )
                                }
                            }
                            .addOnFailureListener {
                                Log.e("Error", "saveBook: Error updating doc", it)
                            }
                    }
            }
        }
    }

    fun updateBook() {
        navigate(NavigateTo(UpdateScreenDestination(args.bookId)))
    }
}