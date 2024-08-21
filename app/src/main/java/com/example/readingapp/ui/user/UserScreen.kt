package com.example.readingapp.ui.user

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.readingapp.R
import com.example.readingapp.common.ViewModelBinding
import com.example.readingapp.common.observeLifecycle
import com.example.readingapp.ui.components.ReadingAppButton
import com.example.readingapp.ui.details.ObserveDetailsLifecycleEvents
import com.example.readingapp.ui.theme.AppTheme
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@Destination
@Composable
fun UserScreen(
    viewModel: UserViewModel = hiltViewModel(),
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    navigator: DestinationsNavigator
) {
    ViewModelBinding(viewModel = viewModel, navigator = navigator)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ObserveUserLifecycleEvents(lifecycleOwner, viewModel)

    UserContent(
        userDisplayName = uiState.user?.displayName ?: "",
        userBio = uiState.user?.quote ?: "",
        userAvatarUrl = uiState.user?.avatarUrl?.ifBlank { null },
        viewModel::onLogoutClick
    )
}

@Composable
fun UserContent(
    userDisplayName: String,
    userBio: String,
    userAvatarUrl: String? = null,
    onLogoutClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = AppTheme.spacing.lgSpacing, horizontal = AppTheme.spacing.mdSpacing)
    ) {
        userAvatarUrl?.let {
            AsyncImage(
                model = userAvatarUrl,
                contentDescription = stringResource(R.string.cont_desc_profile_image),
                contentScale = ContentScale.Crop,
                modifier = Modifier.clip(CircleShape).size(200.dp)
            )
        } ?: run {
            Icon(
                imageVector = Icons.Filled.AccountCircle,
                contentDescription = stringResource(R.string.cont_desc_profile_image),
                modifier = Modifier.size(200.dp)
            )
        }
        Text(
            text = userDisplayName,
            fontWeight = FontWeight.Bold,
            style = AppTheme.typography.titleLarge,
            modifier = Modifier.padding(top = AppTheme.spacing.mdSpacing)
        )
        Text(
            text = userBio,
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

@Composable
fun ObserveUserLifecycleEvents(
    lifecycleOwner: LifecycleOwner,
    viewModel: UserViewModel
) {
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) viewModel.loadUser()
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    viewModel.observeLifecycle(lifecycleOwner)
}