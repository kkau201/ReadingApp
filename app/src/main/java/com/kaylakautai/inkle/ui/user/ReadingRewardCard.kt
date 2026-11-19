package com.kaylakautai.inkle.ui.user

// Both sides keep fixed colours in both themes: the front is always yellow, the back ink.
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.kaylakautai.inkle.R
import com.kaylakautai.inkle.model.MBook
import com.kaylakautai.inkle.model.asReadingGoal
import com.kaylakautai.inkle.ui.components.FlipCard
import com.kaylakautai.inkle.ui.components.FlipCardState
import com.kaylakautai.inkle.ui.components.rememberFlipCardState
import com.kaylakautai.inkle.ui.theme.InkBase
import com.kaylakautai.inkle.ui.theme.PaperBase
import com.kaylakautai.inkle.ui.theme.Yellow
import java.util.Calendar

private val CardShape = RoundedCornerShape(20.dp)

/**
 * The stamp card, with the reward for reaching the goal on its back: swipe or tap to turn it
 * over. [reward] is this year's, or null if none is set; [onSetReward] (when given) is offered
 * on the back to set one.
 */
@Composable
fun StampAndRewardCard(
    books: List<MBook>,
    goal: Int,
    reward: String?,
    modifier: Modifier = Modifier,
    ready: Boolean = true,
    onSetReward: (() -> Unit)? = null,
    flipState: FlipCardState = rememberFlipCardState(),
    onFlip: () -> Unit = {}
) {
    val stamped = books.stampedThisYear(goal).size
    FlipCard(
        state = flipState,
        showBackLabel = stringResource(R.string.reward_flip_action_show),
        showFrontLabel = stringResource(R.string.reward_flip_action_hide),
        onFlip = onFlip,
        modifier = modifier,
        front = { ReadingStampCard(books = books, goal = goal, ready = ready) },
        back = {
            RewardSide(
                reward = reward,
                stamped = stamped,
                goal = goal.asReadingGoal(),
                onSetReward = onSetReward
            )
        }
    )
}

/** The back of the card: the reward and how close it is, or a nudge to set one. */
@Composable
private fun RewardSide(
    reward: String?,
    stamped: Int,
    goal: Int,
    onSetReward: (() -> Unit)?
) {
    val toGo = (goal - stamped).coerceAtLeast(0)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .clip(CardShape)
            .background(Yellow)
            .padding(horizontal = 20.dp, vertical = 22.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = Calendar.getInstance().get(Calendar.YEAR).toString(),
            color = InkBase.copy(alpha = 0.7f),
            style = MaterialTheme.typography.labelSmall
        )
        when {
            reward == null -> {
                Text(
                    text = stringResource(R.string.reward_none_title),
                    color = InkBase,
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = if (onSetReward != null) {
                        pluralStringResource(R.plurals.reward_none_body, goal, goal)
                    } else {
                        stringResource(R.string.reward_none_body_popup)
                    },
                    color = InkBase.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.weight(1f))
                onSetReward?.let { onClick ->
                    Text(
                        text = stringResource(R.string.reward_set),
                        color = PaperBase,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(InkBase)
                            .clickable(onClick = onClick)
                            .padding(horizontal = 18.dp, vertical = 10.dp)
                    )
                }
            }

            toGo == 0 -> {
                Text(
                    text = stringResource(R.string.reward_earned),
                    color = InkBase,
                    style = MaterialTheme.typography.headlineSmall
                )
                RewardText(reward)
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = stringResource(R.string.reward_earned_body),
                    color = InkBase.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            else -> {
                Text(
                    text = stringResource(R.string.reward_working_towards),
                    color = InkBase.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.labelLarge
                )
                RewardText(reward)
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = pluralStringResource(R.plurals.reward_books_to_go, toGo, toGo),
                    color = InkBase,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun RewardText(reward: String) {
    Text(
        text = reward,
        color = InkBase,
        style = MaterialTheme.typography.headlineMedium,
        maxLines = 4,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.padding(top = 8.dp)
    )
}

/**
 * Whether to show the hint that the card turns over: until the user first does it. Kept on
 * the device. Never shown in previews.
 */
@Composable
internal fun rememberRewardHint(): RewardHint {
    val context = LocalContext.current
    val inPreview = LocalInspectionMode.current
    return remember(context, inPreview) {
        val prefs = context.getSharedPreferences(StampLedger.PREFS_NAME, Context.MODE_PRIVATE)
        RewardHint(
            initiallyShown = !inPreview && !prefs.getBoolean(KEY_FLIPPED_ONCE, false),
            onSeen = { prefs.edit { putBoolean(KEY_FLIPPED_ONCE, true) } }
        )
    }
}

internal class RewardHint(initiallyShown: Boolean, private val onSeen: () -> Unit) {
    var shown by mutableStateOf(initiallyShown)
        private set

    /** The card was turned over, so the hint has done its job. */
    fun cardFlipped() {
        if (!shown) return
        shown = false
        onSeen()
    }
}

private const val KEY_FLIPPED_ONCE = "reward_card_flipped"

@Preview(showBackground = true, backgroundColor = 0xFFF7F4EF)
@Composable
private fun RewardSideWorkingTowardsPreview() {
    Column(Modifier.padding(24.dp)) {
        RewardSidePreviewBox { RewardSide("A weekend in Wellington", stamped = 3, goal = 8, onSetReward = {}) }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF7F4EF)
@Composable
private fun RewardSideEarnedPreview() {
    Column(Modifier.padding(24.dp)) {
        RewardSidePreviewBox { RewardSide("A weekend in Wellington", stamped = 8, goal = 8, onSetReward = {}) }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF7F4EF)
@Composable
private fun RewardSideNonePreview() {
    Column(Modifier.padding(24.dp)) {
        RewardSidePreviewBox { RewardSide(null, stamped = 3, goal = 8, onSetReward = {}) }
    }
}

/** The back fills the front card's size in the app; previews give it a typical card size. */
@Composable
private fun RewardSidePreviewBox(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    ) { content() }
}
