package com.example.readingapp.ui.details

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.readingapp.R
import com.example.readingapp.model.MBook
import com.example.readingapp.ui.components.BookStatus
import com.example.readingapp.ui.components.shelfKey
import com.example.readingapp.ui.components.shelfSwatches
import com.example.readingapp.ui.theme.AppTheme
import com.example.readingapp.ui.theme.Ink
import com.example.readingapp.ui.theme.Muted
import com.example.readingapp.utils.fromHtmlToSpanned
import com.example.readingapp.utils.toAnnotatedString

private val CoverShape = RoundedCornerShape(6.dp)

@Composable
fun DetailsScreenContent(
    book: MBook,
    savedStatus: BookStatus?,
    onStatusClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cloth = shelfSwatches(listOf(book.shelfKey())).first().cloth
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = AppTheme.spacing.mdSpacing)
            .padding(top = 12.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            BookHeading(
                book = book,
                cloth = cloth,
                savedStatus = savedStatus,
                onStatusClick = onStatusClick
            )
            val genres = book.genreTags()
            if (genres.isNotEmpty()) {
                GenreTags(genres)
            }
        }
        book.description?.takeIf { it.isNotBlank() }?.let { description ->
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.details_about),
                    color = Ink,
                    style = AppTheme.typography.titleLarge
                )
                Text(
                    text = description.fromHtmlToSpanned().toAnnotatedString(),
                    color = Ink,
                    style = AppTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
private fun BookHeading(
    book: MBook,
    cloth: Color,
    savedStatus: BookStatus?,
    onStatusClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        BookCover(book = book, cloth = cloth)
        Column(
            modifier = Modifier.padding(start = 20.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = book.title.orEmpty(),
                color = Ink,
                style = AppTheme.typography.headlineSmall
            )
            val author = book.authors?.filter { it.isNotBlank() }?.joinToString(", ")
            if (!author.isNullOrBlank()) {
                Text(
                    text = author,
                    color = Muted,
                    style = AppTheme.typography.titleMedium
                )
            }
            val meta = bookMeta(book)
            if (meta.isNotBlank()) {
                Text(
                    text = meta,
                    color = Muted,
                    style = AppTheme.typography.bodySmall
                )
            }
            savedStatus?.let { status ->
                Text(
                    text = status.shelfLabel(),
                    color = Ink,
                    style = AppTheme.typography.labelLarge,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .clickable(onClick = onStatusClick)
                )
            }
        }
    }
}

@Composable
private fun bookMeta(book: MBook): String {
    return listOfNotNull(
        book.pubDate?.publishedYear(),
        book.pageCount?.takeIf { it > 0 }?.let { stringResource(R.string.home_pages, it) }
    ).joinToString(" · ")
}

@Composable
private fun BookCover(
    book: MBook,
    cloth: Color
) {
    Box(
        modifier = Modifier
            .width(112.dp)
            .height(168.dp)
            .shadow(
                elevation = 6.dp,
                shape = CoverShape,
                ambientColor = Color(0x221C1917),
                spotColor = Color(0x331C1917)
            )
            .clip(CoverShape)
            .background(cloth)
    ) {
        val imageUrl = book.imgUrl?.takeIf { it.isNotBlank() } ?: return
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current).data(imageUrl).build(),
            contentDescription = stringResource(R.string.cont_desc_book_image),
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun GenreTags(genres: List<String>) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        genres.forEach { genre ->
            Text(
                text = genre,
                color = Ink,
                style = AppTheme.typography.labelLarge,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.White)
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            )
        }
    }
}

private fun MBook.genreTags(): List<String> {
    return genres.orEmpty()
        .flatMap { it.split("/") }
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .distinct()
}

private fun String.publishedYear(): String {
    val year = take(4)
    return if (year.length == 4 && year.all { it.isDigit() }) year else this
}

@Composable
private fun BookStatus.shelfLabel(): String = when (this) {
    BookStatus.LIBRARY -> stringResource(R.string.home_filter_want_to_read)
    BookStatus.READING -> stringResource(R.string.home_filter_reading)
    BookStatus.FINISHED -> stringResource(R.string.home_filter_finished)
}
