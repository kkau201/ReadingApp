package com.kaylakautai.inkle.ui.session

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kaylakautai.inkle.R
import com.kaylakautai.inkle.model.TextLimits
import com.kaylakautai.inkle.ui.components.InkleButton
import com.kaylakautai.inkle.ui.details.update.RatingField
import com.kaylakautai.inkle.ui.theme.Muted

/** Marks the book finished, with an optional rating and note (the book's own note). */
@Composable
internal fun FinishBookDialog(
    form: FinishForm,
    onRatingChanged: (Int) -> Unit,
    onNoteChanged: (String) -> Unit,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    SessionFormDialog(onDismiss = onCancel) {
        FinishBookForm(
            form = form,
            onRatingChanged = onRatingChanged,
            onNoteChanged = onNoteChanged,
            onCancel = onCancel,
            onConfirm = onConfirm
        )
    }
}

@Composable
private fun ColumnScope.FinishBookForm(
    form: FinishForm,
    onRatingChanged: (Int) -> Unit,
    onNoteChanged: (String) -> Unit,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = form.title,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = form.message,
            color = Muted,
            style = MaterialTheme.typography.bodyMedium
        )
    }
    RatingField(selectedRating = form.rating, onRatingChanged = onRatingChanged)
    NoteField(
        value = form.noteInput,
        onValueChange = onNoteChanged,
        label = stringResource(R.string.update_book_notes),
        hint = stringResource(R.string.session_finish_note_hint),
        maxLength = TextLimits.BOOK_NOTES
    )
    InkleButton(
        text = stringResource(R.string.session_finish_confirm),
        onClick = onConfirm
    )
    FormCancelLink(text = form.cancelText, onClick = onCancel)
}

@Preview(showBackground = true, backgroundColor = 0xFFF7F4EF)
@Composable
private fun FinishBookFormPreview() {
    BorderedCard(modifier = Modifier.padding(24.dp)) {
        FinishBookForm(
            form = FinishForm(
                title = "You’ve reached the last page",
                message = "You’ve logged every page of Persuasion. Mark it as finished?",
                cancelText = "Not yet",
                rating = 4
            ),
            onRatingChanged = {},
            onNoteChanged = {},
            onCancel = {},
            onConfirm = {}
        )
    }
}
