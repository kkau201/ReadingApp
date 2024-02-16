package com.example.readingapp

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.readingapp.common.LoadingState
import com.example.readingapp.nav.NavigationEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@Composable
fun mainActivity() = LocalContext.current as MainActivity

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel(), DefaultLifecycleObserver {
    val navigationFlow: Flow<NavigationEvent?> = MutableSharedFlow()
    val loadingFlow: MutableStateFlow<LoadingState> = MutableStateFlow(LoadingState.IDLE)

    fun navigate(navigationEvent: NavigationEvent) {
        viewModelScope.launch {
            (navigationFlow as MutableSharedFlow).emit(navigationEvent)
        }
    }

    fun updateLoadingState(state: LoadingState) = loadingFlow.update { state }
}