package com.example.readingapp.ui.user

import com.example.readingapp.model.MBook
import com.example.readingapp.model.MUser

data class UserUiState(
    val user: MUser? = null,
    val books: List<MBook> = emptyList(),
    /** True when the saved books couldn't be loaded, so [books] being empty isn't real. */
    val booksFailed: Boolean = false,
    /** Set while the "Verify your email" card should show; it's the address the link went to. */
    val unverifiedEmail: String? = null,
    /** True once a verification email has been resent from the card, to avoid repeat sends. */
    val verificationResent: Boolean = false,
    /** True briefly after the app notices the email was just verified, to say thanks. */
    val emailJustVerified: Boolean = false
)