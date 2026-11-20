package com.kaylakautai.inkle.ui.session

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kaylakautai.inkle.R
import com.kaylakautai.inkle.model.MSession
import com.kaylakautai.inkle.ui.components.LibraryPageTitle
import com.kaylakautai.inkle.ui.theme.AppTheme

/**
 * The Session screen: the books being read in a carousel, the selected one's progress and
 * journal, and the log and finish forms. Each part lives in its own file in this package.
 */
@Composable
fun SessionContent(
    state: SessionUiState,
    onBookSelected: (String) -> Unit,
    onBookClick: (String) -> Unit,
    onLogByChanged: (SessionLogBy) -> Unit,
    onPagesChanged: (String) -> Unit,
    onNoteChanged: (String) -> Unit,
    onYearChanged: (Int) -> Unit,
    onMonthChanged: (Int?) -> Unit,
    onDayChanged: (String) -> Unit,
    onStartLog: () -> Unit,
    onCancelLog: () -> Unit,
    onChangeDay: () -> Unit,
    onUseToday: () -> Unit,
    onSave: () -> Unit,
    onRemove: (MSession) -> Unit,
    onRetry: () -> Unit,
    onMarkFinished: () -> Unit,
    onFinishRatingChanged: (Int) -> Unit,
    onFinishNoteChanged: (String) -> Unit,
    onCancelFinish: () -> Unit,
    onConfirmFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selected =
        state.books.find { it.shelfId() == state.selectedBookId } ?: state.books.firstOrNull()
    Column(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 24.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            LibraryPageTitle(
                text = stringResource(R.string.session_title),
                modifier = Modifier.padding(horizontal = AppTheme.spacing.mdSpacing)
            )
            if (!state.booksLoaded) {
                // Still loading: show nothing rather than flashing the empty state.
                if (state.loadFailed) {
                    LoadErrorCard(
                        onRetry = onRetry,
                        modifier = Modifier.padding(horizontal = AppTheme.spacing.mdSpacing)
                    )
                }
            } else if (state.books.isEmpty()) {
                NoReadingBooks(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppTheme.spacing.mdSpacing)
                        .padding(top = 24.dp)
                )
            } else {
                ReadingCarousel(
                    books = state.books,
                    selectedBookId = state.selectedBookId,
                    onBookSelected = onBookSelected,
                    onBookClick = onBookClick
                )
                selected?.let { book ->
                    SelectedBook(
                        book = book,
                        sessions = state.sessions.filter { it.googleBookId == book.shelfId() },
                        onStartLog = onStartLog,
                        onMarkFinished = onMarkFinished,
                        onRemove = onRemove,
                        modifier = Modifier.padding(horizontal = AppTheme.spacing.mdSpacing)
                    )
                }
            }
        }
        if (state.logging && selected != null) {
            SessionLogDialog(
                state = state.copy(books = listOf(selected)),
                onBookSelected = onBookSelected,
                onLogByChanged = onLogByChanged,
                onPagesChanged = onPagesChanged,
                onNoteChanged = onNoteChanged,
                onYearChanged = onYearChanged,
                onMonthChanged = onMonthChanged,
                onDayChanged = onDayChanged,
                onChangeDay = onChangeDay,
                onUseToday = onUseToday,
                onCancel = onCancelLog,
                onSave = onSave
            )
        }
        state.finishing?.let { form ->
            FinishBookDialog(
                form = form,
                onRatingChanged = onFinishRatingChanged,
                onNoteChanged = onFinishNoteChanged,
                onCancel = onCancelFinish,
                onConfirm = onConfirmFinish
            )
        }
    }
}
