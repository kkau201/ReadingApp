package com.kaylakautai.inkle.ui.user

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kaylakautai.inkle.R
import com.kaylakautai.inkle.model.MAX_READING_GOAL
import com.kaylakautai.inkle.model.MBook
import com.kaylakautai.inkle.model.MIN_READING_GOAL
import com.kaylakautai.inkle.model.TextLimits
import com.kaylakautai.inkle.model.asReadingGoal
import com.kaylakautai.inkle.model.limitTo
import com.kaylakautai.inkle.ui.components.FlipCard
import com.kaylakautai.inkle.ui.components.InkleButton
import com.kaylakautai.inkle.ui.components.rememberFlipCardState
import com.kaylakautai.inkle.ui.theme.AppTheme
import com.kaylakautai.inkle.ui.theme.InkBase
import com.kaylakautai.inkle.ui.theme.MutedBase
import kotlinx.coroutines.launch

private val FieldShape = RoundedCornerShape(28.dp)

/** Below this content width, the edit side stacks instead of sitting side-by-side. */
private val EditSideBySideMinWidth = 300.dp

/** Shared height for the cream goal pill, reward field, and Update button. */
private val FieldHeight = 36.dp

/**
 * Top and bottom padding that makes the app's button [FieldHeight] tall, to match the fields:
 * (36 - 18dp label line) / 2.
 */
private val CompactButtonPadding = 9.dp

/** Space between the form's rows. */
private val FieldGap = 4.dp

/** At least this much space above Update, so it never touches the reward field. */
private val UpdateGap = 8.dp

/**
 * Content height the side-by-side form needs: two labels (16 each, the second with 4 above),
 * the stepper and the field, the gaps between them, then Update and the gap above it.
 * Shorter cards stack and scroll instead, so nothing spills past the card's edge.
 */
private val EditSideBySideMinHeight =
    16.dp + FieldHeight + 20.dp + FieldHeight + FieldGap * 3 + UpdateGap + FieldHeight

/**
 * The stamp card. With [onSave], it turns over (swipe or tap) to a form for the year's goal
 * and [reward]; saving turns it back. Without, it's just the front, as in the pop-up.
 */
@Composable
fun StampAndRewardCard(
    books: List<MBook>,
    goal: Int,
    reward: String?,
    modifier: Modifier = Modifier,
    ready: Boolean = true,
    onSave: ((goal: Int, reward: String) -> Unit)? = null
) {
    if (onSave == null) {
        ReadingStampCard(books = books, goal = goal, reward = reward, ready = ready, modifier = modifier)
        return
    }
    val flipState = rememberFlipCardState()
    val scope = rememberCoroutineScope()
    FlipCard(
        state = flipState,
        showBackLabel = stringResource(R.string.stamp_card_action_edit),
        showFrontLabel = stringResource(R.string.stamp_card_action_stamps),
        modifier = modifier,
        front = {
            ReadingStampCard(books = books, goal = goal, reward = reward, ready = ready, editable = true)
        },
        back = {
            // Only composed while it's showing, so it starts from the saved values each time.
            GoalAndRewardForm(goal = goal, reward = reward) { newGoal, newReward ->
                onSave(newGoal, newReward)
                scope.launch { flipState.flip() }
            }
        }
    )
}

/**
 * The back of the card: the year's goal and the reward for reaching it. Side-by-side when
 * the card is wide enough (copy left, form right); stacked on narrower phones.
 */
@Composable
private fun GoalAndRewardForm(
    goal: Int,
    reward: String?,
    onSave: (goal: Int, reward: String) -> Unit
) {
    var goalInput by rememberSaveable { mutableIntStateOf(goal.asReadingGoal()) }
    var rewardInput by rememberSaveable { mutableStateOf(reward.orEmpty()) }
    val focusManager = LocalFocusManager.current
    val save = {
        focusManager.clearFocus()
        onSave(goalInput, rewardInput)
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .clip(StampCardShape)
            .background(StampCardColor)
            .padding(StampCardPadding)
    ) {
        val sideBySide = maxWidth >= EditSideBySideMinWidth && maxHeight >= EditSideBySideMinHeight
        if (sideBySide) {
            Row(modifier = Modifier.fillMaxSize()) {
                EditIntro(
                    modifier = Modifier
                        .weight(0.42f)
                        .fillMaxHeight()
                        .padding(end = 12.dp)
                )
                EditFields(
                    goalInput = goalInput,
                    onGoalChange = { goalInput = it },
                    rewardInput = rewardInput,
                    onRewardChange = { rewardInput = it },
                    onSave = save,
                    pinUpdateToBottom = true,
                    modifier = Modifier
                        .weight(0.58f)
                        .fillMaxHeight()
                )
            }
        } else {
            // Narrow cards are short; scroll so the form never gets crushed.
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                StampCardLogo()
                EditFields(
                    goalInput = goalInput,
                    onGoalChange = { goalInput = it },
                    rewardInput = rewardInput,
                    onRewardChange = { rewardInput = it },
                    onSave = save,
                    pinUpdateToBottom = false,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth()
                )
            }
        }
    }
}

