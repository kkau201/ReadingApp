package com.example.readingapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.readingapp.R
import com.example.readingapp.model.MBook
import androidx.compose.ui.tooling.preview.Preview
import com.example.readingapp.ui.theme.AppTheme
import com.example.readingapp.ui.theme.Ink
import com.example.readingapp.ui.theme.InkBase

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
            style = MaterialTheme.typography.labelSmall
        )
        Column {
            Text(
                text = book.title.orEmpty(),
                color = ink,
                style = MaterialTheme.typography.headlineSmall,
                maxLines = 5,
                overflow = TextOverflow.Ellipsis
            )
            book.authors?.firstOrNull()?.let { author ->
                Text(
                    text = author,
                    color = ink.copy(alpha = 0.75f),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun ShelfVolume(
    book: MBook,
    cloth: Color,
    ink: Color,
    width: Dp,
    height: Dp,
    pulled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        if (pulled) {
            PulledBook(book = book, cloth = cloth, onClick = onClick)
        } else {
            BookSpine(
                book = book,
                cloth = cloth,
                ink = ink,
                width = width,
                height = height,
                onClick = onClick
            )
        }
    }
}

@Composable
fun BookSpine(
    book: MBook,
    cloth: Color,
    ink: Color,
    width: Dp,
    height: Dp,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(width)
            .height(height)
            .shadow(
                elevation = 3.dp,
                shape = ShelfSpineShape,
                ambientColor = Color(0x221C1917),
                spotColor = Color(0x331C1917)
            )
            .clip(ShelfSpineShape)
            .background(cloth)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxHeight()
                .width(3.dp)
                .background(ink.copy(alpha = 0.16f))
        )
        Text(
            text = book.title.orEmpty(),
            color = ink,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .requiredWidth(height - 20.dp)
                .rotate(-90f)
        )
    }
}

@Composable
fun GhostSpine(height: Dp) {
    Box(
        modifier = Modifier
            .width(34.dp)
            .height(height)
            .clip(ShelfSpineShape)
            .background(Ink.copy(alpha = 0.06f))
    )
}

/** Text colour for a coloured book cloth. Fixed (not theme-aware): it sits on the cloth, not the page. */
internal fun Color.onCloth(): Color {
    val luminance = 0.299f * red + 0.587f * green + 0.114f * blue
    return if (luminance > 0.55f) InkBase else Color.White
}

@Preview(name = "Pulled Book Preview")
@Composable
fun PulledBookPreview() {
    AppTheme {
        PulledBook(
            book = MBook(title = "The Great Gatsby", authors = listOf("F. Scott Fitzgerald")),
            cloth = Color(0xFF5C6BC0),
            onClick = {}
        )
    }
}

@Preview(name = "Book Spine Preview")
@Composable
fun BookSpinePreview() {
    AppTheme {
        val cloth = Color(0xFFAB47BC)
        BookSpine(
            book = MBook(title = "To Kill a Mockingbird", authors = listOf("Harper Lee")),
            cloth = cloth,
            ink = cloth.onCloth(),
            width = 34.dp,
            height = 160.dp,
            onClick = {}
        )
    }
}

@Preview(name = "Ghost Spine Preview")
@Composable
fun GhostSpinePreview() {
    AppTheme {
        GhostSpine(height = 140.dp)
    }
}
