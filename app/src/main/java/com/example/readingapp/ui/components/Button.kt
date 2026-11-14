package com.example.readingapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.readingapp.ui.theme.AppTheme
import com.example.readingapp.ui.theme.Ink

@Composable
fun ReadingAppButton(
    modifier: Modifier = Modifier,
    text: String,
    enabled: Boolean = true,
    backgroundColor: Color = Ink,
    contentColor: Color = Color.White,
    onClick: () -> Unit
) {
    val container = if (enabled) backgroundColor else backgroundColor.copy(alpha = 0.35f)
    val content = if (enabled) contentColor else contentColor.copy(alpha = 0.8f)
    Text(
        text = text,
        color = content,
        style = AppTheme.typography.labelLarge,
        textAlign = TextAlign.Center,
        modifier = modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .background(container)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp)
    )
}
