package com.kaylakautai.inkle.ui.user.update

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.kaylakautai.inkle.R
import com.kaylakautai.inkle.ui.components.InkleFieldLabel
import com.kaylakautai.inkle.ui.components.InkleTextField
import com.kaylakautai.inkle.model.TextLimits
import com.kaylakautai.inkle.ui.model.Avatar
import com.kaylakautai.inkle.ui.theme.CardBackground
import com.kaylakautai.inkle.ui.theme.Cover
import com.kaylakautai.inkle.ui.theme.Ink
import com.kaylakautai.inkle.ui.theme.OnInk
import com.kaylakautai.inkle.ui.theme.PaperBase
import kotlinx.coroutines.flow.drop
import kotlin.math.absoluteValue


@Composable
fun UpdateUserContent(
    modifier: Modifier = Modifier,
    uiState: UpdateUserUiState,
    onDisplayNameInputChange: (String) -> Unit,
    onBioInputChange: (String) -> Unit,
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
    InkleFieldLabel(text = text, modifier = modifier)
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
    imeAction: ImeAction = ImeAction.Done
) {
    InkleTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        error = error,
        maxLength = maxLength,
        singleLine = singleLine,
        minLines = if (singleLine) 1 else 3,
        keyboardOptions = KeyboardOptions(imeAction = imeAction),
        modifier = modifier
    )
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
