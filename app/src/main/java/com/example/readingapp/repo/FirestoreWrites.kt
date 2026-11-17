package com.example.readingapp.repo

import android.util.Log
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.seconds

/** How a Firestore write went, once the app can move on. */
enum class WriteResult {
    /** The server confirmed it. */
    SAVED,

    /** Saved on the device; Firestore sends it when the connection returns. */
    SAVED_OFFLINE
}

private const val TAG = "FirestoreWrites"

/** Online, but the server hasn't confirmed by now (e.g. weak signal): treat as saved offline. */
private val ConfirmTimeout = 5.seconds

/*
 * Firestore applies a write to its local cache straight away (later reads include it), but the
 * Task only completes once the server confirms, which never happens while offline. So offline,
 * report SAVED_OFFLINE at once instead of waiting forever.
 */

/** Waits for a write; throws if the server rejects it. */
internal suspend fun Task<*>.awaitWrite(online: Boolean): WriteResult {
    if (!online) return savedOffline()
    return withTimeoutOrNull(ConfirmTimeout) {
        await()
        WriteResult.SAVED
    } ?: savedOffline()
}

/** Callback version of [awaitWrite]: exactly one of [onSaved] or [onError] is called. */
internal fun Task<*>.onWriteDone(
    online: Boolean,
    onSaved: (WriteResult) -> Unit,
    onError: (Exception) -> Unit
) {
    if (!online) {
        onSaved(savedOffline())
        return
    }
    addOnCompleteListener { task ->
        if (task.isSuccessful) {
            onSaved(WriteResult.SAVED)
        } else {
            onError(task.exception ?: IllegalStateException("Firestore write failed"))
        }
    }
}

private fun Task<*>.savedOffline(): WriteResult {
    // Rare: rejected when it finally syncs (e.g. by firestore.rules). The user has moved on by
    // then, so just log it.
    addOnFailureListener { Log.w(TAG, "A write saved offline failed to sync", it) }
    return WriteResult.SAVED_OFFLINE
}
