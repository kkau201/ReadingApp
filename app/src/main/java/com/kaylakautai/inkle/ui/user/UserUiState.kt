package com.kaylakautai.inkle.ui.user

import com.kaylakautai.inkle.model.MBook
import com.kaylakautai.inkle.model.MUser

data class UserUiState(
    val user: MUser? = null,
    val books: List<MBook> = emptyList(),
    /** True when the saved books couldn't be loaded, so [books] being empty isn't real. */
    val booksFailed: Boolean = false,
    /** True once the saved books have loaded, so [books] being empty is real. */
    val booksLoaded: Boolean = false,
    /** Set while the "Verify your email" card should show; it's the address the link went to. */
    val unverifiedEmail: String? = null,
    /** True once a verification email has been resent from the card, to avoid repeat sends. */
    val verificationResent: Boolean = false,
    /** True briefly after the app notices the email was just verified, to say thanks. */
    val emailJustVerified: Boolean = false
)