package com.kaylakautai.inkle.auth

import com.kaylakautai.inkle.model.TextLimits
import com.kaylakautai.inkle.model.limitTo

/**
 * Display name for a new user record: the account's name if it has one, otherwise the part of
 * the email before '@', otherwise "Reader". Cut to [TextLimits.DISPLAY_NAME].
 */
internal fun defaultDisplayName(displayName: String?, email: String?): String {
    val name = displayName?.takeIf { it.isNotBlank() }
        ?: email?.substringBefore('@').orEmpty().ifBlank { "Reader" }
    // Google names and email prefixes can be longer than firestore.rules allows.
    return name.trim().limitTo(TextLimits.DISPLAY_NAME).trimEnd()
}
