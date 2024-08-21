package com.example.readingapp.ui.user.update

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.readingapp.ui.theme.AppTheme.spacing
import com.example.readingapp.ui.theme.Pink

@Composable
fun UpdateUserInputField(
    modifier: Modifier = Modifier,
    label: String,
    input: String,
    onInputChange: (String) -> Unit
) {
    val textFieldColors = TextFieldDefaults.outlinedTextFieldColors(
        textColor = Pink,
        unfocusedLabelColor = Pink.copy(alpha = 0.5f),
        unfocusedBorderColor = Pink.copy(alpha = 0.5f),
        focusedBorderColor = Pink,
        focusedLabelColor = Pink,
        cursorColor = Pink
    )

    OutlinedTextField(
        label = { Text(text = label) },
        value = input,
        shape = RoundedCornerShape(spacing.smSpacing),
        colors = textFieldColors,
        onValueChange = onInputChange,
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = spacing.smSpacing)
    )
}