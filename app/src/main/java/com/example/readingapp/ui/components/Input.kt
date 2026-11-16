package com.example.readingapp.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.readingapp.R
import com.example.readingapp.common.LoadingState
import com.example.readingapp.ui.theme.CardBackground
import com.example.readingapp.ui.theme.Ink
import com.example.readingapp.ui.theme.Muted

private val FieldShape = RoundedCornerShape(20.dp)

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
    Column(modifier = modifier) {
        Text(
            text = label,
            color = Muted,
            style = MaterialTheme.typography.labelLarge
        )
        TextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = Ink),
            shape = FieldShape,
            colors = TextFieldDefaults.colors(
                focusedTextColor = Ink,
                unfocusedTextColor = Ink,
                disabledTextColor = Ink.copy(alpha = 0.6f),
                cursorColor = Ink,
                focusedContainerColor = CardBackground,
                unfocusedContainerColor = CardBackground,
                disabledContainerColor = CardBackground,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ),
            singleLine = true,
            enabled = enabled,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            trailingIcon = trailingIcon,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        )
    }
}
