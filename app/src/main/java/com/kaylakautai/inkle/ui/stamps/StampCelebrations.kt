package com.kaylakautai.inkle.ui.stamps

import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Says when the user has just finished a book this year on this device, so the stamp card
 * can pop up with the new stamp pressing on. Screens that mark books finished report it here;
 * [StampCelebrationViewModel] shows the card.
 */
@Singleton
class StampCelebrations @Inject constructor() {

    /** The shelf key (Google Books id) of the book to celebrate, while there is one. */
    val pending: StateFlow<String?>
        field = MutableStateFlow<String?>(null)

    /**
     * The book with [shelfKey] was just marked finished on [finishedAt]. Only books finished
     * this year earn a stamp; a finish backdated to an earlier year is left alone.
     */
    fun bookFinished(shelfKey: String?, finishedAt: Timestamp?) {
        if (shelfKey.isNullOrBlank() || finishedAt == null) return
        if (finishedAt.year() != currentYear()) return
        pending.value = shelfKey
    }

    /** The card was shown (or couldn't be), so there's nothing left to celebrate. */
    fun done() {
        pending.value = null
    }

    private fun currentYear() = Calendar.getInstance().get(Calendar.YEAR)

    private fun Timestamp.year(): Int = Calendar.getInstance().apply { time = toDate() }.get(Calendar.YEAR)
}
