package com.example.readingapp.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.readingapp.R
import com.example.readingapp.common.LoadingState
import com.example.readingapp.common.ViewModelBinding
import com.example.readingapp.common.observeLifecycle
import com.example.readingapp.ui.components.LibraryPageTitle
import com.example.readingapp.ui.theme.AppTheme
import com.example.readingapp.ui.theme.Canvas
import com.example.readingapp.utils.keyboardAsState

@Composable
fun SearchScreen(
    viewModel: SearchViewModel = hiltViewModel(),
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
) {
    ViewModelBinding(viewModel)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val keyboardController = LocalSoftwareKeyboardController.current
    val isKeyboardOpen by keyboardAsState()
    val focusManager = LocalFocusManager.current
    if (!isKeyboardOpen) focusManager.clearFocus()

    ObserveLifecycleEvents(viewModel, lifecycleOwner)

    val results = uiState.results
    LaunchedEffect(results) {
        when (results) {
            is SearchResults.Success -> viewModel.updateLoadingState(LoadingState.Success)
            is SearchResults.Loading -> viewModel.updateLoadingState(LoadingState.Loading())
            is SearchResults.Error -> {
                viewModel.updateLoadingState(LoadingState.Failed(results.exception?.message))
                viewModel.showErrorDialog(results.exception)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Canvas)
            .statusBarsPadding()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { keyboardController?.hide() })
            }
    ) {
        LibraryPageTitle(
            text = stringResource(R.string.search_title),
            modifier = Modifier.padding(
                start = AppTheme.spacing.mdSpacing,
                end = AppTheme.spacing.mdSpacing,
                top = 12.dp
            )
        )
        SearchInput(
            currentInput = uiState.searchInput,
            keyboardController = keyboardController,
            onInputChange = viewModel::onInputChange,
            onDoneClick = viewModel::onDoneClick,
            modifier = Modifier.padding(
                start = AppTheme.spacing.mdSpacing,
                end = AppTheme.spacing.mdSpacing,
                top = 20.dp
            )
        )
        if (results is SearchResults.Success) {
            SearchResults(
                books = results.books,
                onBookClick = viewModel::onBookClick,
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 12.dp)
            )
        }
    }
}

@Composable
fun ObserveLifecycleEvents(viewModel: SearchViewModel, lifecycleOwner: LifecycleOwner) {
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) viewModel.onLoad()
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    viewModel.observeLifecycle(lifecycleOwner)
}
