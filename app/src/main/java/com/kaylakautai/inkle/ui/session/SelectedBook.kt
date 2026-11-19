package com.kaylakautai.inkle.ui.session

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kaylakautai.inkle.R
import com.kaylakautai.inkle.model.MBook
import com.kaylakautai.inkle.model.MSession
import com.kaylakautai.inkle.ui.components.InkleButton
import com.kaylakautai.inkle.ui.details.update.parseFinishedOn
import com.kaylakautai.inkle.ui.details.update.readableLabel
import com.kaylakautai.inkle.ui.theme.Cover
import com.kaylakautai.inkle.ui.theme.Ink
import com.kaylakautai.inkle.ui.theme.Muted

/** The carousel's selected book: its progress, the log and finish actions, and its journal. */
@Composable
internal fun SelectedBook(
    book: MBook,
    sessions: List<MSession>,
    onStartLog: () -> Unit,
    onMarkFinished: () -> Unit,
    onRemove: (MSession) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        BookHeading(book)
        PagesRead(logged = sessions.sumOf { it.pages ?: 0 }, total = book.pageCount?.takeIf { it > 0 })
        Spacer(modifier = Modifier.height(12.dp))
        InkleButton(
            text = stringResource(R.string.session_log),
            onClick = onStartLog
        )
        Text(
            text = stringResource(R.string.session_finish_confirm),
            color = Muted,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier
                .clip(CircleShape)
                .clickable(onClick = onMarkFinished)
                // A comfortable tap target around the short label.
                .padding(horizontal = 16.dp, vertical = 12.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        SessionJournal(sessions = sessions, onRemove = onRemove)
    }
}

@Composable
private fun BookHeading(book: MBook) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = book.title.orEmpty(),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        book.authors?.filter { it.isNotBlank() }?.takeIf { it.isNotEmpty() }?.let { authors ->
            Text(
                text = stringResource(R.string.session_author, authors.joinToString()),
                color = Muted,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

/** A progress bar when the page count is known, otherwise whichever count there is. */
@Composable
private fun PagesRead(logged: Int, total: Int?) {
    when {
        total != null && logged > 0 -> BookProgress(logged = logged, total = total)
        total != null -> Text(
            text = stringResource(R.string.home_pages, total),
            color = Muted,
            style = MaterialTheme.typography.bodyLarge
        )
        logged > 0 -> Text(
            text = pluralStringResource(R.plurals.session_pages_logged, logged, logged),
            color = Muted,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun BookProgress(logged: Int, total: Int) {
    val fraction = (logged.toFloat() / total).coerceIn(0f, 1f)
    val percent = fraction * 100f
    Column(
        modifier = Modifier.fillMaxWidth(0.72f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.session_progress, percent),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleLarge
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(CircleShape)
                .background(Cover)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(4.dp)
                    .background(Ink)
            )
        }
        Text(
            text = stringResource(R.string.session_progress_of, logged, total),
            color = Muted,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

/** Every session logged for the book, newest first, or a hint when there are none. */
@Composable
private fun SessionJournal(sessions: List<MSession>, onRemove: (MSession) -> Unit) {
    if (sessions.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = stringResource(R.string.session_empty_title),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = stringResource(R.string.session_empty_body),
                color = Muted,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    } else {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.session_journal),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.fillMaxWidth()
            )
            sessions.forEach { session ->
                SessionCard(session = session, onRemove = { onRemove(session) })
            }
        }
    }
}

@Composable
private fun SessionCard(
    session: MSession,
    onRemove: () -> Unit
) {
    val date = session.readOn?.let { parseFinishedOn(it)?.readableLabel() } ?: session.readOn
    BorderedCard(spacing = 4.dp) {
        if (!date.isNullOrBlank()) {
            Text(
                text = date,
                color = Muted,
                style = MaterialTheme.typography.labelLarge
            )
        }
        Text(
            text = session.title.orEmpty(),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleLarge
        )
        session.pages?.let { pages ->
            Text(
                text = pluralStringResource(R.plurals.session_pages_read, pages, pages),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyLarge
            )
        }
        session.note?.takeIf { it.isNotBlank() }?.let { note ->
            Text(
                text = note,
                color = Muted,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        Text(
            text = stringResource(R.string.session_remove),
            color = Muted,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier
                .padding(top = 8.dp)
                .clickable(onClick = onRemove)
        )
    }
}
