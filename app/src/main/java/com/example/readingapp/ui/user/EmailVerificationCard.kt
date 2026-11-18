package com.example.readingapp.ui.user

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.readingapp.R
import com.example.readingapp.ui.theme.CardBackground
import com.example.readingapp.ui.theme.Green
import com.example.readingapp.ui.theme.Muted
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

internal sealed interface VerifyCard {
    data class Pending(val email: String) : VerifyCard
    data object Verified : VerifyCard
}

/** How long the "Email verified" card stays before sliding away. */
private const val VERIFIED_CARD_MILLIS = 4_000L

/**
 * The verify-email reminder, or the "Email verified" thanks that replaces it. Slides in and out,
 * and cross-fades between the two. Only takes up space in the column while visible.
 */
@Composable
internal fun EmailVerificationCard(
    card: VerifyCard?,
    resent: Boolean,
    onResend: () -> Unit,
    onDismiss: () -> Unit,
    onVerifiedShown: () -> Unit,
    modifier: Modifier = Modifier
) {
    val visible = remember { MutableTransitionState(card != null) }
    visible.targetState = card != null
    // Keep showing the last card while it animates out.
    var lastCard by remember { mutableStateOf(card) }
    if (card != null && card != lastCard) lastCard = card

    if (visible.currentState || visible.targetState) {
        AnimatedVisibility(
            visibleState = visible,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
            modifier = modifier
        ) {
            AnimatedContent(
                targetState = lastCard,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "verifyCard"
            ) { shown ->
                when (shown) {
                    is VerifyCard.Pending -> VerifyEmailCard(
                        email = shown.email,
                        resent = resent,
                        onResend = onResend,
                        onDismiss = onDismiss
                    )

                    VerifyCard.Verified -> EmailVerifiedCard()
                    null -> Unit
                }
            }
        }
    }

    if (card == VerifyCard.Verified) {
        LaunchedEffect(Unit) {
            delay(VERIFIED_CARD_MILLIS.milliseconds)
            onVerifiedShown()
        }
    }
}

/** Shown briefly once the app notices the user has verified their email. */
@Composable
private fun EmailVerifiedCard(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(ProfileCardShape)
            .background(CardBackground)
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.CheckCircle,
            contentDescription = null,
            tint = Green,
            modifier = Modifier.size(32.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            ProfileSectionTitle(text = stringResource(R.string.verify_email_done_title))
            Text(
                text = stringResource(R.string.verify_email_done_body),
                color = Muted,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF7F4EF)
@Composable
private fun EmailVerifiedCardPreview() {
    EmailVerifiedCard(modifier = Modifier.padding(24.dp))
}

/** Gentle reminder for email accounts that haven't verified their address. */
@Composable
private fun VerifyEmailCard(
    email: String,
    resent: Boolean,
    onResend: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    ProfileCard(modifier = modifier) {
        ProfileSectionTitle(text = stringResource(R.string.verify_email_card_title))
        Text(
            text = stringResource(R.string.verify_email_card_body, email),
            color = Muted,
            style = MaterialTheme.typography.bodyMedium
        )
        Row(
            modifier = Modifier.padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (resent) {
                Text(
                    text = stringResource(R.string.verify_email_resent),
                    color = Muted,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.weight(1f)
                )
            } else {
                ProfilePillButton(
                    text = stringResource(R.string.verify_email_resend),
                    onClick = onResend
                )
            }
            Text(
                text = stringResource(R.string.verify_email_dismiss),
                color = Muted,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(onClick = onDismiss)
                    .padding(horizontal = 8.dp, vertical = 10.dp)
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF7F4EF)
@Composable
private fun VerifyEmailCardPreview() {
    VerifyEmailCard(
        email = "jane@example.com",
        resent = false,
        onResend = {},
        onDismiss = {},
        modifier = Modifier.padding(24.dp)
    )
}
