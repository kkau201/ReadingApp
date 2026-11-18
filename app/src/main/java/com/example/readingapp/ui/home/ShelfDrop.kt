package com.example.readingapp.ui.home

import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

// Books dropping onto the shelf. Tune the feel here.
/** How far above the shelf a book starts its drop. */
internal val DropDistance = 56.dp
/** Gap between books landing, the first time the shelf fills in. */
internal const val DropStaggerMillis = 100L
private val DropSpring = spring<Float>(
    dampingRatio = Spring.DampingRatioLowBouncy,
    stiffness = Spring.StiffnessMediumLow
)

/**
 * Books already shown on the shelf this app launch, so each drops in once: the shelf fills in
 * on first open, and later only newly saved books drop in (not every return to Home).
 */
internal object ShelfArrivals {
    private val seen = mutableSetOf<String>()
    val noneYet: Boolean get() = seen.isEmpty()
    fun isNew(key: String) = key !in seen
    fun markSeen(key: String) {
        seen += key
    }
}

/**
 * 1 while a book is still above the shelf, settling to 0 once it has landed. Books already
 * shown this launch start at 0, so they don't drop again.
 */
@Composable
internal fun rememberShelfDrop(
    key: String,
    delayMillis: Long,
    animate: Boolean
): Animatable<Float, AnimationVector1D> {
    val drop = remember(key) { Animatable(if (animate && ShelfArrivals.isNew(key)) 1f else 0f) }
    LaunchedEffect(key) {
        ShelfArrivals.markSeen(key)
        if (drop.value > 0f) {
            delay(delayMillis.milliseconds)
            drop.animateTo(0f, DropSpring)
        }
    }
    return drop
}

/** Off when the user has turned animations off (Accessibility), and in previews. */
@Composable
internal fun rememberShelfMotionEnabled(): Boolean {
    val context = LocalContext.current
    val inPreview = LocalInspectionMode.current
    return remember(context, inPreview) {
        !inPreview && Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f
        ) != 0f
    }
}
