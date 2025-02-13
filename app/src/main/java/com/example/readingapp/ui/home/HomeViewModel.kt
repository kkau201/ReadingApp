package com.example.readingapp.ui.home

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.viewModelScope
import com.example.readingapp.common.BaseViewModel
import com.example.readingapp.common.DependencyContextWrapper
import com.example.readingapp.common.ErrorType
import com.example.readingapp.common.LoadingState
import com.example.readingapp.nav.NavigateTo
import com.example.readingapp.repo.FireRepository
import com.example.readingapp.ui.destinations.DetailsScreenDestination
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val fireRepository: FireRepository,
    dependencyContextWrapper: DependencyContextWrapper
) : BaseViewModel(dependencyContextWrapper), DefaultLifecycleObserver {

    private val _uiState: MutableStateFlow<HomeUiState?> = MutableStateFlow(null)
    val uiState: StateFlow<HomeUiState?>
        get() = _uiState

    fun onLoad() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                updateLoadingState(LoadingState.Loading())
                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: throw ErrorType.UnknownUserException
                val user = fireRepository.user.value ?: fireRepository.fetchUser(currentUserId).getOrThrow()
                val books = fireRepository.fetchSavedBooksByUser().getOrThrow()

                withContext(Dispatchers.Main) {
                    _uiState.update {
                        HomeUiState(
                            displayName = user?.displayName,
                            userAvatar = user?.avatar,
                            readingList = books
                        )
                    }
                    updateLoadingState(LoadingState.Success)
                }
            } catch (e: Exception) {
                updateLoadingState(LoadingState.Failed(e.message))
                showErrorDialog(e)
            }
        }
    }

    fun onBookClick(bookId: String) = navigate(NavigateTo(DetailsScreenDestination(bookId)))

}