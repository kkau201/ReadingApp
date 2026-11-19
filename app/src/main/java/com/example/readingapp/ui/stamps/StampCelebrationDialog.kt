package com.example.readingapp.ui.stamps

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.readingapp.R
import com.example.readingapp.model.MBook
import com.example.readingapp.model.asReadingGoal
import com.example.readingapp.ui.components.rememberFlipCardState
import com.example.readingapp.ui.components.rememberMotionEnabled
import com.example.readingapp.ui.theme.InkBase
import com.example.readingapp.ui.theme.PaperBase
import com.example.readingapp.ui.user.StampAndRewardCard
import com.example.readingapp.ui.user.rememberStampToLand
import com.example.readingapp.ui.user.stampedThisYear
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

/**
 * After the stamp that fills the card has landed (about 1.1 s in), a moment to take it in
 * before the card turns over to the reward.
 */
private const val RevealRewardAfterMillis = 1_900L

/** Shows the stamp card over the app while a just-finished book's stamp lands. */
@Composable
fun StampCelebrationHost(viewModel: StampCelebrationViewModel) {
    val landing by viewModel.landingBookId.collectAsStateWithLifecycle()
    val books by viewModel.books.collectAsStateWithLifecycle()
    val goal by viewModel.readingGoal.collectAsStateWithLifecycle()
    val reward by viewModel.reward.collectAsStateWithLifecycle()
    landing?.let { bookId ->
        StampCelebrationDialog(
            books = books,
            goal = goal,
            reward = reward,
            landingBookId = bookId,
            onDismiss = viewModel::onDismiss
        )
    }
}

@Composable
private fun StampCelebrationDialog(
    books: List<MBook>,
    goal: Int,
    reward: String?,
    landingBookId: String,
    onDismiss: () -> Unit
) {
    val stamped = books.stampedThisYear(goal)
    val toLand = rememberStampToLand(stamped, landingBookId)
    if (!toLand) {
        // Past the goal, so there's no slot for it: nothing to show.
        LaunchedEffect(landingBookId) { onDismiss() }
        return
    }
    val flipState = rememberFlipCardState()
    val animate = rememberMotionEnabled()
    // This stamp fills the card: once it's down, turn over to reveal what it has earned.
    val completesGoal = stamped.size == goal.asReadingGoal()
    if (completesGoal && reward != null) {
        LaunchedEffect(landingBookId) {
            if (animate) delay(RevealRewardAfterMillis.milliseconds)
            flipState.showBack()
        }
    }
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 480.dp)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StampAndRewardCard(
                books = books,
                goal = goal,
                reward = reward,
                flipState = flipState,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = stringResource(R.string.stamp_celebration_done),
                // Over the dim scrim in both themes, so fixed paper and ink colours.
                color = InkBase,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(PaperBase)
                    .clickable(onClick = onDismiss)
                    .padding(horizontal = 28.dp, vertical = 12.dp)
            )
        }
    }
}
