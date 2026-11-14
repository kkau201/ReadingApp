package com.example.readingapp.ui.user.update

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.example.readingapp.R
import com.example.readingapp.model.MAX_READING_GOAL
import com.example.readingapp.model.MIN_READING_GOAL
import com.example.readingapp.ui.theme.AppTheme
import com.example.readingapp.ui.theme.Ink
import com.example.readingapp.ui.theme.Muted
import kotlin.math.absoluteValue
import kotlinx.coroutines.flow.drop

private val FieldShape = RoundedCornerShape(20.dp)

@Composable
fun UpdateUserContent(
    modifier: Modifier = Modifier,
    uiState: UpdateUserUiState,
    onDisplayNameInputChange: (String) -> Unit,
    onBioInputChange: (String) -> Unit,
    onReadingGoalChange: (Int) -> Unit,
    onAvatarChange: (Avatar) -> Unit,
    onColorChange: (ProfileColor) -> Unit,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 8.dp, bottom = 24.dp)
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
        ProfileColorPicker(
            selected = uiState.selectedColor,
            onColorChange = onColorChange,
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 20.dp)
        )
        ProfileTextField(
            label = stringResource(R.string.update_user_name_label),
            value = uiState.displayNameInput,
            onValueChange = onDisplayNameInputChange,
            imeAction = ImeAction.Next,
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 28.dp)
        )
        ProfileTextField(
            label = stringResource(R.string.update_user_bio_label),
            value = uiState.bioInput,
            onValueChange = onBioInputChange,
            singleLine = false,
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 20.dp)
        )
        ReadingGoalField(
            goal = uiState.readingGoal,
            onGoalChange = onReadingGoalChange,
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 20.dp)
        )
        ProfileFormButton(
            text = stringResource(R.string.update_save),
            container = Ink,
            content = Color.White,
            onClick = onSaveClick,
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 32.dp)
        )
        ProfileFormButton(
            text = stringResource(R.string.update_cancel),
            container = Color.White,
            content = Ink,
            onClick = onCancelClick,
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 10.dp)
        )
    }
}

@Composable
private fun ProfileColorPicker(
    selected: ProfileColor,
    onColorChange: (ProfileColor) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        ProfileFieldLabel(text = stringResource(R.string.update_user_color_label))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProfileColor.entries.forEach { profileColor ->
                val isSelected = profileColor == selected
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(profileColor.color)
                        .border(
                            width = if (isSelected) 2.dp else 0.dp,
                            color = Ink,
                            shape = CircleShape
                        )
                        .clickable { onColorChange(profileColor) }
                )
            }
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
        style = AppTheme.typography.labelLarge,
        modifier = modifier
    )
}

@Composable
private fun ProfileTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    imeAction: ImeAction = ImeAction.Done
) {
    Column(modifier = modifier) {
        ProfileFieldLabel(text = label)
        TextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = AppTheme.typography.bodyLarge.copy(color = Ink),
            shape = FieldShape,
            colors = TextFieldDefaults.colors(
                focusedTextColor = Ink,
                unfocusedTextColor = Ink,
                cursorColor = Ink,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ),
            singleLine = singleLine,
            minLines = if (singleLine) 1 else 3,
            keyboardOptions = KeyboardOptions(imeAction = imeAction),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        )
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
                .background(Color.White),
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
                color = Ink,
                style = AppTheme.typography.headlineSmall,
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
            text = stringResource(R.string.profile_stamp_goal, goal),
            color = Muted,
            style = AppTheme.typography.bodyMedium,
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
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        color = content,
        style = AppTheme.typography.labelLarge,
        modifier = modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .background(container)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp)
    )
}

@OptIn(ExperimentalFoundationApi::class)
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
                .border(
                    width = if (selected) 2.dp else 0.dp,
                    color = Ink,
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
