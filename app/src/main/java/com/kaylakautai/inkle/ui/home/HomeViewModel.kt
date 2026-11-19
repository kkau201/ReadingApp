package com.kaylakautai.inkle.ui.home

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.viewModelScope
import com.kaylakautai.inkle.common.BaseViewModel
import com.kaylakautai.inkle.common.DependencyContextWrapper
import com.kaylakautai.inkle.common.ErrorType
import com.kaylakautai.inkle.common.LoadingState
import com.kaylakautai.inkle.coroutines.CoroutineDispatcherProvider
import com.kaylakautai.inkle.nav.NavigateTo
import com.kaylakautai.inkle.auth.AuthRepository
import com.kaylakautai.inkle.repo.FireRepository
import com.kaylakautai.inkle.ui.destinations.DetailsScreenDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
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

    private var isWatching = false

    fun onLoad() {
        watchForChanges()
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

    /** Once the shelf has loaded, keeps it current as the books or profile change. */
    private fun watchForChanges() {
        if (isWatching) return
        isWatching = true
        viewModelScope.launch {
            fireRepository.savedBooks.collect { books ->
                uiState.update { it?.copy(readingList = books) }
            }
        }
        viewModelScope.launch {
            fireRepository.user.filterNotNull().collect { user ->
                uiState.update { it?.copy(displayName = user.displayName, userAvatar = user.avatar) }
            }
        }
    }

    fun onBookClick(bookId: String) = navigate(NavigateTo(DetailsScreenDestination(bookId)))

}