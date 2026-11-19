package com.kaylakautai.inkle.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kaylakautai.inkle.ui.theme.AppTheme
import com.kaylakautai.inkle.ui.theme.Ink
import com.kaylakautai.inkle.ui.theme.OnInk

/** The app's button padding: a 46dp-tall pill with the label's line height. */
val InkleButtonPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp)

/**
 * The app's button: a full-width pill. [contentPadding] is only for tight spaces (like the
 * business-card-sized stamp card); everything else keeps [InkleButtonPadding].
 */
@Composable
fun InkleButton(
    modifier: Modifier = Modifier,
    text: String,
    enabled: Boolean = true,
    backgroundColor: Color = Ink,
    contentColor: Color = OnInk,
    contentPadding: PaddingValues = InkleButtonPadding,
    onClick: () -> Unit
) {
    val container = if (enabled) backgroundColor else backgroundColor.copy(alpha = 0.35f)
    val content = if (enabled) contentColor else contentColor.copy(alpha = 0.8f)
    Text(
        text = text,
        color = content,
        style = MaterialTheme.typography.labelLarge,
        textAlign = TextAlign.Center,
        modifier = modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .background(container)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(contentPadding)
    )
}

@Preview(name = "Inkle Button Preview")
@Composable
fun InkleButtonPreview() {
    AppTheme {
        InkleButton(text = "Continue", onClick = {})
    }
}
