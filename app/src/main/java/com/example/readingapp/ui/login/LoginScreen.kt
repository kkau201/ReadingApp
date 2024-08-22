package com.example.readingapp.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.readingapp.R
import com.example.readingapp.common.LoadingState
import com.example.readingapp.common.ViewModelBinding
import com.example.readingapp.ui.components.InputForm
import com.example.readingapp.ui.components.Title
import com.example.readingapp.ui.theme.AppTheme
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@Destination
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    navigator: DestinationsNavigator
) {
    ViewModelBinding(viewModel = viewModel, navigator = navigator)
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    val loadingState = viewModel.getLoadingState().collectAsStateWithLifecycle()
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .background(color = AppTheme.colors.background)
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    keyboardController?.hide()
                })
            }
            .padding(AppTheme.spacing.mdSpacing)
    ) {
        Image(
            painter = painterResource(id = R.drawable.book_logo_two_no_bg),
            contentDescription = stringResource(R.string.cont_desc_book_logo),
            modifier = Modifier.size(80.dp)
        )
        Title(
            text = if (uiState.value.loginScreenState == LoginScreenState.LOGIN) R.string.login_title else R.string.register_title,
            modifier = Modifier.padding(top = AppTheme.spacing.xlgSpacing)
        )
        InputForm(
            loading = loadingState.value,
            email = uiState.value.email,
            onEmailChange = viewModel::onEmailChange,
            password = uiState.value.password,
            onPasswordChange = viewModel::onPasswordChange,
            passwordVisible = uiState.value.passwordVisibility,
            updatePasswordVisibility = viewModel::updatePasswordVisibility,
            keyboardController = keyboardController,
            submitText = if (uiState.value.loginScreenState == LoginScreenState.LOGIN) R.string.login else R.string.register,
            submitEnabled = uiState.value.isEmailValid && uiState.value.isPasswordValid && loadingState.value !is LoadingState.Loading,
            onSubmitClick = viewModel::onSubmitClick,
            errorMessage = uiState.value.errorMessage
        )
        SwitchScreenSection(screenState = uiState.value.loginScreenState, onSwitchClick = viewModel::onSwitchScreens)
    }
}

@Composable
fun SwitchScreenSection(screenState: LoginScreenState, onSwitchClick: (LoginScreenState) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(if (screenState == LoginScreenState.LOGIN) R.string.new_user else R.string.already_user),
            modifier = Modifier.padding(end = AppTheme.spacing.xxsmSpacing)
        )
        Text(
            text = stringResource(if (screenState == LoginScreenState.LOGIN) R.string.sign_up else R.string.login),
            color = AppTheme.colors.primary,
            textDecoration = TextDecoration.Underline,
            modifier = Modifier.clickable { onSwitchClick(LoginScreenState.switch(screenState)) })

    }
}