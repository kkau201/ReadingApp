package com.example.readingapp.ui.user

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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
import com.example.readingapp.ui.components.BookStatus
import com.example.readingapp.ui.components.LibraryPageTitle
import com.example.readingapp.ui.theme.AppTheme
import com.example.readingapp.ui.theme.Paper
import com.example.readingapp.ui.theme.Cover
import com.example.readingapp.ui.theme.Ink
import com.example.readingapp.ui.theme.Muted
import com.example.readingapp.ui.user.update.Avatar

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
        containerColor = Paper,
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
    ) { padding ->
        UserContent(
            modifier = Modifier.padding(padding),
            userDisplayName = uiState.user?.displayName.orEmpty(),
            userBio = uiState.user?.quote.orEmpty(),
            userProfession = uiState.user?.profession.orEmpty(),
            userAvatar = uiState.user?.avatar,
            books = uiState.books,
            readingGoal = uiState.user?.readingGoal ?: DEFAULT_READING_GOAL,
            onUpdateClick = viewModel::navigateToUpdateUser,
            onLogoutClick = viewModel::onLogoutClick
        )
    }
}

@Composable
fun UserContent(
    modifier: Modifier = Modifier,
    userDisplayName: String,
    userBio: String,
    userProfession: String = "",
    userAvatar: Avatar? = null,
    books: List<MBook> = emptyList(),
    readingGoal: Int = DEFAULT_READING_GOAL,
    onUpdateClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    val pagePadding = AppTheme.spacing.mdSpacing
    val name = userDisplayName.asProfileText()
    val bio = userBio.asProfileText()
    val profession = userProfession.asProfileText()
    val nowReading = books.filter { it.bookStatus == BookStatus.READING }
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ProfileHeader(
            name = name,
            profession = profession,
            bio = bio,
            avatar = userAvatar,
            onEditClick = onUpdateClick,
            modifier = Modifier.padding(horizontal = pagePadding)
        )
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
        Column(
            modifier = Modifier
                .padding(horizontal = pagePadding)
                .padding(top = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.profile_logout),
                color = Color.White,
                style = AppTheme.typography.labelLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CircleShape)
                    .background(Ink)
                    .clickable(onClick = onLogoutClick)
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            )
            Text(
                text = stringResource(R.string.profile_version, BuildConfig.VERSION_NAME),
                color = Muted,
                style = AppTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ProfileHeader(
    name: String?,
    profession: String?,
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
                style = AppTheme.typography.labelLarge
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
                        tint = Ink
                    )
                }
            }
            if (profession != null) {
                Text(
                    text = profession,
                    color = Muted,
                    style = AppTheme.typography.labelLarge,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            if (bio != null) {
                Text(
                    text = bio,
                    color = Muted,
                    style = AppTheme.typography.bodyLarge,
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
            .background(Color.White)
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
                    style = AppTheme.typography.bodyMedium
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
            color = Ink,
            style = AppTheme.typography.headlineSmall
        )
        Text(
            text = label,
            color = Muted,
            style = AppTheme.typography.labelMedium,
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
                    color = Ink,
                    style = AppTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                val author = book.authors?.filter { it.isNotBlank() }?.joinToString(", ")
                if (!author.isNullOrBlank()) {
                    Text(
                        text = author,
                        color = Muted,
                        style = AppTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileSectionTitle(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        color = Ink,
        style = AppTheme.typography.titleLarge,
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
                color = Ink,
                style = AppTheme.typography.headlineSmall,
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