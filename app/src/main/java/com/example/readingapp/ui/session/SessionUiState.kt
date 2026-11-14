package com.example.readingapp.ui.session

import com.example.readingapp.model.MBook
import com.example.readingapp.model.MSession

data class SessionUiState(
    val books: List<MBook> = emptyList(),
    val sessions: List<MSession> = emptyList(),
    val selectedBookId: String? = null,
    val pagesInput: String = "",
    val noteInput: String = "",
    val year: Int = 0,
    val month: Int? = null,
    val day: String = "",
    val bookError: Boolean = false,
    val pagesError: Boolean = false,
    val dateError: Boolean = false,
    val logging: Boolean = false,
    val changingDate: Boolean = false
)
