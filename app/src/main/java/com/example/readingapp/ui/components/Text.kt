package com.example.readingapp.ui.components

import androidx.annotation.StringRes
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.example.readingapp.ui.theme.AppTheme

@Composable
fun Title(@StringRes text: Int, modifier: Modifier = Modifier, color: Color = AppTheme.colors.primary) {
    Title(text = stringResource(id = text), color = color, modifier = modifier)
}

@Composable
fun Title(text: String, modifier: Modifier = Modifier, color: Color = AppTheme.colors.primary) {
    Text(
        text = text,
        color = color,
        style = AppTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = modifier
    )
}