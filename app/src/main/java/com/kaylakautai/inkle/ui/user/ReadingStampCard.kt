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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.Timestamp
import com.kaylakautai.inkle.R
import com.kaylakautai.inkle.model.MBook
import com.kaylakautai.inkle.model.asReadingGoal
import com.kaylakautai.inkle.ui.components.OutlineStar
import com.kaylakautai.inkle.ui.components.rememberMotionEnabled
import com.kaylakautai.inkle.ui.theme.AppTheme
// The card is green in both themes, so it uses the fixed (not theme-aware) colours.
import com.kaylakautai.inkle.ui.theme.Green
import com.kaylakautai.inkle.ui.theme.PaperBase
import kotlinx.coroutines.delay
import java.util.Calendar
import kotlin.math.ceil
import kotlin.math.min
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

/** A classic 3.5 × 2 inch business card, whatever the goal: the stamps shrink to fit. */
internal const val StampCardAspectRatio = 3.5f / 2f
internal val StampCardShape = RoundedCornerShape(16.dp)
internal val StampCardColor = Green
internal val StampCardInk = PaperBase
internal val StampCardPadding = 16.dp

/** Share of the card's width for the logo and words; the stamps get the rest. */
private const val TextColumnWeight = 0.44f

/** Gap between the copy column and the stamp grid in the side-by-side layout. */
private const val SideBySideGapDp = 12f

/**
 * Smallest stamp cell (dp) that still reads as a star with Blot on it. Below this, stack the
 * card so the stamps can use the full width.
 */
private const val MinReadableStampDp = 26f

/**
 * Goals up to this keep copy left / stamps right (the 6- and 9-star mockups). Larger goals
 * use the full-width stacked layout.
 */
internal const val MaxSideBySideGoal = 9

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
 * The front of the stamp card: logo, what the card is for, and one star per book in this
 * year's goal, stamped for each book finished. Names the [reward] for filling it (null if
 * none is set).
 *
 * Laid out side-by-side (copy left, stamps right) when that keeps the stars readable; stacks
 * centered when the goal is large or the card is narrow. Stamps for books finished since the
 * card was last seen press onto the card. [ready] says the books have loaded; [editable] adds
 * the note that the card turns over to change the goal.
 */
@Composable
fun ReadingStampCard(
    books: List<MBook>,
    goal: Int,
    reward: String?,
    modifier: Modifier = Modifier,
    ready: Boolean = true,
    editable: Boolean = false
) {
    val year = Calendar.getInstance().get(Calendar.YEAR)
    val stampGoal = goal.asReadingGoal()
    val finished = books.finishedIn(year)
    val stamped = finished.take(stampGoal)
    val newStamps = rememberNewStamps(stamped = stamped.mapNotNull { it.id }, ready = ready)
    val animate = rememberMotionEnabled()
    val filled = stamped.size
    val extra = (finished.size - stampGoal).coerceAtLeast(0)
    val goalText = goalLine(goal = stampGoal, reward = reward, complete = filled == stampGoal) +
        if (editable) " *" else ""

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(StampCardAspectRatio)
            .clip(StampCardShape)
            .background(StampCardColor)
            .padding(StampCardPadding)
    ) {
        val sideBySide = useSideBySideStampLayout(
            goal = stampGoal,
            width = maxWidth.value,
            height = maxHeight.value
        )
        if (sideBySide) {
            StampCardSideBySide(
                goalText = goalText,
                extra = extra,
                editable = editable,
                stampGoal = stampGoal,
                filled = filled,
                newCount = newStamps.size,
                animate = animate
            )
        } else {
            StampCardStacked(
                goalText = goalText,
                extra = extra,
                editable = editable,
                stampGoal = stampGoal,
                filled = filled,
                newCount = newStamps.size,
                animate = animate
            )
        }
    }
}

/** Copy on the left, stamps on the right — the mockups for smaller goals. */
@Composable
private fun StampCardSideBySide(
    goalText: String,
    extra: Int,
    editable: Boolean,
    stampGoal: Int,
    filled: Int,
    newCount: Int,
    animate: Boolean
) {
    Row(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(TextColumnWeight)
                .fillMaxHeight()
        ) {
            StampCardLogo()
            Text(
                text = stringResource(R.string.stamp_card_collect),
                color = StampCardInk,
                style = stampCardText(),
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 10.dp)
            )
            Text(
                text = goalText,
                color = StampCardInk,
                style = stampCardText(),
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .padding(top = 10.dp)
                    .weight(1f)
            )
            if (extra > 0) {
                Text(
                    text = pluralStringResource(R.plurals.profile_stamp_more, extra, extra),
                    color = StampCardInk,
                    style = stampCardNote(),
                    maxLines = 1,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            if (editable) {
                Text(
                    text = stringResource(R.string.stamp_card_flip_note),
                    color = StampCardInk.copy(alpha = 0.85f),
                    style = stampCardNote()
                )
            }
        }
        Spacer(modifier = Modifier.width(SideBySideGapDp.dp))
        StampGrid(
            goal = stampGoal,
            filled = filled,
            newCount = newCount,
            animate = animate,
            modifier = Modifier
                .weight(1f - TextColumnWeight)
                .fillMaxHeight()
        )
    }
}

