package com.example.readingapp.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.readingapp.R
import com.example.readingapp.common.ViewModelBinding
import com.example.readingapp.ui.components.EmailInput
import com.example.readingapp.ui.components.PasswordInput
import com.example.readingapp.ui.components.ReadingAppButton
import com.example.readingapp.ui.theme.AppTheme
import com.example.readingapp.utils.keyboardAsState
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
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
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
        LoginForm(
            loading = uiState.value.loading,
            email = uiState.value.email,
            onEmailChange = viewModel::onEmailChange,
            password = uiState.value.password,
            onPasswordChange = viewModel::onPasswordChange,
            passwordVisible = uiState.value.passwordVisibility,
            updatePasswordVisibility = viewModel::updatePasswordVisibility,
            keyboardController = keyboardController,
            onLoginClick = viewModel::onLoginClick
        )
        SignUpSection(onSignUpClick = viewModel::onSignUpClick)
    }
}

@Composable
fun LoginForm(
    loading: Boolean,
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    updatePasswordVisibility: (Boolean) -> Unit,
    onLoginClick: () -> Unit,
    keyboardController: SoftwareKeyboardController?
) {
    // Clear focus if keyboard was hidden by user
    val isKeyboardOpen by keyboardAsState()
    val focusManager = LocalFocusManager.current
    if (!isKeyboardOpen) focusManager.clearFocus()

    val textFieldColors = TextFieldDefaults.textFieldColors(
        focusedIndicatorColor = AppTheme.colors.primary,
        unfocusedIndicatorColor = Color.Transparent
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(AppTheme.spacing.mdSpacing)
    ) {
        EmailInput(
            loading = loading,
            email = email,
            onEmailChange = onEmailChange,
            textFieldColors = textFieldColors,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
            modifier = Modifier.padding(bottom = AppTheme.spacing.smSpacing)
        )

        PasswordInput(
            loading = loading,
            password = password,
            onPasswordChange = onPasswordChange,
            passwordVisible = passwordVisible,
            updatePasswordVisibility = updatePasswordVisibility,
            textFieldColors = textFieldColors,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
            modifier = Modifier.padding(bottom = AppTheme.spacing.mdSpacing)
        )

        ReadingAppButton(
            text = stringResource(R.string.login_btn),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = AppTheme.spacing.xxsmSpacing)
        ) {
            focusManager.clearFocus()
            onLoginClick()
        }
    }
}

@Composable
fun SignUpSection(onSignUpClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(text = stringResource(R.string.new_user), modifier = Modifier.padding(end = AppTheme.spacing.xxsmSpacing))
        Text(
            text = stringResource(R.string.sign_up),
            color = AppTheme.colors.primary,
            textDecoration = TextDecoration.Underline,
            modifier = Modifier.clickable { onSignUpClick() })

    }
}