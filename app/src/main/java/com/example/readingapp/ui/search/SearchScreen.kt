package com.example.readingapp.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.readingapp.R
import com.example.readingapp.common.ViewModelBinding
import com.example.readingapp.ui.components.LibraryPageTitle
import com.example.readingapp.ui.theme.AppTheme
import com.example.readingapp.utils.keyboardAsState

@Composable
fun SearchScreen(
    viewModel: SearchViewModel = hiltViewModel()
) {
    ViewModelBinding(viewModel)
    val query by viewModel.query.collectAsStateWithLifecycle()
    val results by viewModel.results.collectAsStateWithLifecycle()
    val keyboardController = LocalSoftwareKeyboardController.current
    val isKeyboardOpen by keyboardAsState()
    val focusManager = LocalFocusManager.current
    if (!isKeyboardOpen) focusManager.clearFocus()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
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
            currentInput = query,
            keyboardController = keyboardController,
            onInputChange = viewModel::onQueryChange,
            onDoneClick = viewModel::onSearchClick,
            modifier = Modifier.padding(
                start = AppTheme.spacing.mdSpacing,
                end = AppTheme.spacing.mdSpacing,
                top = 20.dp
            )
        )
        SearchResultsContent(
            results = results,
            onBookClick = viewModel::onBookClick,
            onRetryClick = viewModel::onRetryClick,
            modifier = Modifier
                .weight(1f)
                .padding(top = 12.dp)
        )
    }
}
