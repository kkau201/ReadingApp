package com.kaylakautai.inkle.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kaylakautai.inkle.ui.theme.AppTheme
import com.kaylakautai.inkle.ui.theme.CardBackground
import com.kaylakautai.inkle.ui.theme.Cover
import com.kaylakautai.inkle.ui.theme.Ink
import com.kaylakautai.inkle.ui.theme.Muted

/** The look shared by every text field in the app. */
object InkleTextFieldDefaults {

    /** Rounded, not a pill: the search bar is the one exception (it passes its own shape). */
    val Shape: Shape = RoundedCornerShape(20.dp)

    /**
     * A filled field with no underline, in the theme's colours by default. Pass fixed colours
     * for a field on something that doesn't follow the theme (like the green stamp card).
     */
    @Composable
    fun colors(
        text: Color = Ink,
        container: Color = CardBackground,
        placeholder: Color = Muted
    ): TextFieldColors = TextFieldDefaults.colors(
        focusedTextColor = text,
        unfocusedTextColor = text,
        disabledTextColor = text.copy(alpha = 0.6f),
        errorTextColor = text,
        cursorColor = text,
        errorCursorColor = text,
        focusedContainerColor = container,
        unfocusedContainerColor = container,
        disabledContainerColor = container,
        errorContainerColor = container,
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        disabledIndicatorColor = Color.Transparent,
        errorIndicatorColor = Color.Transparent,
        focusedPlaceholderColor = placeholder,
        unfocusedPlaceholderColor = placeholder,
        disabledPlaceholderColor = placeholder,
        errorPlaceholderColor = placeholder,
        focusedLeadingIconColor = text,
        unfocusedLeadingIconColor = text,
        focusedTrailingIconColor = text,
        unfocusedTrailingIconColor = text,
        errorLeadingIconColor = text,
        errorTrailingIconColor = text
    )
}

/**
 * The app's text field: a filled, rounded field with an optional [label] above it, a
 * [placeholder] while it's empty, an [error] below it, and a character counter once the text
 * nears [maxLength] (the limit itself is applied by whoever owns the text).
 *
 * Use [bordered] when the field sits on a card or dialog of the same colour as its fill, so
 * its edge still shows; the border darkens while there's an [error].
 */
@Composable
fun InkleTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    error: String? = null,
    maxLength: Int? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
    enabled: Boolean = true,
    bordered: Boolean = false,
    shape: Shape = InkleTextFieldDefaults.Shape,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    Column(modifier = modifier) {
        label?.let { InkleFieldLabel(text = it) }
        TextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = MaterialTheme.typography.bodyLarge,
            placeholder = placeholder?.let { hint ->
                { Text(text = hint, style = MaterialTheme.typography.bodyLarge) }
            },
            isError = error != null,
            supportingText = maxLength?.let { characterCounter(value.length, it) },
            singleLine = singleLine,
            minLines = minLines,
            enabled = enabled,
            shape = shape,
            colors = InkleTextFieldDefaults.colors(),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            visualTransformation = visualTransformation,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            modifier = Modifier
                .fillMaxWidth()
                .then(if (label != null) Modifier.padding(top = 8.dp) else Modifier)
                .then(
                    if (bordered) {
                        Modifier.border(1.dp, if (error != null) Ink else Cover, shape)
                    } else {
                        Modifier
                    }
                )
        )
        error?.let { InkleFieldError(text = it, modifier = Modifier.padding(top = 8.dp)) }
    }
}

/** The small label above a field (or a group of choices standing in for one). */
@Composable
fun InkleFieldLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        color = Muted,
        style = MaterialTheme.typography.labelLarge,
        modifier = modifier
    )
}

/** What's wrong with a field, shown below it. */
@Composable
fun InkleFieldError(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        color = Ink,
        style = MaterialTheme.typography.bodySmall,
        modifier = modifier
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFF7F4EF, widthDp = 360)
@Composable
private fun InkleTextFieldPreview() {
    AppTheme {
        Column(Modifier.padding(16.dp)) {
            InkleTextField(value = "Kayla", onValueChange = {}, label = "Name", maxLength = 5)
            InkleTextField(
                value = "",
                onValueChange = {},
                label = "Day",
                placeholder = "Optional",
                error = "That day is not in this month",
                bordered = true,
                modifier = Modifier.padding(top = 20.dp)
            )
        }
    }
}
