package com.example.readingapp.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.example.readingapp.common.LoadingState
import com.example.readingapp.ui.theme.AppTheme
import com.example.readingapp.utils.keyboardAsState

@Composable
fun InputForm(
    loading: LoadingState,
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    updatePasswordVisibility: (Boolean) -> Unit,
    @StringRes submitText: Int,
    onSubmitClick: () -> Unit,
    submitEnabled: Boolean,
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
            text = stringResource(submitText),
            enabled = submitEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = AppTheme.spacing.xxsmSpacing)
        ) {
            focusManager.clearFocus()
            onSubmitClick()
        }
    }
}