package com.example.readingapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import com.example.readingapp.ui.theme.AppTheme

@Composable
fun ReadingAppButton(
    modifier: Modifier = Modifier,
    text: String,
    padding: PaddingValues = PaddingValues(AppTheme.spacing.smSpacing),
    enabled: Boolean = true,
    backgroundColor: Color = AppTheme.colors.primary,
    contentColor: Color = AppTheme.colors.onPrimary,
    onClick: () -> Unit
) {

    val buttonColors = ButtonDefaults.buttonColors(
        backgroundColor = backgroundColor,
        contentColor = contentColor,
        disabledBackgroundColor = backgroundColor.copy(alpha = 0.6f),
        disabledContentColor = contentColor.copy(alpha = 0.6f)
    )

    Button(
        onClick = onClick,
        shape = RoundedCornerShape(AppTheme.spacing.lgSpacing),
        colors = buttonColors,
        enabled = enabled,
        modifier = Modifier.padding(padding)
    ) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = text,
                style = AppTheme.typography.bodyMedium,
                color = contentColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f, false)
            )
        }
    }
}