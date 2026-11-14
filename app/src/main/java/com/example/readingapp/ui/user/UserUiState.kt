package com.example.readingapp.ui.user

import com.example.readingapp.model.MBook
import com.example.readingapp.model.MUser

data class UserUiState(
    val user: MUser? = null,
    val books: List<MBook> = emptyList()
)