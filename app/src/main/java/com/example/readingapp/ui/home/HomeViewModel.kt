package com.example.readingapp.ui.home

import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.viewModelScope
import com.example.readingapp.common.BaseViewModel
import com.example.readingapp.common.LoadingState
import com.example.readingapp.mock.generateMockData
import com.example.readingapp.model.MBook
import com.example.readingapp.model.toModel
import com.example.readingapp.nav.NavigateTo
import com.example.readingapp.ui.destinations.UserScreenDestination
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

const val TAG = "HomeViewModel"

@HiltViewModel
class HomeViewModel @Inject constructor() : BaseViewModel(), DefaultLifecycleObserver {
    override val isNavigationDestination: Boolean = true

    private val _uiState: MutableStateFlow<HomeUiState?> = MutableStateFlow(null)
    val uiState: StateFlow<HomeUiState?>
        get() = _uiState

    fun onLoad() {
        val mockBooks = generateMockData()

        viewModelScope.launch(Dispatchers.IO) {
            updateLoadingState(LoadingState.LOADING)
            val user = FirebaseAuth.getInstance().currentUser
            val db = Firebase.firestore
            db.collection("users").get().addOnSuccessListener { documents ->
                for (document in documents) {
                    if (document.get("user_id")?.equals(user?.uid) == true) {
                        Log.d(TAG, "Found user by id: $document")
                        val mUser = document.toModel()
                        updateUser(mUser)
                        _uiState.update {
                            HomeUiState(
                                displayName = mUser.displayName,
                                readingActivity = mockBooks,
                                readingList = mockBooks
                            )
                        }
                        updateLoadingState(LoadingState.IDLE)
                    }
                }
            }
        }
    }

    fun onProfileClick() {
        navigate(NavigateTo(UserScreenDestination))
    }

    fun onBookClick(book: MBook) {
        Log.d("ReadingAppTesting", "Book clicked with title: ${book.title}")
        // TODO
    }
}