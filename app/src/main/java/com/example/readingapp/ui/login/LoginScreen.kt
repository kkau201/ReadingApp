package com.example.readingapp.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.readingapp.R
import com.example.readingapp.common.LoadingState
import com.example.readingapp.common.ViewModelBinding
import com.example.readingapp.ui.components.InputForm
import com.example.readingapp.ui.components.LibraryPageTitle
import com.example.readingapp.ui.components.ReadingAppButton
import com.example.readingapp.ui.theme.AppTheme
import com.example.readingapp.ui.theme.Ink
import com.example.readingapp.ui.theme.Muted
import com.example.readingapp.ui.theme.Paper
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@Destination<RootGraph>
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    navigator: DestinationsNavigator
) {
    ViewModelBinding(viewModel = viewModel, navigator = navigator)
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    val loadingState = viewModel.getLoadingState().collectAsStateWithLifecycle()
    val keyboardController = LocalSoftwareKeyboardController.current
    val isLogin = uiState.value.loginScreenState == LoginScreenState.LOGIN

    Column(
        modifier = Modifier
            .background(Paper)
            .fillMaxSize()
            .systemBarsPadding()
            .imePadding()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { keyboardController?.hide() })
            }
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(top = 36.dp, bottom = 24.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.book_logo_three_no_bg),
            contentDescription = stringResource(R.string.cont_desc_book_logo),
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(72.dp).align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(24.dp))
        LibraryPageTitle(
            text = stringResource(if (isLogin) R.string.login_title else R.string.register_title),
            modifier = Modifier.padding(top = 28.dp)
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
            submitText = if (isLogin) R.string.login else R.string.register,
            submitEnabled = uiState.value.isEmailValid && uiState.value.isPasswordValid && loadingState.value !is LoadingState.Loading,
            onSubmitClick = viewModel::onSubmitClick,
            errorMessage = uiState.value.errorMessage,
            showPasswordHint = !isLogin,
            modifier = Modifier.padding(top = 32.dp)
        )
        if (isLogin) {
            Text(
                text = stringResource(R.string.forgot_password),
                color = Ink,
                style = AppTheme.typography.labelLarge,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 16.dp)
                    .clickable(enabled = loadingState.value !is LoadingState.Loading) {
                        keyboardController?.hide()
                        viewModel.onForgotPasswordClick()
                    }
            )
        }
        OrDivider(modifier = Modifier.padding(top = 24.dp))
        ReadingAppButton(
            text = stringResource(R.string.continue_with_google),
            enabled = loadingState.value !is LoadingState.Loading,
            backgroundColor = Paper,
            contentColor = Ink,
            onClick = {
                keyboardController?.hide()
                viewModel.onGoogleSignInClick()
            },
            modifier = Modifier
                .padding(top = 24.dp)
                .border(1.dp, Ink, CircleShape)
        )
        SwitchScreenSection(
            screenState = uiState.value.loginScreenState,
            onSwitchClick = viewModel::onSwitchScreens,
            modifier = Modifier.padding(top = 20.dp)
        )
    }
}

@Composable
private fun OrDivider(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = Muted.copy(alpha = 0.4f))
        Text(
            text = stringResource(R.string.or_divider),
            color = Muted,
            style = AppTheme.typography.bodySmall
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = Muted.copy(alpha = 0.4f))
    }
}

@Composable
fun SwitchScreenSection(
    screenState: LoginScreenState,
    onSwitchClick: (LoginScreenState) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = stringResource(if (screenState == LoginScreenState.LOGIN) R.string.new_user else R.string.already_user),
            color = Muted,
            style = AppTheme.typography.bodyMedium
        )
        Text(
            text = stringResource(if (screenState == LoginScreenState.LOGIN) R.string.sign_up else R.string.login),
            color = Ink,
            style = AppTheme.typography.labelLarge,
            modifier = Modifier.clickable { onSwitchClick(LoginScreenState.switch(screenState)) }
        )
    }
}
