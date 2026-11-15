package com.example.readingapp.ui.details.update

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.readingapp.R
import com.example.readingapp.model.MBook
import com.example.readingapp.model.TextLimits
import com.example.readingapp.ui.components.BookStatus
import com.example.readingapp.ui.components.OutlineStar
import com.example.readingapp.ui.components.characterCounter
import com.example.readingapp.ui.theme.AppTheme
import com.example.readingapp.ui.theme.Cover
import com.example.readingapp.ui.theme.Ink
import com.example.readingapp.ui.theme.Muted

private val FieldShape = RoundedCornerShape(20.dp)

@Composable
fun UpdateScreenContent(
    modifier: Modifier = Modifier,
    book: MBook,
    selectedStatus: BookStatus,
    finishedYear: Int,
    finishedMonth: Int?,
    finishedDay: String,
    finishedOnError: Boolean,
    selectedRating: Int,
    noteInput: String,
    onNoteInputChanged: (String) -> Unit,
    onStatusChanged: (BookStatus) -> Unit,
    onFinishedYearChanged: (Int) -> Unit,
    onFinishedMonthChanged: (Int?) -> Unit,
    onFinishedDayChanged: (String) -> Unit,
    onRatingChanged: (Int) -> Unit,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    Column(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 12.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            Text(
                text = book.title.orEmpty(),
                color = Ink,
                style = AppTheme.typography.headlineSmall
            )
            StatusField(
                selected = selectedStatus,
                finishedYear = finishedYear,
                finishedMonth = finishedMonth,
                finishedDay = finishedDay,
                finishedOnError = finishedOnError,
                onStatusChanged = onStatusChanged,
                onFinishedYearChanged = onFinishedYearChanged,
                onFinishedMonthChanged = onFinishedMonthChanged,
                onFinishedDayChanged = onFinishedDayChanged
            )
            RatingField(
                selectedRating = selectedRating,
                onRatingChanged = onRatingChanged
            )
            NotesField(
                noteInput = noteInput,
                onNoteInputChanged = onNoteInputChanged
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FormButton(
                text = stringResource(R.string.update_cancel),
                container = Color.White,
                content = Ink,
                border = Cover,
                onClick = onCancelClick,
                modifier = Modifier.weight(1f)
            )
            FormButton(
                text = stringResource(R.string.update_save),
                container = Ink,
                content = Color.White,
                onClick = onSaveClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatusField(
    selected: BookStatus,
    finishedYear: Int,
    finishedMonth: Int?,
    finishedDay: String,
    finishedOnError: Boolean,
    onStatusChanged: (BookStatus) -> Unit,
    onFinishedYearChanged: (Int) -> Unit,
    onFinishedMonthChanged: (Int?) -> Unit,
    onFinishedDayChanged: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        FieldLabel(text = stringResource(R.string.update_book_status))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BookStatus.entries.forEach { status ->
                val isSelected = status == selected
                Text(
                    text = status.shelfLabel(),
                    color = if (isSelected) Color.White else Ink,
                    style = AppTheme.typography.labelLarge,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (isSelected) Ink else Color.White)
                        .border(
                            width = if (isSelected) 0.dp else 1.dp,
                            color = Cover,
                            shape = CircleShape
                        )
                        .clickable { onStatusChanged(status) }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
        if (selected == BookStatus.FINISHED) {
            FinishedDateField(
                year = finishedYear,
                month = finishedMonth,
                day = finishedDay,
                dayError = finishedOnError,
                onYearChanged = onFinishedYearChanged,
                onMonthChanged = onFinishedMonthChanged,
                onDayChanged = onFinishedDayChanged
            )
        }
    }
}

@Composable
private fun RatingField(
    selectedRating: Int,
    onRatingChanged: (Int) -> Unit
) {
    Column {
        FieldLabel(text = stringResource(R.string.update_book_rating))
        Row(
            modifier = Modifier.padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            for (star in 1..5) {
                val description = stringResource(R.string.cont_desc_star_number, star)
                OutlineStar(
                    color = if (star <= selectedRating) Ink else Ink.copy(alpha = 0.28f),
                    filled = star <= selectedRating,
                    modifier = Modifier
                        .semantics { contentDescription = description }
                        .clickable { onRatingChanged(star) }
                )
            }
        }
    }
}

@Composable
private fun NotesField(
    noteInput: String,
    onNoteInputChanged: (String) -> Unit
) {
    Column {
        FieldLabel(text = stringResource(R.string.update_book_notes))
        TextField(
            value = noteInput,
            onValueChange = onNoteInputChanged,
            textStyle = AppTheme.typography.bodyLarge.copy(color = Ink),
            shape = FieldShape,
            colors = TextFieldDefaults.colors(
                focusedTextColor = Ink,
                unfocusedTextColor = Ink,
                cursorColor = Ink,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ),
            minLines = 5,
            supportingText = characterCounter(noteInput.length, TextLimits.BOOK_NOTES),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        )
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        color = Muted,
        style = AppTheme.typography.labelLarge
    )
}

@Composable
private fun FormButton(
    text: String,
    container: Color,
    content: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    border: Color? = null
) {
    Text(
        text = text,
        color = content,
        style = AppTheme.typography.labelLarge,
        textAlign = TextAlign.Center,
        modifier = modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .then(
                if (border != null) Modifier.border(1.dp, border, CircleShape) else Modifier
            )
            .background(container)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp)
    )
}

@Composable
private fun BookStatus.shelfLabel(): String = when (this) {
    BookStatus.LIBRARY -> stringResource(R.string.home_filter_want_to_read)
    BookStatus.READING -> stringResource(R.string.home_filter_reading)
    BookStatus.FINISHED -> stringResource(R.string.home_filter_finished)
}
