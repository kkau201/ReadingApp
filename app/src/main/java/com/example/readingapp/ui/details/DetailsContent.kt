package com.example.readingapp.ui.details

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.readingapp.R
import com.example.readingapp.model.MBook
import com.example.readingapp.ui.components.BookStatus
import com.example.readingapp.ui.components.OutlineStar
import com.example.readingapp.ui.components.shelfKey
import com.example.readingapp.ui.components.shelfSwatches
import com.example.readingapp.ui.details.update.finishedDateLabel
import com.example.readingapp.ui.theme.AppTheme
import com.example.readingapp.ui.theme.CardBackground
import com.example.readingapp.ui.theme.Cover
import com.example.readingapp.ui.theme.Ink
import com.example.readingapp.ui.theme.Muted
import com.example.readingapp.utils.fromHtmlToSpanned
import com.example.readingapp.utils.toAnnotatedString

private val CoverShape = RoundedCornerShape(6.dp)
private val ShelfCardShape = RoundedCornerShape(20.dp)

@Composable
fun DetailsScreenContent(
    book: MBook,
    savedBook: MBook?,
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
            BookHeading(book = book, cloth = cloth)
            val genres = book.genreTags()
            if (genres.isNotEmpty()) {
                GenreTags(genres)
            }
        }
        savedBook?.let { shelf ->
            YourShelfCard(book = shelf, onStatusClick = onStatusClick)
        }
        book.description?.takeIf { it.isNotBlank() }?.let { description ->
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.details_about),
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = description.fromHtmlToSpanned().toAnnotatedString(),
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
private fun BookHeading(
    book: MBook,
    cloth: Color
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
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.headlineSmall
            )
            val author = book.authors?.filter { it.isNotBlank() }?.joinToString(", ")
            if (!author.isNullOrBlank()) {
                Text(
                    text = author,
                    color = Muted,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            val meta = bookMeta(book)
            if (meta.isNotBlank()) {
                Text(
                    text = meta,
                    color = Muted,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun YourShelfCard(
    book: MBook,
    onStatusClick: () -> Unit
) {
    val status = book.bookStatus
    val finishedDate = book.finishedDateLabel()
    val rating = book.shelfRating()
    val notes = book.notes?.takeIf { it.isNotBlank() }
    val starColor = MaterialTheme.colorScheme.onBackground
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(ShelfCardShape)
            .border(1.dp, Cover, ShelfCardShape)
            .background(CardBackground)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.details_on_your_shelf),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleLarge
        )
        Column(
            modifier = Modifier.clickable(onClick = onStatusClick),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ShelfLabel(text = stringResource(R.string.update_book_status))
            Text(
                text = status.shelfLabel(),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyLarge
            )
            if (status == BookStatus.FINISHED && !finishedDate.isNullOrBlank()) {
                Text(
                    text = finishedDate,
                    color = Muted,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        if (rating != null) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                ShelfLabel(text = stringResource(R.string.update_book_rating))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    for (star in 1..5) {
                        val description = stringResource(R.string.cont_desc_star_number, star)
                        OutlineStar(
                            color = if (star <= rating) starColor else starColor.copy(alpha = 0.28f),
                            filled = star <= rating,
                            size = 22.dp,
                            modifier = Modifier.semantics { contentDescription = description }
                        )
                    }
                }
            }
        }
        if (notes != null) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                ShelfLabel(text = stringResource(R.string.update_book_notes))
                Text(
                    text = notes,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
private fun ShelfLabel(text: String) {
    Text(
        text = text,
        color = Muted,
        style = MaterialTheme.typography.labelLarge
    )
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
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(CardBackground)
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            )
        }
    }
}

private fun MBook.shelfRating(): Int? {
    val value = rating ?: return null
    val stars = value.toInt()
    if (stars !in 1..5 || value != stars.toDouble()) return null
    return stars
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
