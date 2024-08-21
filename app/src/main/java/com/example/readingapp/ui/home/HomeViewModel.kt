package com.example.readingapp.ui.home

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.viewModelScope
import com.example.readingapp.common.BaseViewModel
import com.example.readingapp.common.DependencyContextWrapper
import com.example.readingapp.common.LoadingState
import com.example.readingapp.nav.NavigateTo
import com.example.readingapp.repo.FireRepository
import com.example.readingapp.ui.destinations.DetailsScreenDestination
import com.example.readingapp.ui.destinations.SearchScreenDestination
import com.example.readingapp.ui.destinations.UserScreenDestination
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val fireRepository: FireRepository,
    dependencyContextWrapper: DependencyContextWrapper
) : BaseViewModel(dependencyContextWrapper), DefaultLifecycleObserver {
    override val isNavigationDestination: Boolean = true

    private val _uiState: MutableStateFlow<HomeUiState?> = MutableStateFlow(null)
    val uiState: StateFlow<HomeUiState?>
        get() = _uiState

    fun onLoad() {
        viewModelScope.launch(Dispatchers.IO) {
            updateLoadingState(LoadingState.LOADING)

            FirebaseAuth.getInstance().currentUser?.uid?.let { currentUserId ->
                if (fireRepository.user.value == null) {
                    fireRepository.fetchUser(currentUserId)
                }

                fireRepository.fetchSavedBooksByUser(currentUserId)
            } ?: run {
                updateLoadingState(LoadingState.FAILED)
            }

            _uiState.update {
                HomeUiState(
                    displayName = fireRepository.user.value?.displayName,
                    userAvatar = fireRepository.user.value?.avatar,
                    readingList = fireRepository.savedBooks.value
                )
            }
            updateLoadingState(LoadingState.IDLE)
        }
    }

    fun onProfileClick() {
        navigate(NavigateTo(UserScreenDestination))
    }

    fun onFabClick() {
        navigate(NavigateTo(SearchScreenDestination))
    }

    fun onBookClick(bookId: String) = navigate(NavigateTo(DetailsScreenDestination(bookId)))

}