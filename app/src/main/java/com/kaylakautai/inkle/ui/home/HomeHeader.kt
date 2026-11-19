package com.kaylakautai.inkle.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kaylakautai.inkle.R
import com.kaylakautai.inkle.ui.components.LibraryPageTitle
import com.kaylakautai.inkle.ui.model.Avatar
import com.kaylakautai.inkle.ui.theme.AppTheme
import com.kaylakautai.inkle.ui.theme.Cover
import com.kaylakautai.inkle.ui.theme.Muted
import com.kaylakautai.inkle.ui.theme.PaperBase

/** "Hello, name", the Library title and book count, with the avatar on the right. */
@Composable
internal fun HomeHeader(
    displayName: String?,
    avatar: Avatar?,
    bookCount: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppTheme.spacing.mdSpacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            if (!displayName.isNullOrBlank()) {
                Text(
                    text = stringResource(R.string.home_greeting, displayName),
                    color = Muted,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            LibraryPageTitle(
                text = stringResource(R.string.home_title),
                modifier = Modifier.padding(top = 2.dp)
            )
            Text(
                text = pluralStringResource(R.plurals.home_book_count, bookCount, bookCount),
                color = Muted,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        HomeAvatar(avatar = avatar, name = displayName)
    }
}

@Composable
private fun HomeAvatar(avatar: Avatar?, name: String?) {
    if (avatar != null) {
        Image(
            painter = painterResource(avatar.img),
            contentDescription = stringResource(R.string.cont_desc_profile_image),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .padding(start = 16.dp)
                .size(48.dp)
                .clip(CircleShape)
                // Avatars are black line drawings on transparent: keep them on paper in dark theme.
                .background(PaperBase)
        )
    } else {
        val initial = name?.trim()?.firstOrNull()?.uppercase() ?: ""
        Column(
            modifier = Modifier
                .padding(start = 16.dp)
                .size(48.dp)
                .clip(CircleShape)
                .background(Cover),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = initial,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}
