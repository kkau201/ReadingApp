package com.example.readingapp.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.readingapp.ui.details.update.FinishedDateField
import com.example.readingapp.ui.theme.AppTheme
import com.example.readingapp.ui.theme.Cover
import com.example.readingapp.ui.theme.Ink
import com.example.readingapp.ui.theme.Muted

data class DialogState(
    val dismissed: Boolean = false,
    val dismissible: Boolean = true,
    @DrawableRes val icon: Int? = null,
    val title: String? = null,
    val message: String? = null,
    val content: Map<String, String> = emptyMap(),
    val choices: List<DialogChoice> = emptyList(),
    val finishedDate: DialogFinishedDate? = null,
    val primaryButtonText: String? = null,
    val onPrimaryClick: (() -> Unit)? = null,
    val secondaryButtonText: String? = null,
    val onSecondaryClick: (() -> Unit)? = null,
    val onDismissRequest: (() -> Unit)? = null
)

data class DialogChoice(
    val text: String,
    val selected: Boolean = false,
    val onClick: () -> Unit
)

data class DialogFinishedDate(
    val year: Int,
    val month: Int?,
    val day: String,
    val dayError: Boolean,
    val onYearChanged: (Int) -> Unit,
    val onMonthChanged: (Int?) -> Unit,
    val onDayChanged: (String) -> Unit
)

@Composable
fun ReadingAppDialog(modifier: Modifier = Modifier, state: DialogState?) {
    state?.apply {
        if (dismissed) {
            return
        }
        Dialog(
            onDismissRequest = {
                if (dismissible) {
                    onDismissRequest?.invoke()
                }
            },
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            val maxHeight = LocalConfiguration.current.screenHeightDp.dp * 0.85f
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                modifier = modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .heightIn(max = maxHeight)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 32.dp)
                ) {
                    icon?.let {
                        Image(
                            painter = painterResource(id = icon),
                            contentDescription = null,
                            modifier = Modifier
                                .padding(bottom = 16.dp)
                                .size(72.dp)
                        )
                    }
                    title?.let {
                        Text(
                            text = title,
                            style = AppTheme.typography.titleLarge,
                            textAlign = TextAlign.Center,
                            color = Ink,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    message?.let {
                        Text(
                            text = message,
                            style = AppTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = Muted,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        )
                    }
                    if (choices.isNotEmpty() || primaryButtonText != null || secondaryButtonText != null) {
                        Column(
                            modifier = Modifier.padding(top = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            choices.forEach { choice ->
                                DialogButton(
                                    text = choice.text,
                                    container = if (choice.selected) Ink else Color.White,
                                    content = if (choice.selected) Color.White else Ink,
                                    border = if (choice.selected) null else Cover,
                                    onClick = choice.onClick
                                )
                            }
                            finishedDate?.let { date ->
                                FinishedDateField(
                                    year = date.year,
                                    month = date.month,
                                    day = date.day,
                                    dayError = date.dayError,
                                    onYearChanged = date.onYearChanged,
                                    onMonthChanged = date.onMonthChanged,
                                    onDayChanged = date.onDayChanged
                                )
                            }
                            primaryButtonText?.let {
                                DialogButton(
                                    text = primaryButtonText,
                                    container = Ink,
                                    content = Color.White,
                                    onClick = { onPrimaryClick?.invoke() }
                                )
                            }
                            secondaryButtonText?.let {
                                DialogButton(
                                    text = secondaryButtonText,
                                    container = Color.White,
                                    content = Ink,
                                    border = Cover,
                                    onClick = { onSecondaryClick?.invoke() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DialogButton(
    text: String,
    container: Color,
    content: Color,
    onClick: () -> Unit,
    border: Color? = null
) {
    Text(
        text = text,
        color = content,
        style = AppTheme.typography.labelLarge,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .then(
                if (border != null) {
                    Modifier.border(1.dp, border, CircleShape)
                } else {
                    Modifier
                }
            )
            .background(container)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp)
    )
}


@Preview
@Composable
fun DialogPreviewOneButton() {
    ReadingAppDialog(state = DialogState(
        title = "Test Dialog",
        message = "Are you sure you want to test?",
        primaryButtonText = "Yes",
        onPrimaryClick = {}
    ))
}

@Preview
@Composable
fun DialogPreviewTwoButtons() {
    ReadingAppDialog(state = DialogState(
        title = "Test Dialog",
        message = "Are you sure you want to test?",
        primaryButtonText = "Yes",
        onPrimaryClick = {},
        secondaryButtonText = "No",
        onSecondaryClick = {}
    ))
}