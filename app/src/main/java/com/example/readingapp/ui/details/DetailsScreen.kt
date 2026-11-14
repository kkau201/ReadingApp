package com.example.readingapp.ui.details

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.readingapp.R
import com.example.readingapp.common.ViewModelBinding
import com.example.readingapp.common.observeLifecycle
import com.example.readingapp.ui.components.ActionButton
import com.example.readingapp.ui.components.ReadingAppBarNav
import com.example.readingapp.ui.theme.Ink
import com.example.readingapp.ui.theme.Muted
import com.example.readingapp.ui.theme.Paper
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@Suppress("UNUSED_PARAMETER")
@Destination<RootGraph>
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
        containerColor = Paper,
        modifier = Modifier.fillMaxSize(),
        topBar = {
            ReadingAppBarNav(
                backgroundColor = Paper,
                navIconTint = Ink,
                onNavIconClick = viewModel::navigateBack,
                actionButtons = if (uiState.isSaved) {
                    listOf(
                        ActionButton(
                            label = stringResource(R.string.details_remove),
                            tint = Muted,
                            onClick = viewModel::toggleSavedBook
                        ),
                        ActionButton(
                            label = stringResource(R.string.details_edit),
                            tint = Ink,
                            onClick = viewModel::navToUpdateScreen
                        )
                    )
                } else {
                    listOf(
                        ActionButton(
                            label = stringResource(R.string.details_save),
                            tint = Ink,
                            onClick = viewModel::toggleSavedBook
                        )
                    )
                },
            )
        }
    ) { padding ->
        uiState.book?.let {
            DetailsScreenContent(
                book = it,
                savedStatus = uiState.savedBook?.bookStatus,
                onStatusClick = viewModel::onChooseStatus,
                modifier = Modifier.padding(padding)
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