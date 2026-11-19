package com.kaylakautai.inkle.ui.user.update

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
import com.kaylakautai.inkle.common.LoadingState
import com.kaylakautai.inkle.common.ViewModelBinding
import com.kaylakautai.inkle.common.observeLifecycle
import com.kaylakautai.inkle.ui.components.InkleAppBarNav
import com.kaylakautai.inkle.utils.keyboardAsState
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@Destination<RootGraph>
@Composable
fun UpdateUserScreen(
    viewModel: UpdateUserViewModel = hiltViewModel(),
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    navigator: DestinationsNavigator
) {
    ViewModelBinding(viewModel = viewModel, navigator = navigator)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val loadingState by viewModel.getLoadingState().collectAsStateWithLifecycle()

    ObserveUpdateUserLifecycle(lifecycleOwner, viewModel)

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
                title = stringResource(R.string.update_user_title),
                navIconTint = MaterialTheme.colorScheme.onBackground,
                onNavIconClick = viewModel::navigateBack
            )
        }
    ) { padding ->
        when (loadingState) {
            LoadingState.Success -> {
                UpdateUserContent(
                    modifier = Modifier
                        .padding(padding)
                        .consumeWindowInsets(padding)
                        .imePadding(),
                    uiState = uiState,
                    onDisplayNameInputChange = viewModel::onDisplayNameInputChange,
                    onBioInputChange = viewModel::onBioInputChange,
                    onReadingGoalChange = viewModel::onReadingGoalChange,
                    onRewardInputChange = viewModel::onRewardInputChange,
                    onAvatarChange = viewModel::onAvatarChange,
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

            else -> {}
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
            if (event == Lifecycle.Event.ON_START) viewModel.onLoad()
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    viewModel.observeLifecycle(lifecycleOwner)
}