package com.example.readingapp.ui.session

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.readingapp.common.ViewModelBinding
import com.example.readingapp.common.observeLifecycle
import com.example.readingapp.ui.theme.Paper

@Composable
fun SessionScreen(
    viewModel: SessionViewModel = hiltViewModel(),
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
) {
    ViewModelBinding(viewModel)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = Paper,
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
    ) { padding ->
        SessionContent(
            state = uiState,
            onBookSelected = viewModel::onBookSelected,
            onPagesChanged = viewModel::onPagesChanged,
            onNoteChanged = viewModel::onNoteChanged,
            onYearChanged = viewModel::onYearChanged,
            onMonthChanged = viewModel::onMonthChanged,
            onDayChanged = viewModel::onDayChanged,
            onStartLog = viewModel::onStartLog,
            onCancelLog = viewModel::onCancelLog,
            onChangeDay = viewModel::onChangeDay,
            onUseToday = viewModel::onUseToday,
            onSave = viewModel::onSaveSession,
            onRemove = viewModel::onRemoveSession,
            modifier = Modifier.padding(padding)
        )
    }

    LaunchedEffect(lifecycleOwner) {
        viewModel.onLoad()
    }

    viewModel.observeLifecycle(lifecycleOwner)
}
