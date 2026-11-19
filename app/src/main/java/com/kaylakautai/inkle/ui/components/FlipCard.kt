package com.kaylakautai.inkle.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sign

/** Faster than this (degrees a second) and letting go flips the card, however far it turned. */
private const val FlingDegreesPerSecond = 400f

/** Which side of a [FlipCard] is up, and turning it over. */
@Stable
class FlipCardState internal constructor(showingBack: Boolean) {

    /** The card's turn in degrees when not being dragged: 0 is the front, 180 the back. */
    private val rotation = Animatable(if (showingBack) 180f else 0f)

    /**
     * Where a drag has turned the card to, while it's held. Plain state rather than the
     * animation, so each drag step lands at once and none can run after letting go (which
     * would cut the settling animation short).
     */
    private var dragAngle by mutableStateOf<Float?>(null)

    /** The side the card was resting on when the current drag began. */
    private var restAtDragStart = 0f

    /** How far the card is turned now, in degrees. */
    val angle: Float
        get() = dragAngle ?: rotation.value

    val showingBack: Boolean
        get() = normalised(angle) in 90f..270f

    /** Turns the card over to its other side. */
    suspend fun flip() = settleTo(restingAngle() + 180f)

    suspend fun showBack() {
        if (!showingBack) flip()
    }

    internal fun startDrag() {
        restAtDragStart = restingAngle()
        dragAngle = rotation.value
    }

    /** Follows a drag, up to a full half turn either way from where it started. */
    internal fun dragBy(degrees: Float) {
        val turned = angle + degrees
        dragAngle = turned.coerceIn(restAtDragStart - 180f, restAtDragStart + 180f)
    }

    /**
     * Lets go: a quick flick turns the card over in its direction, otherwise it settles on
     * whichever side is facing more.
     */
    internal suspend fun settle(degreesPerSecond: Float) {
        // Hand over from the drag to the animation where the finger left it.
        rotation.snapTo(angle)
        dragAngle = null
        val turned = rotation.value - restAtDragStart
        val target = when {
            abs(degreesPerSecond) > FlingDegreesPerSecond ->
                restAtDragStart + 180f * sign(degreesPerSecond)
            abs(turned) > 90f -> restAtDragStart + 180f * sign(turned)
            else -> restAtDragStart
        }
        settleTo(target)
    }

    private suspend fun settleTo(target: Float) {
        rotation.animateTo(
            target,
            spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessMediumLow)
        )
        // Keep it at 0 or 180, so the next turn starts from a known side.
        rotation.snapTo(normalised(target))
    }

    private fun restingAngle(): Float = (rotation.value / 180f).roundToInt() * 180f

    private fun normalised(degrees: Float): Float = ((degrees % 360f) + 360f) % 360f
}

@Composable
fun rememberFlipCardState(showingBack: Boolean = false): FlipCardState =
    remember { FlipCardState(showingBack) }

/**
 * A card with two sides: drag it sideways to turn it over in 3D, or tap it. The card takes
 * the [front]'s size; the [back] is drawn to fill the same space. Only the side facing up can
 * be tapped or read by accessibility services.
 *
 * @param showBackLabel / [showFrontLabel] say what tapping does, for accessibility services.
 * @param onFlip called after the user turns the card (not when [state] is turned in code).
 */
@Composable
fun FlipCard(
    state: FlipCardState,
    showBackLabel: String,
    showFrontLabel: String,
    modifier: Modifier = Modifier,
    onFlip: () -> Unit = {},
    front: @Composable () -> Unit,
    back: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    var widthPx by remember { mutableIntStateOf(1) }
    // Dragging across most of the card's width turns it over.
    val degreesPerPx = 180f / (widthPx * 0.8f).coerceAtLeast(1f)
    val showingBack by remember { derivedStateOf { state.showingBack } }
    val draggableState = rememberDraggableState { dx -> state.dragBy(dx * degreesPerPx) }

    Box(
        modifier = modifier
            .onSizeChanged { widthPx = it.width }
            // Touches are read before the turn is applied: inside it, a card turned near
            // edge-on would squash the finger's movement and the drag would stall.
            .draggable(
                state = draggableState,
                orientation = Orientation.Horizontal,
                onDragStarted = { state.startDrag() },
                onDragStopped = { velocity ->
                    state.settle(velocity * degreesPerPx)
                    onFlip()
                }
            )
            .clickable(
                onClickLabel = if (showingBack) showFrontLabel else showBackLabel,
                role = Role.Button
            ) {
                scope.launch {
                    state.flip()
                    onFlip()
                }
            }
            .graphicsLayer {
                rotationY = state.angle
                // Further than the default, so the near edge doesn't loom while turning.
                cameraDistance = 16f * density.density
            }
    ) {
        Box(
            modifier = Modifier
                .graphicsLayer { alpha = if (showingBack) 0f else 1f }
                .then(if (showingBack) Modifier.clearAndSetSemantics {} else Modifier)
        ) {
            front()
        }
        if (showingBack) {
            // Turned the other way, so it reads correctly once the card has turned over.
            Box(modifier = Modifier.matchParentSize().graphicsLayer { rotationY = 180f }) {
                back()
            }
        }
    }
}
