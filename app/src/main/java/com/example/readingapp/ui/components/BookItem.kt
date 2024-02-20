package com.example.readingapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.readingapp.ui.theme.AppTheme

@Composable
fun BookItem(
    title: String?,
    authors: String?
) {
    Box(
        modifier = Modifier.background(
            color = AppTheme.colors.surface,
            shape = RoundedCornerShape(AppTheme.spacing.mdSpacing)
        )
    ) {
        title?.let { Text(text = it) }
        authors?.let { Text(text = it) }
    }
}