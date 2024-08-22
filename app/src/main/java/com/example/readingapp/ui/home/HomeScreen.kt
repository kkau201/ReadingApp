package com.example.readingapp.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.readingapp.R
import com.example.readingapp.common.LoadingState
import com.example.readingapp.common.ViewModelBinding
import com.example.readingapp.common.observeLifecycle
import com.example.readingapp.model.MBook
import com.example.readingapp.ui.components.ReadingAppBar
import com.example.readingapp.ui.theme.AppTheme
import com.example.readingapp.ui.theme.AppTheme.spacing

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
) {
    ViewModelBinding(viewModel)
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    val loadingState = viewModel.getLoadingState().collectAsStateWithLifecycle()

    Scaffold(
        backgroundColor = AppTheme.colors.background,
        modifier = Modifier.fillMaxSize(),
        topBar = {
            ReadingAppBar()
        }
    ) { padding ->
        when (loadingState.value) {
            LoadingState.Success -> {
                uiState.value?.let { loadedState ->
                    HomeContent(
                        padding = padding,
                        displayName = loadedState.displayName,
                        readingList = loadedState.readingList,
                        currentReadingList = loadedState.currentReadingList,
                        onBookClick = viewModel::onBookClick
                    )
                }
            }
            else -> {}
        }
    }

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

@Composable
fun HomeContent(
    padding: PaddingValues,
    displayName: String?,
    readingList: List<MBook>,
    currentReadingList: List<MBook>,
    onBookClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .fillMaxSize()
            .padding(padding)
    ) {
        HomeUserIntro(displayName = displayName, modifier = Modifier.padding(bottom = spacing.mdSpacing))
        if (currentReadingList.isNotEmpty()) {
            HomeReadingRow(
                title = R.string.currently_reading,
                books = currentReadingList,
                savedBooks = readingList,
                onBookClick = onBookClick,
                modifier = Modifier.padding(bottom = spacing.mdSpacing)
            )
        }
        if (readingList.isNotEmpty()) {
            HomeReadingRow(
                title = R.string.reading_list,
                books = readingList,
                savedBooks = readingList,
                onBookClick = onBookClick
            )
        }
        Spacer(modifier = Modifier.height(spacing.lgSpacing))
    }
}