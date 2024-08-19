package com.example.readingapp

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.readingapp.common.LoadingState
import com.example.readingapp.model.MUser
import com.example.readingapp.nav.NavigationEvent
import com.example.readingapp.ui.components.DialogState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@Composable
fun mainActivity() = LocalContext.current as MainActivity

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel(), DefaultLifecycleObserver {
    private val _navigationFlow: Flow<NavigationEvent?> = MutableSharedFlow()
    val navigationFlow: Flow<NavigationEvent?>
        get() = _navigationFlow

    private val _loadingFlow: MutableStateFlow<LoadingState> = MutableStateFlow(LoadingState.IDLE)
    val loadingFlow: StateFlow<LoadingState>
        get() = _loadingFlow

    private val _dialogFlow: MutableStateFlow<DialogState?> = MutableStateFlow(null)
    val dialogFlow: StateFlow<DialogState?>
        get() = _dialogFlow

    private val _toastFlow: MutableStateFlow<String?> = MutableStateFlow(null)
    val toastFlow: StateFlow<String?>
        get() = _toastFlow


    private val _userFlow: MutableStateFlow<MUser?> = MutableStateFlow(null)
    val userFlow: StateFlow<MUser?>
        get() = _userFlow

    fun navigate(navigationEvent: NavigationEvent) {
        viewModelScope.launch {
            (_navigationFlow as MutableSharedFlow).emit(navigationEvent)
        }
    }

    fun updateLoadingState(state: LoadingState) = _loadingFlow.update { state }

    fun showDialog(state: DialogState) = _dialogFlow.update { state }
    fun dismissDialog() = _dialogFlow.update { null }

    fun showToast(text: String) = _toastFlow.update { text }
    fun resetToast() { _toastFlow.value = null }

    fun updateUser(user: MUser?) = _userFlow.update { user }
}