/** Logo and short instructions for the wide edit layout. */
@Composable
private fun EditIntro(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        StampCardLogo()
        Text(
            text = stringResource(R.string.stamp_card_edit_intro),
            color = StampCardInk,
            style = stampCardText(),
            modifier = Modifier.padding(top = 10.dp)
        )
        Text(
            text = stringResource(R.string.stamp_card_edit_flip_back),
            color = StampCardInk,
            style = stampCardText(),
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

/** Goal stepper, optional reward field, and Update — shared by both edit layouts. */
@Composable
private fun EditFields(
    goalInput: Int,
    onGoalChange: (Int) -> Unit,
    rewardInput: String,
    onRewardChange: (String) -> Unit,
    onSave: () -> Unit,
    pinUpdateToBottom: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = if (pinUpdateToBottom) Arrangement.SpaceBetween else Arrangement.Top
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(FieldGap)) {
            Text(
                text = stringResource(R.string.stamp_card_goal_label),
                color = StampCardInk,
                style = stampCardText()
            )
            GoalStepper(
                goal = goalInput,
                onGoalChange = onGoalChange,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = stringResource(R.string.stamp_card_reward_label),
                color = StampCardInk,
                style = stampCardText(),
                modifier = Modifier.padding(top = 4.dp)
            )
            RewardField(
                value = rewardInput,
                onValueChange = onRewardChange,
                onDone = onSave,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Spacer(modifier = Modifier.height(UpdateGap))
        // The app's button, in the card's fixed colours (the card is green in both themes),
        // and only as tall as the fields beside it so the form fits the card.
        InkleButton(
            text = stringResource(R.string.stamp_card_update),
            backgroundColor = InkBase,
            contentColor = StampCardInk,
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = CompactButtonPadding),
            onClick = onSave
        )
    }
}

/** Cream pill with − / goal / + , as on the mockups. */
@Composable
private fun GoalStepper(
    goal: Int,
    onGoalChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .height(FieldHeight)
            .clip(FieldShape)
            .background(StampCardInk)
            .padding(horizontal = 4.dp)
    ) {
        GoalStep(
            label = "\u2212",
            description = stringResource(R.string.cont_desc_decrease_goal),
            enabled = goal > MIN_READING_GOAL,
            onClick = { onGoalChange(goal - 1) }
        )
        Text(
            text = goal.toString(),
            color = InkBase,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(min = 28.dp)
        )
        GoalStep(
            label = "+",
            description = stringResource(R.string.cont_desc_increase_goal),
            enabled = goal < MAX_READING_GOAL,
            onClick = { onGoalChange(goal + 1) }
        )
    }
}

/** Cream pill text field for the optional reward. */
@Composable
private fun RewardField(
    value: String,
    onValueChange: (String) -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    BasicTextField(
        value = value,
        onValueChange = { onValueChange(it.limitTo(TextLimits.REWARD)) },
        singleLine = true,
        textStyle = stampCardText().copy(color = InkBase),
        cursorBrush = SolidColor(InkBase),
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        modifier = modifier.height(FieldHeight),
        decorationBox = { field ->
            Box(
                contentAlignment = Alignment.CenterStart,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(FieldShape)
                    .background(StampCardInk)
                    .padding(horizontal = 14.dp)
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = stringResource(R.string.stamp_card_reward_hint),
                        color = MutedBase,
                        style = stampCardText(),
                        maxLines = 1
                    )
                }
                field()
            }
        }
    )
}

/** − or + as text inside the goal stepper — stays within the pill height. */
@Composable
private fun GoalStep(
    label: String,
    description: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .widthIn(min = 48.dp)
            .fillMaxHeight()
            .clip(FieldShape)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            // Read out as "Fewer books" / "More books", not the − / + symbol as well.
            .clearAndSetSemantics { contentDescription = description }
    ) {
        Text(
            text = label,
            color = InkBase.copy(alpha = if (enabled) 1f else 0.35f),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(widthDp = 380, name = "Phone")
@Preview(widthDp = 320, name = "Narrow phone")
@Composable
private fun GoalAndRewardFormPreview() {
    AppTheme {
        Box(
            Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .aspectRatio(StampCardAspectRatio)
        ) {
            GoalAndRewardForm(goal = 6, reward = null, onSave = { _, _ -> })
        }
    }
}
