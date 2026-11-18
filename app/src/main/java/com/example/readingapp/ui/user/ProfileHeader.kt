package com.example.readingapp.ui.user

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.readingapp.R
import com.example.readingapp.ui.components.LibraryPageTitle
import com.example.readingapp.ui.model.Avatar
import com.example.readingapp.ui.theme.Cover
import com.example.readingapp.ui.theme.Muted
import com.example.readingapp.ui.theme.PaperBase

/** The user's name (with an edit button), bio and avatar. */
@Composable
internal fun ProfileHeader(
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

/** The chosen avatar, or the name's first letter in a circle when there isn't one. */
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
