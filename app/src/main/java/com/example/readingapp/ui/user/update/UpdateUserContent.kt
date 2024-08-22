package com.example.readingapp.ui.user.update

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.example.readingapp.R
import com.example.readingapp.ui.details.update.UpdateButtons
import com.example.readingapp.ui.theme.AppTheme
import com.example.readingapp.ui.theme.AppTheme.spacing
import com.example.readingapp.ui.theme.Blue
import kotlin.math.absoluteValue

@Composable
fun UpdateUserContent(
    modifier: Modifier = Modifier,
    uiState: UpdateUserUiState,
    onDisplayNameInputChange: (String) -> Unit,
    onBioInputChange: (String) -> Unit,
    onAvatarChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    Box(modifier = modifier.fillMaxSize().padding(bottom = spacing.mdSpacing)) {
        Column(
            Modifier
                .align(Alignment.TopCenter)
                .background(shape = RoundedCornerShape(spacing.smSpacing), color = Color.Transparent)
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(vertical = spacing.mdSpacing)
        ) {
            UpdateAvatarPager(
                selectedAvatarId = uiState.selectedAvatarId,
                avatarList = uiState.avatarImgList,
                onAvatarChange = onAvatarChange,
                modifier = Modifier.padding(vertical = spacing.lgSpacing)
            )

            UpdateUserInputField(
                label = stringResource(id = R.string.update_user_name_label),
                input = uiState.displayNameInput,
                onInputChange = onDisplayNameInputChange,
                modifier = Modifier.padding(horizontal = spacing.lgSpacing)
            )
            UpdateUserInputField(
                label = stringResource(id = R.string.update_user_bio_label),
                input = uiState.bioInput,
                onInputChange = onBioInputChange,
                modifier = Modifier.padding(horizontal = spacing.lgSpacing)
            )
        }

        Divider(
            color = Blue.copy(alpha = 0.5f),
            modifier = Modifier
                .padding(spacing.lgSpacing)
                .padding(bottom = spacing.mdSpacing)
                .align(Alignment.BottomCenter)
        )

        UpdateButtons(
            saveColor = Blue,
            cancelColor = Blue.copy(alpha = 0.7f),
            onSaveClick = onSaveClick,
            onCancelClick = onCancelClick,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.align(Alignment.BottomCenter).padding(horizontal = spacing.mdSpacing)
        )
    }
}

@Composable
fun UpdateUserInputField(
    modifier: Modifier = Modifier,
    label: String,
    input: String,
    onInputChange: (String) -> Unit
) {
    val textFieldColors = TextFieldDefaults.outlinedTextFieldColors(
        textColor = AppTheme.colors.onBackground,
        unfocusedLabelColor = Blue.copy(alpha = 0.5f),
        unfocusedBorderColor = Blue.copy(alpha = 0.5f),
        focusedBorderColor = Blue,
        focusedLabelColor = Blue,
        cursorColor = Blue
    )

    OutlinedTextField(
        label = { Text(text = label) },
        value = input,
        shape = RoundedCornerShape(spacing.smSpacing),
        colors = textFieldColors,
        onValueChange = onInputChange,
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = spacing.smSpacing)
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UpdateAvatarPager(
    modifier: Modifier = Modifier,
    selectedAvatarId: String,
    avatarList: List<Avatar>,
    onAvatarChange: (String) -> Unit
) {
    val initSelectedIndex = avatarList.indexOfFirst { it.id == selectedAvatarId }.coerceAtLeast(0)
    val pagerState = rememberPagerState(initialPage = initSelectedIndex, pageCount = { avatarList.size })

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            onAvatarChange(avatarList[page].id)
        }
    }

    HorizontalPager(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 110.dp),
        pageSize = PageSize.Fixed(200.dp),
        state = pagerState
    ) { page ->
        Image(
            painter = painterResource(id = avatarList[page].img),
            contentDescription = stringResource(R.string.cont_desc_profile_image),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .clip(CircleShape)
                .size(200.dp)
                .graphicsLayer {
                    // Calculate the absolute offset for the current page from the
                    // scroll position. We use the absolute value which allows us to mirror
                    // any effects for both directions
                    val pageOffset = (
                            (pagerState.currentPage - page) + pagerState
                                .currentPageOffsetFraction
                            ).absoluteValue

                    // We animate the alpha, between 50% and 100%
                    alpha = lerp(
                        start = 0.5f,
                        stop = 1f,
                        fraction = 1f - pageOffset.coerceIn(0f, 1f)
                    )

                    scaleX = lerp(
                        start = 0.75f,
                        stop = 1f,
                        fraction = 1f - pageOffset.coerceIn(0f, 1f)
                    )

                    scaleY = lerp(
                        start = 0.75f,
                        stop = 1f,
                        fraction = 1f - pageOffset.coerceIn(0f, 1f)
                    )
                }
        )
    }
}