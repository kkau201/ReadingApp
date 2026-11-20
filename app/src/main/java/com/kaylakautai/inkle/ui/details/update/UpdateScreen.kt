package com.kaylakautai.inkle.ui.details.update

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kaylakautai.inkle.R
import com.kaylakautai.inkle.common.ViewModelBinding
import com.kaylakautai.inkle.common.observeLifecycle
import com.kaylakautai.inkle.ui.components.InkleAppBarNav
import com.kaylakautai.inkle.utils.keyboardAsState
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@Suppress("UNUSED_PARAMETER")
@Destination<RootGraph>
@Composable
fun UpdateScreen(
    bookId: String,
    navigator: DestinationsNavigator,
    viewModel: UpdateViewModel = hiltViewModel(),
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
) {
    ViewModelBinding(viewModel = viewModel, navigator = navigator)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ObserveUpdateLifecycleEvents(lifecycleOwner, viewModel)

    // Clear focus if keyboard was hidden by user
    val keyboardController = LocalSoftwareKeyboardController.current
    val isKeyboardOpen by keyboardAsState()
    val focusManager = LocalFocusManager.current
    if (!isKeyboardOpen) focusManager.clearFocus()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    keyboardController?.hide()
                })
            },
        topBar = {
            InkleAppBarNav(
                backgroundColor = MaterialTheme.colorScheme.background,
                title = stringResource(R.string.update_book_title),
                navIconTint = MaterialTheme.colorScheme.onBackground,
                onNavIconClick = viewModel::navigateBack
            )
        }
    ) { padding ->
        uiState.book?.let {
            UpdateScreenContent(
                modifier = Modifier
                    .padding(padding)
                    .consumeWindowInsets(padding)
                    .imePadding(),
                book = it,
                noteInput = uiState.noteInput,
                selectedStatus = uiState.selectedStatus,
                finishedYear = uiState.finishedYear,
                finishedMonth = uiState.finishedMonth,
                finishedDay = uiState.finishedDay,
                finishedOnError = uiState.finishedOnError,
                currentPageInput = uiState.currentPageInput,
                currentPageError = uiState.currentPageError,
                selectedRating = uiState.selectedRating,
                onNoteInputChanged = viewModel::onNoteInputChanged,
                onStatusChanged = viewModel::onStatusChanged,
                onFinishedYearChanged = viewModel::onFinishedYearChanged,
                onFinishedMonthChanged = viewModel::onFinishedMonthChanged,
                onFinishedDayChanged = viewModel::onFinishedDayChanged,
                onCurrentPageChanged = viewModel::onCurrentPageChanged,
                onRatingChanged = viewModel::onRatingChanged,
                onSaveClick = viewModel::onSaveClick,
                onCancelClick = viewModel::onCancelClick,
            )
        }
    }
}

@Composable
fun ObserveUpdateLifecycleEvents(
    lifecycleOwner: LifecycleOwner,
    viewModel: UpdateViewModel
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