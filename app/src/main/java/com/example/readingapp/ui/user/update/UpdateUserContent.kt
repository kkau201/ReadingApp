package com.example.readingapp.ui.user.update

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.example.readingapp.R
import com.example.readingapp.model.MAX_READING_GOAL
import com.example.readingapp.model.MIN_READING_GOAL
import com.example.readingapp.model.TextLimits
import com.example.readingapp.ui.components.characterCounter
import com.example.readingapp.ui.model.Avatar
import com.example.readingapp.ui.theme.CardBackground
import com.example.readingapp.ui.theme.Cover
import com.example.readingapp.ui.theme.Ink
import com.example.readingapp.ui.theme.Muted
import com.example.readingapp.ui.theme.OnInk
import com.example.readingapp.ui.theme.PaperBase
import kotlinx.coroutines.flow.drop
import kotlin.math.absoluteValue

private val FieldShape = RoundedCornerShape(20.dp)

@Composable
fun UpdateUserContent(
    modifier: Modifier = Modifier,
    uiState: UpdateUserUiState,
    onDisplayNameInputChange: (String) -> Unit,
    onBioInputChange: (String) -> Unit,
    onReadingGoalChange: (Int) -> Unit,
    onRewardInputChange: (String) -> Unit,
    onAvatarChange: (Avatar) -> Unit,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    Column(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(top = 8.dp, bottom = 16.dp)
        ) {
            ProfileFieldLabel(
                text = stringResource(R.string.update_user_avatar_label),
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 8.dp)
            )
            UpdateAvatarPager(
                selectedAvatar = uiState.selectedAvatar,
                avatarList = uiState.avatarImgList,
                onAvatarChange = onAvatarChange
            )
            ProfileTextField(
                label = stringResource(R.string.update_user_name_label),
                value = uiState.displayNameInput,
                onValueChange = onDisplayNameInputChange,
                maxLength = TextLimits.DISPLAY_NAME,
                error = if (uiState.displayNameError) {
                    stringResource(R.string.update_user_name_error)
                } else {
                    null
                },
                imeAction = ImeAction.Next,
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 28.dp)
            )
            ProfileTextField(
                label = stringResource(R.string.update_user_bio_label),
                value = uiState.bioInput,
                onValueChange = onBioInputChange,
                maxLength = TextLimits.QUOTE,
                singleLine = false,
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 20.dp)
            )
            ReadingGoalField(
                goal = uiState.readingGoal,
                onGoalChange = onReadingGoalChange,
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 20.dp)
            )
            ProfileTextField(
                label = stringResource(R.string.update_user_reward_label),
                value = uiState.rewardInput,
                onValueChange = onRewardInputChange,
                maxLength = TextLimits.REWARD,
                placeholder = stringResource(R.string.update_user_reward_hint),
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 20.dp)
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProfileFormButton(
                text = stringResource(R.string.update_cancel),
                container = CardBackground,
                content = MaterialTheme.colorScheme.onBackground,
                border = Cover,
                onClick = onCancelClick,
                modifier = Modifier.weight(1f)
            )
            ProfileFormButton(
                text = stringResource(R.string.update_save),
                container = Ink,
                content = OnInk,
                onClick = onSaveClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ProfileFieldLabel(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        color = Muted,
        style = MaterialTheme.typography.labelLarge,
        modifier = modifier
    )
}

@Composable
private fun ProfileTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    maxLength: Int,
    modifier: Modifier = Modifier,
    error: String? = null,
    singleLine: Boolean = true,
    imeAction: ImeAction = ImeAction.Done,
    placeholder: String? = null
) {
    Column(modifier = modifier) {
        ProfileFieldLabel(text = label)
        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = placeholder?.let { hint ->
                { Text(text = hint, color = Muted, style = MaterialTheme.typography.bodyLarge) }
            },
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onBackground),
            shape = FieldShape,
            colors = TextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                cursorColor = MaterialTheme.colorScheme.onBackground,
                focusedContainerColor = CardBackground,
                unfocusedContainerColor = CardBackground,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ),
            singleLine = singleLine,
            minLines = if (singleLine) 1 else 3,
            supportingText = characterCounter(value.length, maxLength),
            keyboardOptions = KeyboardOptions(imeAction = imeAction),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        )
        if (error != null) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun ReadingGoalField(
    goal: Int,
    onGoalChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        ProfileFieldLabel(text = stringResource(R.string.update_user_goal_label))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .clip(FieldShape)
                .background(CardBackground),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            IconButton(
                onClick = { onGoalChange(goal - 1) },
                enabled = goal > MIN_READING_GOAL
            ) {
                Icon(
                    imageVector = Icons.Rounded.Remove,
                    contentDescription = stringResource(R.string.cont_desc_decrease_goal),
                    tint = if (goal > MIN_READING_GOAL) Ink else Muted
                )
            }
            Text(
                text = goal.toString(),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            IconButton(
                onClick = { onGoalChange(goal + 1) },
                enabled = goal < MAX_READING_GOAL
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = stringResource(R.string.cont_desc_increase_goal),
                    tint = if (goal < MAX_READING_GOAL) Ink else Muted
                )
            }
        }
        Text(
            text = pluralStringResource(R.plurals.profile_stamp_goal, goal, goal),
            color = Muted,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
private fun ProfileFormButton(
    text: String,
    container: Color,
    content: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    border: Color? = null
) {
    Text(
        text = text,
        color = content,
        style = MaterialTheme.typography.labelLarge,
        textAlign = TextAlign.Center,
        modifier = modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .then(if (border != null) Modifier.border(1.dp, border, CircleShape) else Modifier)
            .background(container)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp)
    )
}

@Composable
fun UpdateAvatarPager(
    modifier: Modifier = Modifier,
    selectedAvatar: Avatar?,
    avatarList: List<Avatar>,
    onAvatarChange: (Avatar) -> Unit
) {
    val selectedIndex = avatarList.indexOfFirst { it.id == selectedAvatar?.id }.coerceAtLeast(0)
    val pagerState = rememberPagerState(
        initialPage = selectedIndex,
        pageCount = { avatarList.size }
    )

    LaunchedEffect(selectedAvatar?.id) {
        val index = avatarList.indexOfFirst { it.id == selectedAvatar?.id }
        if (index >= 0 && index != pagerState.currentPage) {
            pagerState.animateScrollToPage(index)
        }
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }
            .drop(1)
            .collect { page ->
                onAvatarChange(avatarList[page])
            }
    }

    HorizontalPager(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 120.dp),
        pageSize = PageSize.Fixed(120.dp),
        state = pagerState
    ) { page ->
        val selected = pagerState.currentPage == page
        Image(
            painter = painterResource(id = avatarList[page].img),
            contentDescription = stringResource(R.string.cont_desc_profile_image),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .clip(CircleShape)
                .size(96.dp)
                // Avatars are black line drawings on transparent: keep them on paper in dark theme.
                .background(PaperBase)
                .border(
                    width = if (selected) 2.dp else 0.dp,
                    color = MaterialTheme.colorScheme.onBackground,
                    shape = CircleShape
                )
                .graphicsLayer {
                    val pageOffset = (
                            (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                            ).absoluteValue
                    val fraction = 1f - pageOffset.coerceIn(0f, 1f)
                    alpha = lerp(start = 0.45f, stop = 1f, fraction = fraction)
                    scaleX = lerp(start = 0.8f, stop = 1f, fraction = fraction)
                    scaleY = lerp(start = 0.8f, stop = 1f, fraction = fraction)
                }
        )
    }
}
