package com.example.readingapp.ui.details.update

import com.example.readingapp.common.LoadingState
import com.example.readingapp.model.MBook
import com.example.readingapp.ui.components.DialogState

data class UpdateUiState(
    val book: MBook? = null,
    val loadingState: LoadingState = LoadingState.IDLE,
    val dialogState: DialogState? = null
)