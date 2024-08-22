package com.example.readingapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBackIosNew
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.readingapp.ui.theme.AppTheme

@Composable
fun ReadingAppBar(
    title: String? = null
) {
    TopAppBar(
        modifier = Modifier.padding(vertical = AppTheme.spacing.smSpacing, horizontal = AppTheme.spacing.xsmSpacing),
        backgroundColor = AppTheme.colors.background,
        elevation = 0.dp,
        title = {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                title?.let {
                    Text(text = title, style = AppTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
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
        modifier = Modifier.padding(vertical = AppTheme.spacing.smSpacing, horizontal = AppTheme.spacing.xsmSpacing),
        colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundColor),
        title = {
            title?.let {
                Text(
                    text = title,
                    style = AppTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
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
                IconButton(
                    modifier = Modifier.background(color = button.backgroundColor, shape = CircleShape),
                    enabled = button.isEnabled,
                    onClick = button.onClick
                ) {
                    Icon(imageVector = button.icon, contentDescription = null, tint = button.tint)
                }
            }
        }
    )
}

data class ActionButton(
    val icon: ImageVector,
    val tint: Color = Color.Black,
    val backgroundColor: Color = Color.Transparent,
    val isEnabled: Boolean = true,
    val onClick: () -> Unit
)