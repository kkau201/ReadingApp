package com.example.readingapp.ui.user

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.readingapp.R
import com.example.readingapp.common.ViewModelBinding
import com.example.readingapp.common.observeLifecycle
import com.example.readingapp.ui.components.ReadingAppBar
import com.example.readingapp.ui.components.ReadingAppTab
import com.example.readingapp.ui.theme.AppTheme
import com.example.readingapp.ui.theme.AppTheme.spacing
import com.example.readingapp.ui.user.update.Avatar

@Composable
fun UserScreen(
    themeColor: Color,
    viewModel: UserViewModel = hiltViewModel(),
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
) {
    ViewModelBinding(viewModel)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ObserveUserLifecycleEvents(lifecycleOwner, viewModel)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = AppTheme.colors.background,
        topBar = {
            ReadingAppBar()
        }
    ) { padding ->
        UserContent(
            modifier = Modifier.padding(padding),
            themeColor = themeColor,
            userDisplayName = uiState.user?.displayName ?: "",
            userBio = uiState.user?.quote ?: "",
            userAvatar = uiState.user?.avatar,
            viewModel::navigateToUpdateUser,
            viewModel::onLogoutClick,
        )
    }
}

@Composable
fun UserContent(
    modifier: Modifier = Modifier,
    themeColor: Color,
    userDisplayName: String,
    userBio: String,
    userAvatar: Avatar? = null,
    onUpdateClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(vertical = spacing.lgSpacing, horizontal = spacing.mdSpacing)
    ) {
        userAvatar?.let {
            Image(
                painter = painterResource(id = userAvatar.img),
                contentDescription = stringResource(R.string.cont_desc_profile_image),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .clip(CircleShape)
                    .size(200.dp)
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
            modifier = Modifier.padding(top = spacing.mdSpacing)
        )
        Text(
            text = userBio,
            style = AppTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = spacing.smSpacing, bottom = spacing.lgSpacing)
        )
        Column(
            modifier = Modifier
                .background(
                    color = themeColor.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(spacing.smSpacing)
                )
                .padding(spacing.xsmSpacing)
        ) {
            ReadingAppTab(
                text = "Update Details",
                backgroundColor = themeColor,
                onClick = onUpdateClick,
                modifier = Modifier.fillMaxWidth()
            )
            ReadingAppTab(
                text = "Logout",
                backgroundColor = themeColor,
                onClick = onLogoutClick,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun ObserveUserLifecycleEvents(
    lifecycleOwner: LifecycleOwner,
    viewModel: UserViewModel
) {
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.loadUser()
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    viewModel.observeLifecycle(lifecycleOwner)
}