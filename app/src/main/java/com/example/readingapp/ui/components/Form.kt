package com.example.readingapp.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.readingapp.R
import com.example.readingapp.common.LoadingState
import com.example.readingapp.ui.theme.AppTheme
import com.example.readingapp.ui.theme.Ink
import com.example.readingapp.ui.theme.Muted
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
    keyboardController: SoftwareKeyboardController?,
    errorMessage: String?,
    showPasswordHint: Boolean = false,
    modifier: Modifier = Modifier
) {
    val isKeyboardOpen by keyboardAsState()
    val focusManager = LocalFocusManager.current
    if (!isKeyboardOpen) focusManager.clearFocus()

    Column(modifier = modifier.fillMaxWidth()) {
        EmailInput(
            loading = loading,
            email = email,
            onEmailChange = onEmailChange,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
            modifier = Modifier.fillMaxWidth()
        )
        PasswordInput(
            loading = loading,
            password = password,
            onPasswordChange = onPasswordChange,
            passwordVisible = passwordVisible,
            updatePasswordVisibility = updatePasswordVisibility,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp)
        )
        if (showPasswordHint) {
            Text(
                text = stringResource(R.string.password_hint),
                color = Muted,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp, start = 8.dp)
            )
        }
        if (!errorMessage.isNullOrBlank()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
        ReadingAppButton(
            text = stringResource(submitText),
            enabled = submitEnabled,
            modifier = Modifier.padding(top = 32.dp)
        ) {
            focusManager.clearFocus()
            onSubmitClick()
        }
    }
}
