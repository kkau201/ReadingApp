package com.example.readingapp.ui.user

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.readingapp.R
import com.example.readingapp.common.ViewModelBinding
import com.example.readingapp.ui.components.ReadingAppButton
import com.example.readingapp.ui.theme.AppTheme
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@Destination
@Composable
fun UserScreen(
    viewModel: UserViewModel = hiltViewModel(),
    navigator: DestinationsNavigator
) {
    ViewModelBinding(viewModel = viewModel, navigator = navigator)

    UserContent(viewModel::onLogoutClick)
}

@Composable
fun UserContent(onLogoutClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = AppTheme.spacing.lgSpacing, horizontal = AppTheme.spacing.mdSpacing)
    ) {
        Icon(
            imageVector = Icons.Filled.AccountCircle,
            contentDescription = stringResource(R.string.cont_desc_profile_image),
            modifier = Modifier.size(200.dp)
        )
        Text(
            text = "Jane Doe",
            fontWeight = FontWeight.Bold,
            style = AppTheme.typography.titleLarge,
            modifier = Modifier.padding(top = AppTheme.spacing.mdSpacing)
        )
        Text(
            text = "\"So many books, so little time.\"",
            style = AppTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = AppTheme.spacing.smSpacing, bottom = AppTheme.spacing.lgSpacing)
        )
        ReadingAppButton(
            text = "Logout",
            onClick = onLogoutClick,
            modifier = Modifier.fillMaxWidth()
        )
    }
}