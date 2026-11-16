package com.example.readingapp.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.example.readingapp.ui.theme.Ink
import com.example.readingapp.ui.theme.Muted

/**
 * A "123 / 1000" counter for a TextField's `supportingText`, or null while the text is still
 * below [showFrom] of [max]. Returning null (rather than an empty lambda) keeps the field from
 * reserving space for supporting text until the counter is actually needed.
 */
fun characterCounter(
    length: Int,
    max: Int,
    showFrom: Float = 0.8f
): (@Composable () -> Unit)? {
    if (length < max * showFrom) return null
    return {
        Text(
            text = "$length / $max",
            color = if (length >= max) Ink else Muted,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.End,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
