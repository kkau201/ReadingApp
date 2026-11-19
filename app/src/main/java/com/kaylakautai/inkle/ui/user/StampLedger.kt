package com.kaylakautai.inkle.ui.user

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.core.content.edit

/**
 * Which finished books have had their stamp shown landing on the stamp card. Kept on the
 * device, so each stamp lands once, however often Profile is opened.
 *
 * The first time the ledger is used, everything already stamped counts as seen, so a whole
 * card of stamps doesn't land at once.
 */
internal class StampLedger(private val prefs: SharedPreferences) {

    private val seen: MutableSet<String> =
        prefs.getStringSet(KEY_SEEN, null)?.toMutableSet() ?: mutableSetOf()

    /** The [stamped] book ids that haven't been shown landing yet, in the same order. */
    fun unseen(stamped: List<String>): List<String> {
        if (!prefs.contains(KEY_SEEN)) return emptyList()
        return stamped.filterNot { it in seen }
    }

    /**
     * The user has just finished [landing] on this device, so its stamp lands when the card is
     * shown, even if it landed before (finished, moved back to Reading, then finished again).
     * Before Profile has ever been opened, the stamps already on the card count as seen.
     * Returns false only when [landing] has no slot on the card (the goal is already full).
     */
    fun prepareToLand(stamped: List<String>, landing: String): Boolean {
        if (landing !in stamped) return false
        if (!prefs.contains(KEY_SEEN)) {
            markSeen(stamped - landing)
        } else if (seen.remove(landing)) {
            save()
        }
        return true
    }

    fun markSeen(stamped: Collection<String>) {
        if (prefs.contains(KEY_SEEN) && seen.containsAll(stamped)) return
        seen += stamped
        save()
    }

    private fun save() = prefs.edit { putStringSet(KEY_SEEN, seen.toSet()) }

    companion object {
        const val PREFS_NAME = "reading_stamps"
        private const val KEY_SEEN = "seen_stamps"
    }
}

/**
 * Whether [landing]'s stamp can land on the card, after getting the ledger ready for it (see
 * [StampLedger.prepareToLand]). Worked out once per [landing]. False in previews.
 */
@Composable
internal fun rememberStampToLand(stamped: List<String>, landing: String): Boolean {
    val ledger = rememberStampLedger() ?: return false
    return remember(landing) { ledger.prepareToLand(stamped, landing) }
}

@Composable
private fun rememberStampLedger(): StampLedger? {
    val context = LocalContext.current
    val inPreview = LocalInspectionMode.current
    return remember(context, inPreview) {
        if (inPreview) null
        else StampLedger(context.getSharedPreferences(StampLedger.PREFS_NAME, Context.MODE_PRIVATE))
    }
}

/**
 * The [stamped] book ids whose stamps should land now: new since they were last shown.
 * Nothing is decided until [ready] (the books have loaded), so a list that's empty only
 * because it's loading isn't taken as the user having no stamps. Always empty in previews.
 */
@Composable
internal fun rememberNewStamps(stamped: List<String>, ready: Boolean): List<String> {
    val ledger = rememberStampLedger()
    val new = remember(stamped, ready) {
        if (ledger == null || !ready) emptyList() else ledger.unseen(stamped)
    }
    LaunchedEffect(stamped, ready) {
        if (ready) ledger?.markSeen(stamped)
    }
    return new
}
