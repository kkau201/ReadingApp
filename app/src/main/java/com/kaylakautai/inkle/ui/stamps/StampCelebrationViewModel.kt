package com.kaylakautai.inkle.ui.stamps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaylakautai.inkle.model.DEFAULT_READING_GOAL
import com.kaylakautai.inkle.model.MBook
import com.kaylakautai.inkle.repo.FireRepository
import com.kaylakautai.inkle.ui.components.shelfKey
import com.kaylakautai.inkle.ui.user.finishedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Calendar
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Pops up the stamp card when the user finishes a book this year (see [StampCelebrations]).
 * Scoped to the activity, so it shows over whichever screen they finished it on.
 */
@HiltViewModel
class StampCelebrationViewModel @Inject constructor(
    private val celebrations: StampCelebrations,
    fireRepository: FireRepository
) : ViewModel() {

    /** The id of the book whose stamp is landing, while the card is up. */
    val landingBookId: StateFlow<String?>
        field = MutableStateFlow<String?>(null)

    /** The shelf, kept current, to draw the card from. */
    val books: StateFlow<List<MBook>> = fireRepository.savedBooks

    val readingGoal: StateFlow<Int> = fireRepository.user
        .map { it?.readingGoal ?: DEFAULT_READING_GOAL }
        .stateIn(viewModelScope, SharingStarted.Eagerly, DEFAULT_READING_GOAL)

    /** This year's reward for reaching the goal, named on the card. */
    val reward: StateFlow<String?> = fireRepository.user
        .map { it?.rewardFor(Calendar.getInstance().get(Calendar.YEAR)) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /** How long to wait for the finished book to show on the shelf before giving up. */
    internal var shelfWait: Duration = 5.seconds

    init {
        viewModelScope.launch {
            celebrations.pending.collectLatest { shelfKey ->
                if (shelfKey == null) {
                    landingBookId.value = null
                    return@collectLatest
                }
                // The save updates the shelf a moment later (at once, from the device cache).
                val finished = withTimeoutOrNull(shelfWait) {
                    books.first { shelf -> shelf.finishedThisYear(shelfKey) != null }
                }?.finishedThisYear(shelfKey)
                if (finished == null) celebrations.done() else landingBookId.value = finished.id
            }
        }
    }

    /** The card was closed, or there turned out to be nothing to land. */
    fun onDismiss() {
        celebrations.done()
    }

    private fun List<MBook>.finishedThisYear(shelfKey: String): MBook? =
        finishedIn(Calendar.getInstance().get(Calendar.YEAR))
            .firstOrNull { it.shelfKey() == shelfKey && it.id != null }
}
