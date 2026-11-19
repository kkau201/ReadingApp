package com.kaylakautai.inkle.ui.components

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.kaylakautai.inkle.R
import com.kaylakautai.inkle.common.LoadingState
import com.kaylakautai.inkle.ui.theme.Ink

@Composable
fun EmailInput(
    loading: LoadingState,
    email: String,
    onEmailChange: (String) -> Unit,
    keyboardOptions: KeyboardOptions,
    keyboardActions: KeyboardActions,
    modifier: Modifier = Modifier
) {
    AuthField(
        label = stringResource(R.string.email),
        value = email,
        onValueChange = onEmailChange,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        // Only lock the field while a request is in flight. The loading state is shared across
        // screens, so it may still be Success/Failed from the previous screen (e.g. after logout).
        enabled = loading !is LoadingState.Loading,
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
    keyboardOptions: KeyboardOptions,
    keyboardActions: KeyboardActions,
    modifier: Modifier = Modifier
) {
    AuthField(
        label = stringResource(R.string.password),
        value = password,
        onValueChange = onPasswordChange,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        // Only lock the field while a request is in flight. The loading state is shared across
        // screens, so it may still be Success/Failed from the previous screen (e.g. after logout).
        enabled = loading !is LoadingState.Loading,
        visualTransformation = if (passwordVisible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        trailingIcon = {
            IconButton(onClick = { updatePasswordVisibility(!passwordVisible) }) {
                Icon(
                    imageVector = if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                    tint = Ink,
                    contentDescription = stringResource(
                        if (passwordVisible) R.string.cont_desc_hide_password else R.string.cont_desc_show_password
                    )
                )
            }
        },
        modifier = modifier
    )
}

@Composable
private fun AuthField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardOptions: KeyboardOptions,
    keyboardActions: KeyboardActions,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    InkleTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        enabled = enabled,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        visualTransformation = visualTransformation,
        trailingIcon = trailingIcon,
        modifier = modifier
    )
}
