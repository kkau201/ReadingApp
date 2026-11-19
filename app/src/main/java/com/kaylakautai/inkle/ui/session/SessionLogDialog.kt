package com.kaylakautai.inkle.ui.session

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kaylakautai.inkle.R
import com.kaylakautai.inkle.model.MBook
import com.kaylakautai.inkle.ui.components.InkleButton
import com.kaylakautai.inkle.ui.details.update.FinishedDateField
import com.kaylakautai.inkle.ui.details.update.FinishedParts
import com.kaylakautai.inkle.ui.details.update.readableLabel
import com.kaylakautai.inkle.ui.details.update.toFinishedOn
import com.kaylakautai.inkle.ui.details.update.todaySessionParts
import com.kaylakautai.inkle.ui.theme.CardBackground
import com.kaylakautai.inkle.ui.theme.Cover
import com.kaylakautai.inkle.ui.theme.Ink
import com.kaylakautai.inkle.ui.theme.Muted
import com.kaylakautai.inkle.ui.theme.OnInk

/** The "Log a session" form: which book, how many pages, an optional note and the day. */
@Composable
internal fun SessionLogDialog(
    state: SessionUiState,
    onBookSelected: (String) -> Unit,
    onPagesChanged: (String) -> Unit,
    onNoteChanged: (String) -> Unit,
    onYearChanged: (Int) -> Unit,
    onMonthChanged: (Int?) -> Unit,
    onDayChanged: (String) -> Unit,
    onChangeDay: () -> Unit,
    onUseToday: () -> Unit,
    onCancel: () -> Unit,
    onSave: () -> Unit
) {
    SessionFormDialog(onDismiss = onCancel) {
        LogSessionForm(
            state = state,
            onBookSelected = onBookSelected,
            onPagesChanged = onPagesChanged,
            onNoteChanged = onNoteChanged,
            onYearChanged = onYearChanged,
            onMonthChanged = onMonthChanged,
            onDayChanged = onDayChanged,
            onChangeDay = onChangeDay,
            onUseToday = onUseToday,
            onCancel = onCancel,
            onSave = onSave
        )
    }
}

@Composable
private fun ColumnScope.LogSessionForm(
    state: SessionUiState,
    onBookSelected: (String) -> Unit,
    onPagesChanged: (String) -> Unit,
    onNoteChanged: (String) -> Unit,
    onYearChanged: (Int) -> Unit,
    onMonthChanged: (Int?) -> Unit,
    onDayChanged: (String) -> Unit,
    onChangeDay: () -> Unit,
    onUseToday: () -> Unit,
    onCancel: () -> Unit,
    onSave: () -> Unit
) {
    Text(
        text = stringResource(R.string.session_log),
        color = MaterialTheme.colorScheme.onBackground,
        style = MaterialTheme.typography.headlineSmall
    )
    BookChoice(
        books = state.books,
        selectedBookId = state.selectedBookId,
        bookError = state.bookError,
        onBookSelected = onBookSelected
    )
    NumberField(
        label = stringResource(R.string.session_pages),
        value = state.pagesInput,
        placeholder = stringResource(R.string.session_pages_hint),
        error = if (state.pagesError) stringResource(R.string.session_pages_error) else null,
        onValueChange = onPagesChanged
    )
    NoteField(
        value = state.noteInput,
        onValueChange = onNoteChanged
    )
    SessionDay(
        state = state,
        onYearChanged = onYearChanged,
        onMonthChanged = onMonthChanged,
        onDayChanged = onDayChanged,
        onChangeDay = onChangeDay,
        onUseToday = onUseToday
    )
    InkleButton(
        text = stringResource(R.string.update_save),
        onClick = onSave
    )
    FormCancelLink(text = stringResource(R.string.update_cancel), onClick = onCancel)
}

@Composable
private fun BookChoice(
    books: List<MBook>,
    selectedBookId: String?,
    bookError: Boolean,
    onBookSelected: (String) -> Unit
) {
    val chosen = books.find { it.shelfId() == selectedBookId } ?: books.singleOrNull()
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        FieldLabel(text = stringResource(R.string.session_book))
        if (books.size <= 1 && chosen != null) {
            Text(
                text = chosen.title.orEmpty(),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyLarge
            )
        } else {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                books.forEach { book ->
                    val bookId = book.shelfId() ?: return@forEach
                    ChoiceChip(
                        text = book.title.orEmpty(),
                        selected = bookId == selectedBookId,
                        onClick = { onBookSelected(bookId) }
                    )
                }
            }
        }
        if (bookError) FieldError(text = stringResource(R.string.session_book_error))
    }
}

@Composable
private fun ChoiceChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Text(
        text = text,
        color = if (selected) OnInk else Ink,
        style = MaterialTheme.typography.labelLarge,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .widthIn(max = 180.dp)
            .clip(CircleShape)
            .background(if (selected) Ink else CardBackground)
            .border(
                width = if (selected) 0.dp else 1.dp,
                color = Cover,
                shape = CircleShape
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    )
}

/** "Today", or the chosen day, with a link to change it and the date picker when changing. */
@Composable
private fun SessionDay(
    state: SessionUiState,
    onYearChanged: (Int) -> Unit,
    onMonthChanged: (Int?) -> Unit,
    onDayChanged: (String) -> Unit,
    onChangeDay: () -> Unit,
    onUseToday: () -> Unit
) {
    val today = todaySessionParts()
    val isToday = state.year == today.year && state.month == today.month && state.day == today.day
    val dateLabel = if (isToday) {
        stringResource(R.string.session_today)
    } else {
        FinishedParts(state.year, state.month, state.day).toFinishedOn()?.readableLabel()
            ?: stringResource(R.string.session_today)
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        DayRow(
            dateLabel = dateLabel,
            action = stringResource(
                if (state.changingDate) R.string.session_use_today else R.string.session_change_day
            ),
            onAction = if (state.changingDate) onUseToday else onChangeDay
        )
        if (state.changingDate) {
            FinishedDateField(
                year = state.year,
                month = state.month,
                day = state.day,
                dayError = state.dateError && state.month != null && state.day.isNotBlank(),
                onYearChanged = onYearChanged,
                onMonthChanged = onMonthChanged,
                onDayChanged = onDayChanged
            )
            if (state.dateError && (state.month == null || state.day.isBlank())) {
                FieldError(text = stringResource(R.string.session_date_error))
            }
        }
    }
}

@Composable
private fun DayRow(dateLabel: String, action: String, onAction: () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = dateLabel,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = action,
            color = Muted,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.clickable(onClick = onAction)
        )
    }
}
