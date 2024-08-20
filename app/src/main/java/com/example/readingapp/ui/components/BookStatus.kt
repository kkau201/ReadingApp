package com.example.readingapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.readingapp.ui.theme.AppTheme.spacing
import com.example.readingapp.ui.theme.Pink

@Composable
fun BookStatusButton(
    color: Color = Pink,
    status: BookStatus,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    IconButton(
        modifier = Modifier
            .background(
                color = if (isSelected) color.copy(alpha = 0.3f) else Color.Transparent,
                shape = RoundedCornerShape(20)
            )
            .padding(spacing.xxsmSpacing),
        onClick = onClick
    ) {
        Icon(
            imageVector = status.icon,
            tint = color,
            contentDescription = status.statusText,
            modifier = Modifier.size(40.dp)
        )
    }
}

enum class BookStatus(val icon: ImageVector, val statusText: String) {
    LIBRARY(Icons.Rounded.Book, "Library"),
    READING(Icons.AutoMirrored.Rounded.MenuBook, "Reading"),
    FINISHED(Icons.Rounded.CheckCircle, "Finished")
}