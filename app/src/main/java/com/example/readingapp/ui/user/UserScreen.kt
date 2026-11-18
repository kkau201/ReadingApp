package com.example.readingapp.ui.user

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.readingapp.BuildConfig
import com.example.readingapp.R
import com.example.readingapp.common.ViewModelBinding
import com.example.readingapp.common.observeLifecycle
import com.example.readingapp.model.DEFAULT_READING_GOAL
import com.example.readingapp.model.MBook
import com.example.readingapp.ui.model.BookStatus
import com.example.readingapp.ui.components.LibraryPageTitle
import com.example.readingapp.ui.theme.AppTheme
import com.example.readingapp.ui.theme.CardBackground
import com.example.readingapp.ui.theme.Cover
import com.example.readingapp.ui.theme.OnInk
import com.example.readingapp.ui.theme.PaperBase
import com.example.readingapp.ui.theme.Green
import com.example.readingapp.ui.theme.Ink
import com.example.readingapp.ui.theme.Muted
import com.example.readingapp.ui.model.Avatar
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun UserScreen(
    viewModel: UserViewModel = hiltViewModel(),
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
) {
    ViewModelBinding(viewModel)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ObserveUserLifecycleEvents(lifecycleOwner, viewModel)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
    ) { padding ->
        UserContent(
            modifier = Modifier.padding(padding),
            userDisplayName = uiState.user?.displayName.orEmpty(),
            userBio = uiState.user?.quote.orEmpty(),
            userAvatar = uiState.user?.avatar,
            books = uiState.books,
            booksFailed = uiState.booksFailed,
            onRetryBooks = viewModel::onRetryBooksClick,
            readingGoal = uiState.user?.readingGoal ?: DEFAULT_READING_GOAL,
            onUpdateClick = viewModel::navigateToUpdateUser,
            onLogoutClick = viewModel::onLogoutClick,
            onDeleteAccountClick = viewModel::onDeleteAccountClick,
            unverifiedEmail = uiState.unverifiedEmail,
            verificationResent = uiState.verificationResent,
            onResendVerification = viewModel::onResendVerificationClick,
            onDismissVerification = viewModel::onDismissVerificationClick,
            emailJustVerified = uiState.emailJustVerified,
            onEmailVerifiedShown = viewModel::onEmailVerifiedShown
        )
    }
}

@Composable
fun UserContent(
    modifier: Modifier = Modifier,
    userDisplayName: String,
    userBio: String,
    userAvatar: Avatar? = null,
    books: List<MBook> = emptyList(),
    booksFailed: Boolean = false,
    onRetryBooks: () -> Unit = {},
    readingGoal: Int = DEFAULT_READING_GOAL,
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
    val bio = userBio.asProfileText()
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
            bio = bio,
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
            ReadingStampCard(
                books = books,
                goal = readingGoal,
                modifier = Modifier.padding(horizontal = pagePadding)
            )
            ReadingSummary(
                books = books,
                nowReading = nowReading,
                modifier = Modifier.padding(horizontal = pagePadding)
            )
        }
        Column(
            modifier = Modifier
                .padding(horizontal = pagePadding)
                .padding(top = 24.dp),
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
}