/** Logo and copy above a full-width stamp grid — better when many stars need the width. */
@Composable
private fun StampCardStacked(
    goalText: String,
    extra: Int,
    editable: Boolean,
    stampGoal: Int,
    filled: Int,
    newCount: Int,
    animate: Boolean
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        StampCardLogo()
        Text(
            text = stringResource(R.string.stamp_card_collect),
            color = StampCardInk,
            style = stampCardText(),
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .padding(top = 6.dp)
                .fillMaxWidth()
        )
        Text(
            text = goalText,
            color = StampCardInk,
            style = stampCardText(),
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis,
            maxLines = 2,
            modifier = Modifier
                .padding(top = 4.dp)
                .fillMaxWidth()
        )
        StampGrid(
            goal = stampGoal,
            filled = filled,
            newCount = newCount,
            animate = animate,
            modifier = Modifier
                .padding(top = 8.dp)
                .weight(1f)
                .fillMaxWidth()
        )
        if (extra > 0) {
            Text(
                text = pluralStringResource(R.plurals.profile_stamp_more, extra, extra),
                color = StampCardInk,
                style = stampCardNote(),
                textAlign = TextAlign.Center,
                maxLines = 1,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        if (editable) {
            Text(
                text = stringResource(R.string.stamp_card_flip_note),
                color = StampCardInk.copy(alpha = 0.85f),
                style = stampCardNote(),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

/** What the goal is for: the reward if there is one, or that it's been reached. */
@Composable
private fun goalLine(goal: Int, reward: String?, complete: Boolean): String = when {
    complete && reward != null -> pluralStringResource(R.plurals.stamp_card_done_reward, goal, goal, reward)
    complete -> pluralStringResource(R.plurals.stamp_card_done, goal, goal)
    reward != null -> pluralStringResource(R.plurals.stamp_card_goal_reward, goal, goal, reward)
    else -> pluralStringResource(R.plurals.stamp_card_goal, goal, goal)
}

/**
 * One star per book in the [goal], [filled] of them stamped, the last [newCount] landing.
 * Laid out in whichever number of columns gives the biggest stars in the space.
 */
@Composable
private fun StampGrid(
    goal: Int,
    filled: Int,
    newCount: Int,
    animate: Boolean,
    modifier: Modifier = Modifier
) {
    val description = stringResource(R.string.profile_stamp_progress, filled, goal)
    BoxWithConstraints(
        modifier = modifier.clearAndSetSemantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        val columns = stampGridColumns(goal, maxWidth.value, maxHeight.value)
        val rows = ceil(goal / columns.toFloat()).toInt()
        val cell = min(maxWidth.value / columns, maxHeight.value / rows).dp
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            (0 until goal).chunked(columns).forEach { indexes ->
                Row {
                    indexes.forEach { index ->
                        val landingOrder = stampLandingOrder(index, filled, newCount)
                        ReadingStamp(
                            stamped = index < filled,
                            wobble = stampWobble(index),
                            landing = animate && landingOrder != null,
                            delayMillis = StampFirstDelayMillis + (landingOrder ?: 0) * StampStaggerMillis,
                            modifier = Modifier.size(cell)
                        )
                    }
                    // Short last row stays lined up with the ones above.
                    Spacer(modifier = Modifier.width(cell * (columns - indexes.size)))
                }
            }
        }
    }
}

/**
 * How many columns of stars fit [count] of them into a [width] × [height] space with the
 * stars as big as possible (each star gets a square cell). Fewer columns on a tie.
 */
internal fun stampGridColumns(count: Int, width: Float, height: Float): Int {
    if (count <= 1) return 1
    return (1..count).maxBy { columns ->
        val rows = ceil(count / columns.toFloat())
        min(width / columns, height / rows)
    }
}

/** Edge length of each star's square cell when [count] stars fill [width] × [height]. */
internal fun stampCellSize(count: Int, width: Float, height: Float): Float {
    if (count <= 0 || width <= 0f || height <= 0f) return 0f
    val columns = stampGridColumns(count, width, height)
    val rows = ceil(count / columns.toFloat())
    return min(width / columns, height / rows)
}

/**
 * Whether copy-left / stamps-right fits the mockups: goals up to [MaxSideBySideGoal] stay
 * split when stars stay readable; larger goals open to a full-width grid.
 */
internal fun useSideBySideStampLayout(
    goal: Int,
    width: Float,
    height: Float,
    textWeight: Float = TextColumnWeight,
    gap: Float = SideBySideGapDp,
    minCell: Float = MinReadableStampDp
): Boolean {
    if (goal > MaxSideBySideGoal) return false
    if (width <= 0f || height <= 0f) return true
    val sideStampWidth = (width * (1f - textWeight) - gap).coerceAtLeast(0f)
    return stampCellSize(goal, sideStampWidth, height) >= minCell
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

/** How far a stamp sits off-true: a turn, and a nudge as a share of its slot's size. */
internal data class StampWobble(val degrees: Float, val dx: Float, val dy: Float)

/** The furthest a stamp turns either way, and is nudged off-centre (share of its slot). */
private const val MaxWobbleDegrees = 14f
private const val MaxWobbleOffset = 0.07f

/**
 * How the stamp in [slot] sits, as if pressed by hand: some nearly straight, some turned or
 * a little off-centre. Worked out from the slot, so a stamp never moves once it's down.
 */
internal fun stampWobble(slot: Int): StampWobble {
    val random = Random(seed = slot * 7919 + 17)
    fun next() = random.nextFloat() * 2f - 1f
    return StampWobble(
        degrees = next() * MaxWobbleDegrees,
        dx = next() * MaxWobbleOffset,
        dy = next() * MaxWobbleOffset
    )
}

/**
 * One slot on the card: a paper star, inked with Blot when [stamped], sitting as [wobble]
 * says. When [landing], the stamp presses down onto the star after [delayMillis] instead of
 * already being there.
 */
@Composable
private fun ReadingStamp(
    stamped: Boolean,
    wobble: StampWobble,
    modifier: Modifier = Modifier,
    landing: Boolean = false,
    delayMillis: Long = 0L
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        OutlineStar(
            color = StampCardInk,
            filled = true,
            size = Dp.Unspecified,
            modifier = Modifier.fillMaxSize(0.94f)
        )
        if (stamped) {
            val scale = rememberStampPress(landing = landing, delayMillis = delayMillis)
            Image(
                painter = painterResource(R.drawable.blot_reading_stamp),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize(0.9f)
                    .graphicsLayer {
                        // Read here (not in composition), so the press only redraws.
                        val s = scale.value
                        scaleX = s
                        scaleY = s
                        // Fully tilted and see-through when lifted, settling at its wobble and
                        // solid once down.
                        val lifted = ((s - 1f) / (StampLiftScale - 1f)).coerceIn(0f, 1f)
                        rotationZ = wobble.degrees + lifted * StampLiftDegrees
                        translationX = wobble.dx * size.width
                        translationY = wobble.dy * size.height
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
private fun rememberStampPress(
    landing: Boolean,
    delayMillis: Long
): Animatable<Float, AnimationVector1D> {
    val scale = remember(landing) { Animatable(if (landing) StampLiftScale else 1f) }
    val haptics = LocalHapticFeedback.current
    LaunchedEffect(landing) {
        if (!landing) return@LaunchedEffect
        delay(delayMillis.milliseconds)
        scale.animateTo(
            StampPressScale,
            tween(durationMillis = 220, easing = FastOutLinearInEasing)
        )
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        scale.animateTo(
            1f,
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        )
    }
    return scale
}

/** The inkle logo in the card's colours. Decorative: the card's text says what it is. */
@Composable
internal fun StampCardLogo(modifier: Modifier = Modifier, height: Dp = 26.dp) {
    Image(
        painter = painterResource(R.drawable.inkle_logo_on_green),
        contentDescription = null,
        modifier = modifier
            .height(height)
            .widthIn(max = height * 3)
    )
}

/** The card's words: Nanum (serif), small enough for a business-card-sized card. */
@Composable
@ReadOnlyComposable
internal fun stampCardText(): TextStyle =
    MaterialTheme.typography.titleLarge.copy(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp
    )

/** The card's small print. */
@Composable
@ReadOnlyComposable
internal fun stampCardNote(): TextStyle =
    stampCardText().copy(fontSize = 10.sp, lineHeight = 13.sp)

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

@Preview(widthDp = 380, name = "Phone · 6 with reward")
@Preview(widthDp = 320, name = "Narrow · 6")
@Composable
private fun ReadingStampCardSixPreview() {
    val now = Timestamp.now()
    val books = (1..2).map { MBook(id = "b$it", finishedReading = now) }
    AppTheme {
        Box(Modifier.padding(16.dp)) {
            ReadingStampCard(books = books, goal = 6, reward = "a spa day", editable = true)
        }
    }
}

@Preview(widthDp = 380, name = "Phone · no reward")
@Composable
private fun ReadingStampCardNoRewardPreview() {
    val now = Timestamp.now()
    val books = (1..2).map { MBook(id = "b$it", finishedReading = now) }
    AppTheme {
        Box(Modifier.padding(16.dp)) {
            ReadingStampCard(books = books, goal = 6, reward = null, editable = true)
        }
    }
}

@Preview(widthDp = 380, name = "Phone · 10")
@Preview(widthDp = 380, name = "Phone · 14")
@Composable
private fun ReadingStampCardStackedPreview() {
    val now = Timestamp.now()
    val books = (1..2).map { MBook(id = "b$it", finishedReading = now) }
    AppTheme {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            ReadingStampCard(books = books, goal = 10, reward = "a spa day", editable = true)
            ReadingStampCard(books = books, goal = 14, reward = "a spa day", editable = true)
            ReadingStampCard(books = books, goal = 24, reward = null, editable = true)
        }
    }
}
