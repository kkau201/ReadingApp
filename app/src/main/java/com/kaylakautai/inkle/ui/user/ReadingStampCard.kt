package com.kaylakautai.inkle.ui.user

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.kaylakautai.inkle.R
import com.kaylakautai.inkle.model.MBook
import com.kaylakautai.inkle.model.asReadingGoal
import com.kaylakautai.inkle.ui.components.OutlineStar
import com.kaylakautai.inkle.ui.components.rememberMotionEnabled
// This card is always yellow, so its text uses the fixed (not theme-aware) colours.
import com.kaylakautai.inkle.ui.theme.InkBase
import com.kaylakautai.inkle.ui.theme.MutedBase
import com.kaylakautai.inkle.ui.theme.Yellow
import com.google.firebase.Timestamp
import kotlinx.coroutines.delay
import java.util.Calendar
import kotlin.time.Duration.Companion.milliseconds

private const val StampColumns = 4
private val CardShape = RoundedCornerShape(20.dp)

// A newly finished book's stamp pressing onto the card. Tune the feel here.
/** How much bigger than its slot a stamp starts, held above the card. */
private const val StampLiftScale = 2.2f
/** Squashed slightly as it presses down, before springing back to size. */
private const val StampPressScale = 0.88f
/** Tilt at the start, straightening as it comes down. */
private const val StampLiftDegrees = -14f
/** Pause before the first stamp, so the empty slot is seen first. */
private const val StampFirstDelayMillis = 450L
/** Gap between stamps when more than one is new. */
private const val StampStaggerMillis = 350L

/**
 * This year's finished books against the reading goal, one stamp each. Stamps for books
 * finished since the card was last seen press onto the card, filling the next empty slots
 * in order. [ready] says the books have loaded, so which stamps are new can be worked out.
 */
@Composable
fun ReadingStampCard(
    books: List<MBook>,
    goal: Int,
    modifier: Modifier = Modifier,
    ready: Boolean = true
) {
    val year = Calendar.getInstance().get(Calendar.YEAR)
    val stampGoal = goal.asReadingGoal()
    val finished = books.finishedIn(year)
    val stamped = finished.take(stampGoal)
    val newStamps = rememberNewStamps(stamped = stamped.mapNotNull { it.id }, ready = ready)
    val animate = rememberMotionEnabled()
    val filled = stamped.size
    val extra = (finished.size - stampGoal).coerceAtLeast(0)
    val complete = filled == stampGoal
    val description = stringResource(R.string.profile_stamp_progress, filled, stampGoal)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(Yellow)
            .semantics { contentDescription = description }
            .padding(horizontal = 20.dp, vertical = 22.dp)
    ) {
        Text(
            text = year.toString(),
            color = InkBase,
            style = MaterialTheme.typography.labelSmall
        )
        Text(
            text = stringResource(R.string.profile_stamp_title),
            color = InkBase,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(top = 4.dp)
        )
        Text(
            text = if (complete) {
                stringResource(R.string.profile_stamp_complete)
            } else {
                pluralStringResource(R.plurals.profile_stamp_goal, stampGoal, stampGoal)
            },
            color = InkBase,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            (0 until stampGoal).chunked(StampColumns).forEach { indexes ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    indexes.forEach { index ->
                        val landingOrder = stampLandingOrder(index, filled, newStamps.size)
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            ReadingStamp(
                                stamped = index < filled,
                                landing = animate && landingOrder != null,
                                delayMillis = StampFirstDelayMillis +
                                    (landingOrder ?: 0) * StampStaggerMillis
                            )
                        }
                    }
                    repeat(StampColumns - indexes.size) {
                        Box(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
        Text(
            text = description,
            color = InkBase,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier
                .padding(top = 18.dp)
                .align(Alignment.End)
        )
        if (extra > 0) {
            Text(
                text = pluralStringResource(R.plurals.profile_stamp_more, extra, extra),
                color = MutedBase,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

/**
 * Where a slot's stamp comes in the landing sequence, or null if it isn't landing now.
 *
 * Stamps all look alike, so new ones always take the next empty slots, left to right, after
 * the [filled] - [newCount] already there. (By finish date, a book finished earlier in the
 * year would land between stamps already on the card.)
 */
internal fun stampLandingOrder(slot: Int, filled: Int, newCount: Int): Int? {
    val firstNew = filled - newCount
    return (slot - firstNew).takeIf { slot in firstNew until filled }
}

/**
 * One slot on the card: an outline star, with a stamp on it when [stamped]. When [landing],
 * the stamp presses down onto the slot after [delayMillis] instead of already being there.
 */
@Composable
private fun ReadingStamp(stamped: Boolean, landing: Boolean = false, delayMillis: Long = 0L) {
    Box(
        modifier = Modifier.size(56.dp),
        contentAlignment = Alignment.Center
    ) {
        // On the yellow card, so use the fixed ink colour in both themes.
        OutlineStar(color = InkBase.copy(alpha = 0.28f))
        if (stamped) {
            val scale = rememberStampPress(landing = landing, delayMillis = delayMillis)
            Image(
                painter = painterResource(R.drawable.blot_reading),
                contentDescription = null,
                modifier = Modifier
                    .size(52.dp)
                    .graphicsLayer {
                        // Read here (not in composition), so the press only redraws.
                        val s = scale.value
                        scaleX = s
                        scaleY = s
                        // Fully tilted and see-through when lifted, upright and solid once down.
                        val lifted = ((s - 1f) / (StampLiftScale - 1f)).coerceIn(0f, 1f)
                        rotationZ = lifted * StampLiftDegrees
                        alpha = 1f - lifted
                    }
            )
        }
    }
}

/**
 * The stamp's scale: held up at [StampLiftScale], then pressed down past full size with a
 * haptic tick, springing back to 1. Just 1 when not [landing].
 */
@Composable
private fun rememberStampPress(landing: Boolean, delayMillis: Long): Animatable<Float, AnimationVector1D> {
    val scale = remember(landing) { Animatable(if (landing) StampLiftScale else 1f) }
    val haptics = LocalHapticFeedback.current
    LaunchedEffect(landing) {
        if (!landing) return@LaunchedEffect
        delay(delayMillis.milliseconds)
        scale.animateTo(StampPressScale, tween(durationMillis = 220, easing = FastOutLinearInEasing))
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        scale.animateTo(
            1f,
            spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
        )
    }
    return scale
}

/** The books finished in [year], earliest first: the order they fill the stamp card. */
internal fun List<MBook>.finishedIn(year: Int): List<MBook> {
    return mapNotNull { book ->
        val finished = book.finishedReading ?: return@mapNotNull null
        if (finished.year() != year) return@mapNotNull null
        book
    }.sortedBy { it.finishedReading?.seconds }
}

/** The ids of this year's books that have a stamp on the card (up to the [goal]). */
internal fun List<MBook>.stampedThisYear(goal: Int): List<String> =
    finishedIn(Calendar.getInstance().get(Calendar.YEAR))
        .take(goal.asReadingGoal())
        .mapNotNull { it.id }

private fun Timestamp.year(): Int {
    return Calendar.getInstance().apply { time = toDate() }.get(Calendar.YEAR)
}
