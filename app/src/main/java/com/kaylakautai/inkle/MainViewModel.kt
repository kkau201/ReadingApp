package com.kaylakautai.inkle

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaylakautai.inkle.common.LoadingState
import com.kaylakautai.inkle.nav.NavigationEvent
import com.kaylakautai.inkle.ui.components.DialogState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel(), DefaultLifecycleObserver {
    private val _navigationFlow: Flow<NavigationEvent?> = MutableSharedFlow()
    val navigationFlow: Flow<NavigationEvent?>
        get() = _navigationFlow

    val loadingFlow: StateFlow<LoadingState>
        field: MutableStateFlow<LoadingState> = MutableStateFlow(LoadingState.Idle)

    val dialogFlow: StateFlow<DialogState?>
        field: MutableStateFlow<DialogState?> = MutableStateFlow(null)

    val toastFlow: StateFlow<String?>
        field: MutableStateFlow<String?> = MutableStateFlow(null)

    fun navigate(navigationEvent: NavigationEvent) {
        viewModelScope.launch {
            (_navigationFlow as MutableSharedFlow).emit(navigationEvent)
        }
    }

    fun updateLoadingState(state: LoadingState) = loadingFlow.update { state }

    fun showDialog(state: DialogState) = dialogFlow.update { state }
    fun dismissDialog() = dialogFlow.update { null }

    fun showToast(text: String) = toastFlow.update { text }
    fun resetToast() {
        toastFlow.value = null
    }
}