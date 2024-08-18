package com.example.readingapp.ui.home

import com.example.readingapp.model.MBook
import com.example.readingapp.model.MBookDetails

data class HomeUiState(
    val displayName: String? = null,
    val readingActivity: List<MBook> = emptyList(),
    val readingList: List<MBookDetails> = emptyList()
)