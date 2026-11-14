package com.example.readingapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.readingapp.R
import com.example.readingapp.model.MBook
import com.example.readingapp.ui.theme.AppTheme
import com.example.readingapp.ui.theme.Ink

val PulledWidth = 132.dp
val PulledHeight = 206.dp

@Composable
fun PulledBook(
    book: MBook,
    cloth: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ink = cloth.onCloth()
    Column(
        modifier = modifier
            .width(PulledWidth)
            .height(PulledHeight)
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(6.dp),
                ambientColor = Color(0x221C1917),
                spotColor = Color(0x401C1917)
            )
            .clip(RoundedCornerShape(6.dp))
            .background(cloth)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = stringResource(R.string.home_filter_reading).uppercase(),
            color = ink.copy(alpha = 0.72f),
            style = AppTheme.typography.labelSmall
        )
        Column {
            Text(
                text = book.title.orEmpty(),
                color = ink,
                style = AppTheme.typography.headlineSmall,
                maxLines = 5,
                overflow = TextOverflow.Ellipsis
            )
            book.authors?.firstOrNull()?.let { author ->
                Text(
                    text = author,
                    color = ink.copy(alpha = 0.75f),
                    style = AppTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

internal fun Color.onCloth(): Color {
    val luminance = 0.299f * red + 0.587f * green + 0.114f * blue
    return if (luminance > 0.55f) Ink else Color.White
}