@Composable
private fun ProfileHeader(
    name: String?,
    bio: String?,
    avatar: Avatar?,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = stringResource(R.string.profile_title),
                color = Muted,
                style = MaterialTheme.typography.labelLarge
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LibraryPageTitle(
                    text = name ?: stringResource(R.string.profile_title),
                    modifier = Modifier.weight(1f, fill = false)
                )
                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = stringResource(R.string.profile_update),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
            if (bio != null) {
                Text(
                    text = bio,
                    color = Muted,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
        ProfileAvatar(
            avatar = avatar,
            name = name.orEmpty(),
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}

@Composable
private fun ReadingSummary(
    books: List<MBook>,
    nowReading: List<MBook>,
    modifier: Modifier = Modifier
) {
    val finished = books.count { it.bookStatus == BookStatus.FINISHED }
    val pages = books.filter { it.bookStatus == BookStatus.FINISHED }.sumOf { it.pageCount ?: 0 }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CardBackground)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            ProfileSectionTitle(text = stringResource(R.string.profile_section_reading))
            Row(modifier = Modifier.fillMaxWidth()) {
                ReadingStat(
                    count = books.count { it.bookStatus == BookStatus.LIBRARY },
                    label = stringResource(R.string.home_filter_want_to_read),
                    modifier = Modifier.weight(1f)
                )
                ReadingStat(
                    count = books.count { it.bookStatus == BookStatus.READING },
                    label = stringResource(R.string.home_filter_reading),
                    modifier = Modifier.weight(1f)
                )
                ReadingStat(
                    count = finished,
                    label = stringResource(R.string.home_filter_finished),
                    modifier = Modifier.weight(1f)
                )
            }
            if (pages > 0) {
                Text(
                    text = pluralStringResource(R.plurals.profile_pages_finished, pages, pages),
                    color = Muted,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        if (nowReading.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            NowReading(books = nowReading)
        }
    }
}

@Composable
private fun ReadingStat(
    count: Int,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = count.toString(),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = label,
            color = Muted,
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
private fun NowReading(books: List<MBook>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ProfileSectionTitle(text = stringResource(R.string.profile_section_now))
        books.take(3).forEach { book ->
            Column {
                Text(
                    text = book.title.orEmpty(),
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                val author = book.authors?.filter { it.isNotBlank() }?.joinToString(", ")
                if (!author.isNullOrBlank()) {
                    Text(
                        text = author,
                        color = Muted,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}

private sealed interface VerifyCard {
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
private fun EmailVerificationCard(
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
            .clip(RoundedCornerShape(20.dp))
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
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CardBackground)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
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
                Text(
                    text = stringResource(R.string.verify_email_resend),
                    color = OnInk,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Ink)
                        .clickable(onClick = onResend)
                        .padding(horizontal = 18.dp, vertical = 10.dp)
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

/** Shown in place of the reading stats when the saved books couldn't be loaded. */
@Composable
private fun BooksErrorCard(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CardBackground)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ProfileSectionTitle(text = stringResource(R.string.profile_section_reading))
        Text(
            text = stringResource(R.string.profile_books_error),
            color = Muted,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = stringResource(R.string.error_try_again),
            color = OnInk,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier
                .padding(top = 8.dp)
                .clip(CircleShape)
                .background(Ink)
                .clickable(onClick = onRetry)
                .padding(horizontal = 18.dp, vertical = 10.dp)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF7F4EF)
@Composable
private fun BooksErrorCardPreview() {
    BooksErrorCard(onRetry = {}, modifier = Modifier.padding(24.dp))
}

@Composable
private fun ProfileSectionTitle(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.onBackground,
        style = MaterialTheme.typography.titleLarge,
        modifier = modifier
    )
}

@Composable
private fun ProfileAvatar(
    avatar: Avatar?,
    name: String,
    modifier: Modifier = Modifier
) {
    if (avatar != null) {
        Image(
            painter = painterResource(avatar.img),
            contentDescription = stringResource(R.string.cont_desc_profile_image),
            contentScale = ContentScale.Crop,
            modifier = modifier
                .size(72.dp)
                .clip(CircleShape)
                // Avatars are black line drawings on transparent: keep them on paper in dark theme.
                .background(PaperBase)
        )
    } else {
        val initial = name.trim().firstOrNull()?.uppercase().orEmpty()
        Column(
            modifier = modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(Cover),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = initial,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.headlineSmall,
                maxLines = 1,
                overflow = TextOverflow.Clip
            )
        }
    }
}

@Composable
fun ObserveUserLifecycleEvents(
    lifecycleOwner: LifecycleOwner,
    viewModel: UserViewModel
) {
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.loadUser()
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    viewModel.observeLifecycle(lifecycleOwner)
}

private fun String.asProfileText(): String? {
    val value = trim()
    return value.takeUnless { it.isEmpty() || it.equals("null", ignoreCase = true) }
}