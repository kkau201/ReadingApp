package com.example.readingapp.ui.components

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.example.readingapp.R
import com.example.readingapp.common.LoadingState
import com.example.readingapp.ui.theme.AppTheme

@Composable
fun EmailInput(
    loading: LoadingState,
    email: String,
    onEmailChange: (String) -> Unit,
    textFieldColors: TextFieldColors,
    keyboardOptions: KeyboardOptions,
    keyboardActions: KeyboardActions,
    modifier: Modifier = Modifier
) {
    TextField(
        value = email,
        onValueChange = onEmailChange,
        placeholder = { Text(stringResource(R.string.email)) },
        colors = textFieldColors,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = true,
        enabled = loading == LoadingState.Idle,
        modifier = modifier
    )
}


@Composable
fun PasswordInput(
    loading: LoadingState,
    password: String,
    onPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    updatePasswordVisibility: (Boolean) -> Unit,
    textFieldColors: TextFieldColors,
    keyboardOptions: KeyboardOptions,
    keyboardActions: KeyboardActions,
    modifier: Modifier = Modifier
) {
    val hasFocus = remember { mutableStateOf(false) }
    TextField(
        value = password,
        onValueChange = onPasswordChange,
        placeholder = { Text(stringResource(R.string.password)) },
        colors = textFieldColors,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = true,
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = { updatePasswordVisibility(!passwordVisible) }) {
                Icon(
                    imageVector = if (passwordVisible) Icons.Rounded.Visibility else Icons.Filled.VisibilityOff,
                    tint = if (hasFocus.value) AppTheme.colors.primary else Color.Gray,
                    contentDescription = if (passwordVisible) stringResource(R.string.cont_desc_hide_password)
                    else stringResource(R.string.cont_desc_show_password)
                )
            }
        },
        enabled = loading == LoadingState.Idle,
        modifier = modifier.onFocusChanged { hasFocus.value = it.isFocused }
    )
}