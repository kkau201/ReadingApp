package com.example.readingapp.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.readingapp.R
import com.example.readingapp.ui.theme.AppTheme

@Composable
fun ReadingAppBar(
    title: String? = null,
    actionIcon: ImageVector? = null,
    actionIconTint: Color = Color.Black,
    onActionIconClick: () -> Unit = {}
) {
    TopAppBar(
        modifier = Modifier.padding(vertical = AppTheme.spacing.smSpacing, horizontal = AppTheme.spacing.xsmSpacing),
        backgroundColor = AppTheme.colors.background,
        elevation = 0.dp,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.book_logo),
                    contentDescription = stringResource(id = R.string.cont_desc_book_logo),
                    modifier = Modifier
                        .size(60.dp)
                        .padding(end = AppTheme.spacing.smSpacing)
                )
                title?.let { Text(text = title, style = AppTheme.typography.bodyLarge) }
            }
        },
        actions = {
            actionIcon?.let {
                IconButton(onClick = onActionIconClick) {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = actionIconTint,
                        modifier = Modifier.size(35.dp)
                    )
                }
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingAppBarNav(
    backgroundColor: Color = Color.Transparent,
    title: String? = null,
    navIcon: ImageVector = Icons.AutoMirrored.Rounded.ArrowBack,
    actionIcon: ImageVector? = null,
    navIconTint: Color = Color.White,
    actionIconTint: Color = Color.White,
    onNavIconClick: () -> Unit = {},
    onActionIconClick: () -> Unit = {}
) {
    CenterAlignedTopAppBar(
        modifier = Modifier.padding(vertical = AppTheme.spacing.smSpacing, horizontal = AppTheme.spacing.xsmSpacing),
        colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundColor),
        title = {
            title?.let { Text(text = title, style = AppTheme.typography.bodyLarge, color = navIconTint) }
        },
        navigationIcon = {
            IconButton(onClick = onNavIconClick) {
                Icon(imageVector = navIcon, contentDescription = null, tint = navIconTint)
            }
        },
        actions = {
            actionIcon?.let {
                IconButton(onClick = onActionIconClick) {
                    Icon(imageVector = actionIcon, contentDescription = null, tint = actionIconTint)
                }
            }
        }
    )
}