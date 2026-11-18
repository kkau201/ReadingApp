package com.example.readingapp.ui.session

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.readingapp.R
import com.example.readingapp.model.MBook
import com.example.readingapp.model.MSession
import com.example.readingapp.model.TextLimits
import com.example.readingapp.ui.components.LibraryPageTitle
import com.example.readingapp.ui.components.PulledBook
import com.example.readingapp.ui.components.PulledHeight
import com.example.readingapp.ui.components.PulledWidth
import com.example.readingapp.ui.components.ReadingAppButton
import com.example.readingapp.ui.components.characterCounter
import com.example.readingapp.ui.components.shelfKey
import com.example.readingapp.ui.components.shelfSwatches
import com.example.readingapp.ui.details.update.FinishedDateField
import com.example.readingapp.ui.details.update.FinishedParts
import com.example.readingapp.ui.details.update.parseFinishedOn
import com.example.readingapp.ui.details.update.readableLabel
import com.example.readingapp.ui.details.update.toFinishedOn
import com.example.readingapp.ui.details.update.todaySessionParts
import com.example.readingapp.ui.theme.AppTheme
import com.example.readingapp.ui.theme.CardBackground
import com.example.readingapp.ui.theme.Cover
import com.example.readingapp.ui.theme.OnInk
import com.example.readingapp.ui.theme.Ink
import com.example.readingapp.ui.theme.Muted
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private val CardShape = RoundedCornerShape(20.dp)
private val FieldShape = RoundedCornerShape(20.dp)

@Composable
fun SessionContent(
    state: SessionUiState,
    onBookSelected: (String) -> Unit,
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
                    onBookSelected = onBookSelected
                )
                selected?.let { book ->
                    SelectedBook(
                        book = book,
                        sessions = state.sessions.filter { it.googleBookId == book.shelfId() },
                        onStartLog = onStartLog,
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
    }
}

/** Empty state when no book is marked as reading: the diver illustration plus a hint. */
@Composable
private fun NoReadingBooks(modifier: Modifier = Modifier) {
    // Fade in when first shown, so it eases in after loading instead of popping in.
    val visible = remember { MutableTransitionState(false).apply { targetState = true } }
    AnimatedVisibility(visibleState = visible, enter = fadeIn(), modifier = modifier) {
        NoReadingBooksContent()
    }
}

@Composable
private fun NoReadingBooksContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(R.drawable.illustration_dive_into_book),
            // Decorative: the text below says what this screen needs.
            contentDescription = null,
            colorFilter = ColorFilter.tint(Ink),
            modifier = Modifier.width(150.dp)
        )
        Text(
            text = stringResource(R.string.session_no_books_title),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 28.dp)
        )
        Text(
            text = stringResource(R.string.session_no_books),
            color = Muted,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .widthIn(max = 320.dp)
                .padding(top = 8.dp)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF7F4EF)
@Composable
private fun NoReadingBooksPreview() {
    NoReadingBooksContent(modifier = Modifier
        .fillMaxWidth()
        .padding(24.dp))
}

@Composable
private fun ReadingCarousel(
    books: List<MBook>,
    selectedBookId: String?,
    onBookSelected: (String) -> Unit
) {
    val start = books.indexOfFirst { it.shelfId() == selectedBookId }.coerceAtLeast(0)
    val pagerState = rememberPagerState(initialPage = start, pageCount = { books.size })
    val scope = rememberCoroutineScope()
    val swatches = shelfSwatches(books.map { it.shelfKey() })
    LaunchedEffect(pagerState, books) {
        snapshotFlow { pagerState.settledPage }.collect { page ->
            books.getOrNull(page)?.shelfId()?.let(onBookSelected)
        }
    }
    LaunchedEffect(selectedBookId, books) {
        val index = books.indexOfFirst { it.shelfId() == selectedBookId }
        if (index >= 0 && index != pagerState.currentPage && !pagerState.isScrollInProgress) {
            pagerState.scrollToPage(index)
        }
    }
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val side = ((maxWidth - PulledWidth) / 2).coerceAtLeast(0.dp)
        HorizontalPager(
            state = pagerState,
            pageSize = PageSize.Fixed(PulledWidth),
            contentPadding = PaddingValues(horizontal = side),
            pageSpacing = 16.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(PulledHeight + 16.dp)
        ) { page ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                PulledBook(
                    book = books[page],
                    cloth = swatches[page].cloth,
                    onClick = { scope.launch { pagerState.animateScrollToPage(page) } }
                )
            }
        }
    }
}

@Composable
private fun SelectedBook(
    book: MBook,
    sessions: List<MSession>,
    onStartLog: () -> Unit,
    onRemove: (MSession) -> Unit,
    modifier: Modifier = Modifier
) {
    val logged = sessions.sumOf { it.pages ?: 0 }
    val total = book.pageCount?.takeIf { it > 0 }
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
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
        ReadingAppButton(
            text = stringResource(R.string.session_log),
            onClick = onStartLog
        )
        Spacer(modifier = Modifier.height(12.dp))
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
}

@Composable
private fun BookProgress(logged: Int, total: Int) {
    val percent = ((logged * 100f) / total).roundToInt().coerceIn(0, 100)
    val fraction = (logged.toFloat() / total).coerceIn(0f, 1f)
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

@Composable
private fun SessionLogDialog(
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
    Dialog(
        onDismissRequest = onCancel,
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
                LogSessionCard(
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
    }
}

@Composable
private fun LogSessionCard(
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .border(1.dp, Cover, CardShape)
            .background(CardBackground)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
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
        ReadingAppButton(
            text = stringResource(R.string.update_save),
            onClick = onSave
        )
        Text(
            text = stringResource(R.string.update_cancel),
            color = Muted,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier
                .clickable(onClick = onCancel)
                .align(Alignment.CenterHorizontally)
        )
    }
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
        if (!state.changingDate) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = dateLabel,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = stringResource(R.string.session_change_day),
                    color = Muted,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.clickable(onClick = onChangeDay)
                )
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = dateLabel,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = stringResource(R.string.session_use_today),
                    color = Muted,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.clickable(onClick = onUseToday)
                )
            }
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
private fun SessionCard(
    session: MSession,
    onRemove: () -> Unit
) {
    val date = session.readOn?.let { parseFinishedOn(it)?.readableLabel() } ?: session.readOn
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .border(1.dp, Cover, CardShape)
            .background(CardBackground)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
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

@Composable
private fun NumberField(
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
private fun NoteField(
    value: String,
    onValueChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        FieldLabel(text = stringResource(R.string.session_note))
        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = stringResource(R.string.session_note_hint),
                    color = Muted,
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onBackground),
            minLines = 2,
            supportingText = characterCounter(value.length, TextLimits.SESSION_NOTE),
            shape = FieldShape,
            colors = sessionFieldColors(),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Cover, FieldShape)
        )
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

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        color = Muted,
        style = MaterialTheme.typography.labelLarge
    )
}

@Composable
private fun FieldError(text: String) {
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

private fun MBook.shelfId(): String? = googleBookId ?: id
