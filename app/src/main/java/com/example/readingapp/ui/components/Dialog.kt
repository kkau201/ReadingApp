package com.example.readingapp.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.readingapp.ui.theme.AppTheme

data class DialogState(
    val dismissed: Boolean = false,
    val dismissible: Boolean = true,
    @DrawableRes val icon: Int? = null,
    val title: String? = null,
    val message: String? = null,
    val content: Map<String, String> = emptyMap(),
    val primaryButtonText: String? = null,
    val onPrimaryClick: (() -> Unit)? = null,
    val secondaryButtonText: String? = null,
    val onSecondaryClick: (() -> Unit)? = null,
    val onDismissRequest: (() -> Unit)? = null
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
            Surface(
                shape = RoundedCornerShape(AppTheme.spacing.lgSpacing),
                color = AppTheme.colors.background,
                modifier = modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppTheme.spacing.mdSpacing)
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = modifier.padding(AppTheme.spacing.mdSpacing)

                ) {
                    icon?.let {
                        Image(
                            painter = painterResource(id = icon),
                            contentDescription = null,
                            modifier = modifier.padding(bottom = AppTheme.spacing.mdSpacing)
                        )
                    }
                    title?.let {
                        Text(
                            text = title,
                            style = AppTheme.typography.titleLarge,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.colors.onBackground,
                            modifier = modifier
                                .fillMaxWidth()
                                .padding(bottom = AppTheme.spacing.smSpacing)
                        )
                    }

                    message?.let {
                        Text(
                            text = message,
                            style = AppTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = AppTheme.colors.onBackground,
                            modifier = modifier
                                .fillMaxWidth()
                                .padding(bottom = AppTheme.spacing.smSpacing)
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = modifier.fillMaxWidth()
                    ) {
                        secondaryButtonText?.let {
                            Box(modifier = modifier.weight(1f)) {
                                ReadingAppButton(
                                    text = secondaryButtonText,
                                    modifier = modifier.fillMaxWidth(),
                                    padding = PaddingValues(end = AppTheme.spacing.xxsmSpacing),
                                    onClick = { onSecondaryClick?.invoke() }
                                )
                            }
                        }
                        primaryButtonText?.let {
                            Box(modifier = modifier.weight(1f)) {
                                ReadingAppButton(
                                    text = primaryButtonText,
                                    modifier = modifier.fillMaxWidth(),
                                    padding = PaddingValues(start = AppTheme.spacing.xxsmSpacing),
                                    onClick = { onPrimaryClick?.invoke() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
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