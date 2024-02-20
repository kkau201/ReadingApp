package com.example.readingapp.ui.home

import com.example.readingapp.model.MBook

data class HomeUiState(
    val displayName: String? = null,
    val readingActivity: List<MBook> = emptyList()
)