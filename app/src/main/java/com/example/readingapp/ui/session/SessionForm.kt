package com.example.readingapp.ui.session

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.readingapp.R
import com.example.readingapp.model.TextLimits
import com.example.readingapp.ui.components.characterCounter
import com.example.readingapp.ui.theme.CardBackground
import com.example.readingapp.ui.theme.Cover
import com.example.readingapp.ui.theme.Muted

/** Building blocks shared by the Session screen's cards and forms. */

internal val CardShape = RoundedCornerShape(20.dp)
private val FieldShape = RoundedCornerShape(20.dp)

/** A bordered card, as used for journal entries and the forms. */
@Composable
internal fun BorderedCard(
    modifier: Modifier = Modifier,
    spacing: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(CardShape)
            .border(1.dp, Cover, CardShape)
            .background(CardBackground)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(spacing),
        content = content
    )
}

/**
 * A form shown over the screen in a card. Scrolls when it's taller than the window (e.g. with
 * the keyboard up), and closes when tapped outside via [onDismiss].
 */
@Composable
internal fun SessionFormDialog(
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        BoxWithConstraints {
            val maxHeight = maxHeight * 0.86f
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth()
                    .heightIn(max = maxHeight)
                    .verticalScroll(rememberScrollState())
            ) {
                BorderedCard(content = content)
            }
        }
    }
}

/** The quiet "Cancel" (or "Not yet") link at the bottom of a form. */
@Composable
internal fun ColumnScope.FormCancelLink(text: String, onClick: () -> Unit) {
    Text(
        text = text,
        color = Muted,
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier
            .clickable(onClick = onClick)
            .align(Alignment.CenterHorizontally)
    )
}

@Composable
internal fun NumberField(
    label: String,
    value: String,
    placeholder: String,
    error: String?,
    onValueChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        FieldLabel(text = label)
        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = placeholder,
                    color = Muted,
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onBackground),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = FieldShape,
            colors = sessionFieldColors(),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Cover, FieldShape)
        )
        if (error != null) FieldError(text = error)
    }
}

@Composable
internal fun NoteField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String = stringResource(R.string.session_note),
    hint: String = stringResource(R.string.session_note_hint),
    maxLength: Int = TextLimits.SESSION_NOTE
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        FieldLabel(text = label)
        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = hint,
                    color = Muted,
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onBackground),
            minLines = 2,
            supportingText = characterCounter(value.length, maxLength),
            shape = FieldShape,
            colors = sessionFieldColors(),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Cover, FieldShape)
        )
    }
}

@Composable
internal fun FieldLabel(text: String) {
    Text(
        text = text,
        color = Muted,
        style = MaterialTheme.typography.labelLarge
    )
}

@Composable
internal fun FieldError(text: String) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.onBackground,
        style = MaterialTheme.typography.bodySmall
    )
}

@Composable
private fun sessionFieldColors() = TextFieldDefaults.colors(
    focusedTextColor = MaterialTheme.colorScheme.onBackground,
    unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
    cursorColor = MaterialTheme.colorScheme.onBackground,
    focusedContainerColor = CardBackground,
    unfocusedContainerColor = CardBackground,
    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent,
    disabledIndicatorColor = Color.Transparent
)
