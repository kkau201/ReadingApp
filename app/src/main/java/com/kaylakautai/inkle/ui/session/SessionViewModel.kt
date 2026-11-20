package com.kaylakautai.inkle.ui.session

import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.viewModelScope
import com.kaylakautai.inkle.R
import com.kaylakautai.inkle.common.BaseViewModel
import com.kaylakautai.inkle.common.DependencyContextWrapper
import com.kaylakautai.inkle.common.ErrorType
import com.kaylakautai.inkle.coroutines.CoroutineDispatcherProvider
import com.kaylakautai.inkle.model.MBook
import com.kaylakautai.inkle.model.MSession
import com.kaylakautai.inkle.model.StoredBook
import com.kaylakautai.inkle.model.TextLimits
import com.kaylakautai.inkle.model.limitTo
import com.kaylakautai.inkle.auth.AuthRepository
import com.kaylakautai.inkle.repo.FireRepository
import com.kaylakautai.inkle.repo.PageUpdate
import com.kaylakautai.inkle.ui.model.BookStatus
import com.kaylakautai.inkle.ui.components.DialogState
import com.kaylakautai.inkle.ui.components.shelfKey
import com.kaylakautai.inkle.ui.stamps.StampCelebrations
import com.kaylakautai.inkle.ui.details.update.FinishedOn
import com.kaylakautai.inkle.ui.details.update.FinishedParts
import com.kaylakautai.inkle.ui.details.update.MIN_FINISHED_YEAR
import com.kaylakautai.inkle.ui.details.update.maxFinishedYear
import com.kaylakautai.inkle.ui.details.update.toFinishedOn
import com.kaylakautai.inkle.ui.details.update.todaySessionParts
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val fireRepository: FireRepository,
    private val authRepository: AuthRepository,
    private val dispatchers: CoroutineDispatcherProvider,
    private val celebrations: StampCelebrations,
    dependencyContextWrapper: DependencyContextWrapper
) : BaseViewModel(dependencyContextWrapper), DefaultLifecycleObserver {

    val uiState: StateFlow<SessionUiState>
        field = MutableStateFlow(SessionUiState().withToday())

    private var isWatching = false

    fun onLoad() {
        watchForChanges()
        uiState.update { it.copy(loadFailed = false) }
        viewModelScope.launch(dispatchers.getIo()) {
            try {
                val userId = authRepository.currentUserId
                    ?: throw ErrorType.UnknownUserException
                val books = fireRepository.fetchSavedBooksByUser(userId).getOrThrow()
                val sessions = fireRepository.fetchSessions(userId)
                withContext(dispatchers.getMain()) {
                    uiState.update { state ->
                        state.withShelf(books).copy(booksLoaded = true, sessions = sessions, loadFailed = false)
                    }
                }
            } catch (e: Exception) {
                Log.e("Error", "onLoad: Error loading sessions", e)
                withContext(dispatchers.getMain()) {
                    uiState.update { it.copy(loadFailed = true) }
                }
            }
        }
    }

    fun onRetryClick() = onLoad()

    /** Once loaded, keeps the journal and the books to pick from current as they change. */
    private fun watchForChanges() {
        if (isWatching) return
        isWatching = true
        viewModelScope.launch {
            fireRepository.savedBooks.collect { books ->
                uiState.update { if (it.booksLoaded) it.withShelf(books) else it }
            }
        }
        viewModelScope.launch {
            fireRepository.sessions.collect { sessions ->
                uiState.update { if (it.booksLoaded) it.copy(sessions = sessions) else it }
            }
        }
    }

    fun onStartLog() {
        uiState.update { state ->
            state.withToday().copy(
                logging = true,
                changingDate = false,
                pagesInput = "",
                noteInput = "",
                bookError = false,
                pagesError = null,
                dateError = false,
                selectedBookId = state.selectedBookId?.takeIf { id ->
                    state.books.any { it.shelfId() == id }
                } ?: state.books.firstOrNull()?.shelfId()
            ).withUsableLogBy()
        }
    }

    fun onCancelLog() {
        uiState.update {
            it.withToday().copy(
                logging = false,
                changingDate = false,
                pagesInput = "",
                noteInput = "",
                bookError = false,
                pagesError = null,
                dateError = false
            )
        }
    }

    fun onChangeDay() {
        uiState.update { it.copy(changingDate = true, dateError = false) }
    }

    fun onUseToday() {
        uiState.update { it.withToday().copy(changingDate = false, dateError = false) }
    }

    fun onBookSelected(bookId: String) {
        uiState.update {
            it.copy(selectedBookId = bookId, bookError = false, pagesError = null).withUsableLogBy()
        }
    }

    fun onLogByChanged(logBy: SessionLogBy) {
        uiState.update {
            it.copy(logBy = logBy, pagesInput = "", pagesError = null).withUsableLogBy()
        }
    }

    fun onPagesChanged(input: String) {
        uiState.update {
            it.copy(
                pagesInput = input.filter(Char::isDigit).take(it.logBy.maxDigits()),
                pagesError = null
            )
        }
    }

    fun onNoteChanged(input: String) {
        uiState.update { it.copy(noteInput = input.limitTo(TextLimits.SESSION_NOTE)) }
    }

    fun onYearChanged(year: Int) {
        uiState.update {
            it.copy(year = year.coerceIn(MIN_FINISHED_YEAR, maxFinishedYear()), dateError = false)
        }
    }

    fun onMonthChanged(month: Int?) {
        uiState.update { state ->
            state.copy(
                month = month,
                day = if (month == null) "" else state.day,
                dateError = false
            )
        }
    }

    fun onDayChanged(input: String) {
        uiState.update {
            it.copy(day = input.filter(Char::isDigit).take(2), dateError = false)
        }
    }

    /** True while a session is being saved, so a second tap doesn't log it twice. */
    private var saving = false

    fun onSaveSession() {
        if (saving) return
        val state = uiState.value
        val book = state.books.find { it.shelfId() == state.selectedBookId }
        val place = book?.readingPlace(state.sessions) ?: 0
        val entry = readingEntry(state.logBy, state.pagesInput, place, book?.pageCount)
        val readOn = FinishedParts(state.year, state.month, state.day).toFinishedOn()
            ?.takeIf { it.day != null }
        if (book == null || entry !is ReadingEntry.Valid || readOn == null) {
            uiState.update {
                it.copy(
                    bookError = book == null,
                    pagesError = (entry as? ReadingEntry.Invalid)?.error,
                    dateError = readOn == null
                )
            }
            return
        }
        val userId = authRepository.currentUserId ?: return
        val session = MSession(
            userId = userId,
            googleBookId = book.shelfId(),
            title = book.title,
            pages = entry.pages,
            endPage = entry.pageReached,
            note = state.noteInput.trim().takeIf { it.isNotEmpty() },
            readOn = readOn.display,
            readAt = readOn.toTimestamp()
        )
        // Saved with the session, so the book's progress can't fall out of step with it.
        val page = book.id?.let { PageUpdate(bookId = it, page = entry.pageReached) }
        saving = true
        viewModelScope.launch(dispatchers.getIo()) {
            try {
                val result = fireRepository.logSession(session, page)
                // Includes the new session even offline (Firestore's cache has it).
                val sessions = fireRepository.fetchSessions(userId)
                withContext(dispatchers.getMain()) {
                    uiState.update {
                        it.withToday().copy(
                            sessions = sessions,
                            logging = false,
                            changingDate = false,
                            pagesInput = "",
                            noteInput = "",
                            bookError = false,
                            pagesError = null,
                            dateError = false
                        )
                    }
                    showWriteToast(result, savedRes = R.string.session_saved)
                    if (book.reachedTheEnd(entry.pageReached)) offerToFinish(book, finishedOn = readOn)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("Error", "onSaveSession: Error saving session", e)
                withContext(dispatchers.getMain()) {
                    showToast(getString(R.string.session_error))
                }
            } finally {
                saving = false
            }
        }
    }

    /**
     * Every page has now been logged, so the book is probably done. Asks rather than marking it
     * straight away, as Google Books page counts don't always match the reader's edition.
     */
    private fun offerToFinish(book: MBook, finishedOn: FinishedOn) {
        confirmFinish(
            book = book,
            finishedOn = finishedOn,
            title = getString(R.string.session_finish_title),
            message = getString(R.string.session_finish_message, book.title.orEmpty()),
            cancelText = getString(R.string.session_finish_not_yet)
        )
    }

    /** The selected book is done, whatever has been logged. Finished today. */
    fun onMarkFinishedClick() {
        val state = uiState.value
        val book = state.books.find { it.shelfId() == state.selectedBookId } ?: return
        val today = todaySessionParts().toFinishedOn() ?: return
        confirmFinish(
            book = book,
            finishedOn = today,
            title = getString(R.string.session_mark_finished_title, book.title.orEmpty()),
            message = getString(R.string.session_mark_finished_message),
            cancelText = getString(R.string.update_cancel)
        )
    }

    /** The book and date the open finish form will save, while [SessionUiState.finishing] is set. */
    private var finishing: Pair<MBook, FinishedOn>? = null

    /** Opens the finish form, starting from the book's current rating and note. */
    private fun confirmFinish(
        book: MBook,
        finishedOn: FinishedOn,
        title: String,
        message: String,
        cancelText: String
    ) {
        if (book.id == null) return
        finishing = book to finishedOn
        uiState.update {
            it.copy(
                finishing = FinishForm(
                    title = title,
                    message = message,
                    cancelText = cancelText,
                    rating = book.rating?.toInt()?.takeIf { rating -> rating in 1..5 } ?: 0,
                    noteInput = book.notes.orEmpty()
                )
            )
        }
    }

    /** Tapping the chosen star again clears the rating, as it's optional. */
    fun onFinishRatingChanged(rating: Int) {
        uiState.update { state ->
            val form = state.finishing ?: return@update state
            state.copy(finishing = form.copy(rating = if (rating == form.rating) 0 else rating))
        }
    }

    fun onFinishNoteChanged(input: String) {
        uiState.update { state ->
            val form = state.finishing ?: return@update state
            state.copy(finishing = form.copy(noteInput = input.limitTo(TextLimits.BOOK_NOTES)))
        }
    }

    fun onCancelFinish() {
        finishing = null
        uiState.update { it.copy(finishing = null) }
    }

    fun onConfirmFinish() {
        val (book, finishedOn) = finishing ?: return
        val form = uiState.value.finishing ?: return
        onCancelFinish()
        markFinished(book, finishedOn, form)
    }

    /** The shelf listener then moves the book off this screen and onto the Finished shelf. */
    private fun markFinished(book: MBook, finishedOn: FinishedOn, form: FinishForm) {
        val bookId = book.id ?: return
        val updates = buildMap<String, Any?> {
            put(StoredBook.FINISHED_READING, finishedOn.toTimestamp())
            put(StoredBook.FINISHED_ON, finishedOn.display)
            // No rating chosen clears any old one, as when finishing from the book's Edit screen.
            put(StoredBook.RATING, form.rating.takeIf { it in 1..5 }?.toDouble())
            val note = form.noteInput.trim()
            if (note != book.notes.orEmpty().trim()) put(StoredBook.NOTES, note)
        }
        fireRepository.updateBook(
            bookId = bookId,
            bookUpdates = updates,
            onSaved = { result ->
                showWriteToast(result, savedRes = R.string.session_marked_finished)
                celebrations.bookFinished(book.shelfKey(), finishedOn.toTimestamp())
            },
            onError = { e ->
                Log.e("Error", "markFinished: Error updating book", e)
                showToast(getString(R.string.update_book_error))
            }
        )
    }

    fun onRemoveSession(session: MSession) {
        // Not saved yet (no id), so there's nothing to remove.
        if (session.id == null) return
        showDialog(
            DialogState(
                title = getString(R.string.session_remove_title),
                message = getString(R.string.session_remove_message),
                primaryButtonText = getString(R.string.session_remove),
                secondaryButtonText = getString(R.string.update_cancel),
                onPrimaryClick = {
                    dismissDialog()
                    deleteSession(session)
                },
                onSecondaryClick = { dismissDialog() },
                onDismissRequest = { dismissDialog() }
            )
        )
    }

    private fun deleteSession(session: MSession) {
        val sessionId = session.id ?: return
        val book = uiState.value.books.find { it.shelfId() == session.googleBookId }
        val userId = authRepository.currentUserId ?: return
        // Removed together, so the book's page and its journal stay in step.
        val page = book?.pageAfterRemoving(session)
        viewModelScope.launch(dispatchers.getIo()) {
            try {
                val result = fireRepository.deleteSession(sessionId, page)
                val sessions = fireRepository.fetchSessions(userId)
                withContext(dispatchers.getMain()) {
                    uiState.update { it.copy(sessions = sessions) }
                    showWriteToast(
                        result,
                        savedRes = R.string.session_removed,
                        offlineRes = R.string.removed_offline_toast
                    )
                }
            } catch (e: Exception) {
                Log.e("Error", "deleteSession: Error removing session", e)
                showToast(getString(R.string.session_error))
            }
        }
    }
}

/** Still being read, and [place] has reached a known page count. */
private fun MBook.reachedTheEnd(place: Int): Boolean {
    val total = pageCount?.takeIf { it > 0 } ?: return false
    return bookStatus == BookStatus.READING && place >= total
}

/** Percentages need the book's length: without it, fall back to pages read. */
private fun SessionUiState.withUsableLogBy(): SessionUiState {
    if (logBy != SessionLogBy.PERCENT) return this
    val book = books.find { it.shelfId() == selectedBookId }
    return if (book?.pageCount?.takeIf { it > 0 } != null) this else copy(logBy = SessionLogBy.PAGES)
}

/** Offers the books being read from [shelf], keeping the selection if it's still one of them. */
private fun SessionUiState.withShelf(shelf: List<MBook>): SessionUiState {
    val books = shelf
        .filter { it.bookStatus == BookStatus.READING }
        .sortedBy { it.title.orEmpty() }
    val selected = selectedBookId?.takeIf { id -> books.any { it.shelfId() == id } }
        ?: books.firstOrNull()?.shelfId()
    return copy(books = books, selectedBookId = selected)
}

private fun SessionUiState.withToday(): SessionUiState {
    val today = todaySessionParts()
    return copy(year = today.year, month = today.month, day = today.day)
}
