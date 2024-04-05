package com.example.readingapp.ui.details

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.readingapp.common.BaseViewModel
import com.example.readingapp.common.DependencyContextWrapper
import com.example.readingapp.common.ErrorType
import com.example.readingapp.common.LoadingState
import com.example.readingapp.data.RemoteResult
import com.example.readingapp.repo.BookRepo
import com.example.readingapp.ui.components.DialogState
import com.example.readingapp.ui.destinations.DetailsScreenDestination
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
}