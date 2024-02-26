package com.example.readingapp.ui.components

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.readingapp.R
import com.example.readingapp.ui.theme.AppTheme

@Composable
fun ReadingAppFab(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        shape = CircleShape,
        containerColor = AppTheme.colors.primary
    ) {
        Icon(
            imageVector = Icons.Rounded.Search,
            contentDescription = stringResource(R.string.cont_desc_add_a_book),
            tint = AppTheme.colors.onPrimary
        )
    }
}