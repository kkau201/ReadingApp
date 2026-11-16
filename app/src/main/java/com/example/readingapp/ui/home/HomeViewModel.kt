package com.example.readingapp.ui.home

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.viewModelScope
import com.example.readingapp.common.BaseViewModel
import com.example.readingapp.common.DependencyContextWrapper
import com.example.readingapp.common.ErrorType
import com.example.readingapp.common.LoadingState
import com.example.readingapp.coroutines.CoroutineDispatcherProvider
import com.example.readingapp.nav.NavigateTo
import com.example.readingapp.auth.AuthRepository
import com.example.readingapp.repo.FireRepository
import com.example.readingapp.ui.destinations.DetailsScreenDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val fireRepository: FireRepository,
    private val authRepository: AuthRepository,
    private val dispatchers: CoroutineDispatcherProvider,
    dependencyContextWrapper: DependencyContextWrapper
) : BaseViewModel(dependencyContextWrapper), DefaultLifecycleObserver {

    val uiState: StateFlow<HomeUiState?>
        field: MutableStateFlow<HomeUiState?> = MutableStateFlow(null)

    fun onLoad() {
        viewModelScope.launch(dispatchers.getIo()) {
            try {
                updateLoadingState(LoadingState.Loading())
                val currentUserId = authRepository.currentUserId ?: throw ErrorType.UnknownUserException
                val user = fireRepository.user.value ?: fireRepository.fetchUser(currentUserId).getOrThrow()
                val books = fireRepository.fetchSavedBooksByUser().getOrThrow()

                withContext(dispatchers.getMain()) {
                    uiState.update {
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