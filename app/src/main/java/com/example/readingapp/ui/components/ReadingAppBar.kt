package com.example.readingapp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBackIosNew
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.readingapp.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingAppBar(
    title: String? = null
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
        title = {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                title?.let {
                    Text(text = title, style = MaterialTheme.typography.headlineSmall)
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingAppBarNav(
    backgroundColor: Color = Color.Transparent,
    title: String? = null,
    navIcon: ImageVector = Icons.Outlined.ArrowBackIosNew,
    navIconTint: Color = Color.White,
    onNavIconClick: () -> Unit = {},
    actionButtons: List<ActionButton>? = null
) {
    CenterAlignedTopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundColor),
        title = {
            title?.let {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = navIconTint
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onNavIconClick) {
                Icon(imageVector = navIcon, contentDescription = null, tint = navIconTint)
            }
        },
        actions = {
            actionButtons?.forEach { button ->
                Text(
                    text = button.label,
                    color = button.tint,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier
                        .clickable(enabled = button.isEnabled, onClick = button.onClick)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
    )
}

data class ActionButton(
    val label: String,
    val tint: Color = Color.Black,
    val isEnabled: Boolean = true,
    val onClick: () -> Unit
)

@Preview(name = "Reading App Bar Preview")
@Composable
fun ReadingAppBarPreview() {
    AppTheme {
        ReadingAppBar(title = "Library")
    }
}

@Preview(name = "Reading App Bar Nav Preview")
@Composable
fun ReadingAppBarNavPreview() {
    AppTheme {
        ReadingAppBarNav(
            title = "Book Details",
            onNavIconClick = {}
        )
    }
}
