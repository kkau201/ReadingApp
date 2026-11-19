package com.example.readingapp.ui.user

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.readingapp.BuildConfig
import com.example.readingapp.R
import com.example.readingapp.model.DEFAULT_READING_GOAL
import com.example.readingapp.model.MBook
import com.example.readingapp.ui.model.Avatar
import com.example.readingapp.ui.model.BookStatus
import com.example.readingapp.ui.theme.AppTheme
import com.example.readingapp.ui.theme.Ink
import com.example.readingapp.ui.theme.Muted
import com.example.readingapp.ui.theme.OnInk

/**
 * The Profile screen: who the user is, any email verification card, their reading stats and
 * the account actions. Each part lives in its own file in this package.
 */
@Composable
fun UserContent(
    modifier: Modifier = Modifier,
    userDisplayName: String,
    userBio: String,
    userAvatar: Avatar? = null,
    books: List<MBook> = emptyList(),
    booksFailed: Boolean = false,
    booksLoaded: Boolean = true,
    onRetryBooks: () -> Unit = {},
    readingGoal: Int = DEFAULT_READING_GOAL,
    reward: String? = null,
    onUpdateClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onDeleteAccountClick: () -> Unit = {},
    unverifiedEmail: String? = null,
    verificationResent: Boolean = false,
    onResendVerification: () -> Unit = {},
    onDismissVerification: () -> Unit = {},
    emailJustVerified: Boolean = false,
    onEmailVerifiedShown: () -> Unit = {}
) {
    val pagePadding = AppTheme.spacing.mdSpacing
    val name = userDisplayName.asProfileText()
    val nowReading = books.filter { it.bookStatus == BookStatus.READING }
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 24.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ProfileHeader(
            name = name,
            bio = userBio.asProfileText(),
            avatar = userAvatar,
            onEditClick = onUpdateClick,
            modifier = Modifier.padding(horizontal = pagePadding)
        )
        EmailVerificationCard(
            card = when {
                emailJustVerified -> VerifyCard.Verified
                unverifiedEmail != null -> VerifyCard.Pending(unverifiedEmail)
                else -> null
            },
            resent = verificationResent,
            onResend = onResendVerification,
            onDismiss = onDismissVerification,
            onVerifiedShown = onEmailVerifiedShown,
            modifier = Modifier.padding(horizontal = pagePadding)
        )
        if (booksFailed) {
            // Instead of the stamp card and stats, which would wrongly show zero books.
            BooksErrorCard(
                onRetry = onRetryBooks,
                modifier = Modifier.padding(horizontal = pagePadding)
            )
        } else {
            val hint = rememberRewardHint()
            Column(
                modifier = Modifier.padding(horizontal = pagePadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StampAndRewardCard(
                    books = books,
                    goal = readingGoal,
                    reward = reward,
                    ready = booksLoaded,
                    // The same screen as the edit button: the reward is set with the goal.
                    onSetReward = onUpdateClick,
                    onFlip = hint::cardFlipped
                )
                if (hint.shown) {
                    Text(
                        text = stringResource(R.string.reward_flip_hint),
                        color = Muted,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            ReadingSummary(
                books = books,
                nowReading = nowReading,
                modifier = Modifier.padding(horizontal = pagePadding)
            )
        }
        AccountActions(
            onLogoutClick = onLogoutClick,
            onDeleteAccountClick = onDeleteAccountClick,
            modifier = Modifier
                .padding(horizontal = pagePadding)
                .padding(top = 24.dp)
        )
    }
}

/** Log out, delete account, and the app version. */
@Composable
private fun AccountActions(
    onLogoutClick: () -> Unit,
    onDeleteAccountClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.profile_logout),
            color = OnInk,
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clip(CircleShape)
                .background(Ink)
                .clickable(onClick = onLogoutClick)
                .padding(horizontal = 20.dp, vertical = 14.dp)
        )
        Text(
            text = stringResource(R.string.profile_delete_account),
            color = Muted,
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .clip(CircleShape)
                .clickable(onClick = onDeleteAccountClick)
                .padding(horizontal = 20.dp, vertical = 10.dp)
        )
        Text(
            text = stringResource(R.string.profile_version, BuildConfig.VERSION_NAME),
            color = Muted,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )
    }
}

/** Blank, or the literal "null" some older profiles stored, counts as not set. */
private fun String.asProfileText(): String? {
    val value = trim()
    return value.takeUnless { it.isEmpty() || it.equals("null", ignoreCase = true) }
}
