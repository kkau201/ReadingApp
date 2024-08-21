package com.example.readingapp.ui.user.update

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.readingapp.R
import com.example.readingapp.common.ViewModelBinding
import com.example.readingapp.common.observeLifecycle
import com.example.readingapp.ui.components.ReadingAppBarNav
import com.example.readingapp.ui.details.update.UpdateButtons
import com.example.readingapp.ui.theme.AppTheme
import com.example.readingapp.ui.theme.AppTheme.spacing
import com.example.readingapp.ui.theme.Blue
import com.example.readingapp.utils.keyboardAsState
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@Destination
@Composable
fun UpdateUserScreen(
    viewModel: UpdateUserViewModel = hiltViewModel(),
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    navigator: DestinationsNavigator
) {
    ViewModelBinding(viewModel = viewModel, navigator = navigator)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ObserveUpdateUserLifecycle(lifecycleOwner, viewModel)

    // Clear focus if keyboard was hidden by user
    val keyboardController = LocalSoftwareKeyboardController.current
    val isKeyboardOpen by keyboardAsState()
    val focusManager = LocalFocusManager.current
    if (!isKeyboardOpen) focusManager.clearFocus()

    Scaffold(
        backgroundColor = AppTheme.colors.background,
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    keyboardController?.hide()
                })
            },
        topBar = {
            ReadingAppBarNav(
                title = stringResource(R.string.update_user_title),
                navIconTint = Color.Black,
                onNavIconClick = viewModel::navigateBack
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(horizontal = spacing.lgSpacing)
                .background(shape = RoundedCornerShape(spacing.smSpacing), color = Blue)
                .fillMaxWidth()
                .padding(spacing.smSpacing)
        ) {
            UpdateUserInputField(
                label = stringResource(id = R.string.update_user_name_label),
                input = uiState.displayNameInput,
                onInputChange = viewModel::onDisplayNameInputChange
            )
            UpdateUserInputField(
                label = stringResource(id = R.string.update_user_bio_label),
                input = uiState.bioInput,
                onInputChange = viewModel::onBioInputChange
            )

            UpdateButtons(
                onSaveClick = {
                    keyboardController?.hide()
                    viewModel.onUpdateClick()
                },
                onCancelClick = {
                    keyboardController?.hide()
                    viewModel.navigateBack()
                }
            )
        }
    }
}

@Composable
fun ObserveUpdateUserLifecycle(
    lifecycleOwner: LifecycleOwner,
    viewModel: UpdateUserViewModel,
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