package com.example.readingapp.ui.details

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.readingapp.common.ViewModelBinding
import com.example.readingapp.common.observeLifecycle
import com.example.readingapp.ui.components.ReadingAppBarNav
import com.example.readingapp.ui.theme.AppTheme
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@Suppress("UNUSED_PARAMETER")
@Destination
@Composable
fun DetailsScreen(
    bookId: String,
    viewModel: DetailsViewModel = hiltViewModel(),
    navigator: DestinationsNavigator,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
) {
    ViewModelBinding(viewModel = viewModel, navigator = navigator)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ObserveDetailsLifecycleEvents(lifecycleOwner, viewModel)

    Scaffold(
        backgroundColor = AppTheme.colors.background,
        modifier = Modifier.fillMaxSize(),
        topBar = {
            ReadingAppBarNav(
                navIconTint = Color.Black,
                onNavIconClick = viewModel::navigateBack,
                actionIcon = if (uiState.isSaved) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder,
                actionIconTint = Color.Black,
                onActionIconClick = viewModel::onSaveBookClick
            )
        }
    ) { padding ->
        uiState.book?.let {
            DetailsScreenContent(
                book = it,
                modifier = Modifier.padding(padding),
                onUpdateBookClick = viewModel::updateBook,
            )
        }
    }
}

@Composable
fun ObserveDetailsLifecycleEvents(
    lifecycleOwner: LifecycleOwner,
    viewModel: DetailsViewModel
) {
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) viewModel.loadBook()
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    viewModel.observeLifecycle(lifecycleOwner)
}