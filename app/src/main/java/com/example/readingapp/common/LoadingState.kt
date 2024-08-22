package com.example.readingapp.common

sealed class LoadingState {
    data object Idle : LoadingState()
    data class Loading(val loadingType: LoadingType = LoadingType.MINI) : LoadingState()
    data object Success : LoadingState()
    data class Failed(val message: String? = null) : LoadingState()

    enum class LoadingType {
        MINI, FULL_SCREEN
    }
}