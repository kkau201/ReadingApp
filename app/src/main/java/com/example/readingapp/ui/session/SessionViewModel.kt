package com.example.readingapp.ui.session

import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.viewModelScope
import com.example.readingapp.R
import com.example.readingapp.common.BaseViewModel
import com.example.readingapp.common.DependencyContextWrapper
import com.example.readingapp.common.ErrorType
import com.example.readingapp.model.MBook
import com.example.readingapp.model.MSession
import com.example.readingapp.model.TextLimits
import com.example.readingapp.model.limitTo
import com.example.readingapp.repo.FireRepository
import com.example.readingapp.ui.components.BookStatus
import com.example.readingapp.ui.components.DialogState
import com.example.readingapp.ui.details.update.FinishedParts
import com.example.readingapp.ui.details.update.MIN_FINISHED_YEAR
import com.example.readingapp.ui.details.update.maxFinishedYear
import com.example.readingapp.ui.details.update.toFinishedOn
import com.example.readingapp.ui.details.update.todaySessionParts
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val fireRepository: FireRepository,
    dependencyContextWrapper: DependencyContextWrapper
) : BaseViewModel(dependencyContextWrapper), DefaultLifecycleObserver {

    val uiState: StateFlow<SessionUiState>
        field = MutableStateFlow(SessionUiState().withToday())

    fun onLoad() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                    ?: throw ErrorType.UnknownUserException
                val books = fireRepository.fetchSavedBooksByUser(userId).getOrThrow()
                    .filter { it.bookStatus == BookStatus.READING }
                    .sortedBy { it.title.orEmpty() }
                val sessions = fireRepository.fetchSessions(userId)
                withContext(Dispatchers.Main) {
                    uiState.update { state ->
                        val selected = state.selectedBookId?.takeIf { id ->
                            books.any { it.shelfId() == id }
                        } ?: books.firstOrNull()?.shelfId()
                        state.copy(
                            books = books,
                            sessions = sessions,
                            selectedBookId = selected
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("Error", "onLoad: Error loading sessions", e)
                withContext(Dispatchers.Main) {
                    showToast(getString(R.string.session_error))
                }
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
                pagesError = false,
                dateError = false,
                selectedBookId = state.selectedBookId?.takeIf { id ->
                    state.books.any { it.shelfId() == id }
                } ?: state.books.firstOrNull()?.shelfId()
            )
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
                pagesError = false,
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
        uiState.update { it.copy(selectedBookId = bookId, bookError = false) }
    }

    fun onPagesChanged(input: String) {
        uiState.update {
            it.copy(pagesInput = input.filter(Char::isDigit).take(5), pagesError = false)
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

    fun onSaveSession() {
        val state = uiState.value
        val book = state.books.find { it.shelfId() == state.selectedBookId }
        val pages = state.pagesInput.toIntOrNull()?.takeIf { it > 0 }
        val readOn = FinishedParts(state.year, state.month, state.day).toFinishedOn()
            ?.takeIf { it.day != null }
        if (book == null || pages == null || readOn == null) {
            uiState.update {
                it.copy(
                    bookError = book == null,
                    pagesError = pages == null,
                    dateError = readOn == null
                )
            }
            return
        }
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val session = MSession(
            userId = userId,
            googleBookId = book.googleBookId ?: book.id,
            title = book.title,
            pages = pages,
            note = state.noteInput.trim().takeIf { it.isNotEmpty() },
            readOn = readOn.display,
            readAt = readOn.toTimestamp()
        )
        viewModelScope.launch(Dispatchers.IO) {
            try {
                fireRepository.addSession(session)
                val sessions = fireRepository.fetchSessions(userId)
                withContext(Dispatchers.Main) {
                    uiState.update {
                        it.withToday().copy(
                            sessions = sessions,
                            logging = false,
                            changingDate = false,
                            pagesInput = "",
                            noteInput = "",
                            bookError = false,
                            pagesError = false,
                            dateError = false
                        )
                    }
                    showToast(getString(R.string.session_saved))
                }
            } catch (e: Exception) {
                Log.e("Error", "onSaveSession: Error saving session", e)
                showToast(getString(R.string.session_error))
            }
        }
    }

    fun onRemoveSession(session: MSession) {
        val sessionId = session.id ?: return
        showDialog(
            DialogState(
                title = getString(R.string.session_remove_title),
                message = getString(R.string.session_remove_message),
                primaryButtonText = getString(R.string.session_remove),
                secondaryButtonText = getString(R.string.update_cancel),
                onPrimaryClick = {
                    dismissDialog()
                    deleteSession(sessionId)
                },
                onSecondaryClick = { dismissDialog() },
                onDismissRequest = { dismissDialog() }
            )
        )
    }

    private fun deleteSession(sessionId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                fireRepository.deleteSession(sessionId)
                val sessions = fireRepository.fetchSessions(userId)
                withContext(Dispatchers.Main) {
                    uiState.update { it.copy(sessions = sessions) }
                    showToast(getString(R.string.session_removed))
                }
            } catch (e: Exception) {
                Log.e("Error", "deleteSession: Error removing session", e)
                showToast(getString(R.string.session_error))
            }
        }
    }
}

private fun MBook.shelfId(): String? = googleBookId ?: id

private fun SessionUiState.withToday(): SessionUiState {
    val today = todaySessionParts()
    return copy(year = today.year, month = today.month, day = today.day)
}
