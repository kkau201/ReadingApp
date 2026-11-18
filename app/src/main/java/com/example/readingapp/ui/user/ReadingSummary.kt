package com.example.readingapp.ui.user

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.readingapp.R
import com.example.readingapp.model.MBook
import com.example.readingapp.ui.model.BookStatus
import com.example.readingapp.ui.theme.Muted

/** How many books are in each shelf, pages finished, and what's being read now. */
@Composable
internal fun ReadingSummary(
    books: List<MBook>,
    nowReading: List<MBook>,
    modifier: Modifier = Modifier
) {
    val finished = books.count { it.bookStatus == BookStatus.FINISHED }
    val pages = books.filter { it.bookStatus == BookStatus.FINISHED }.sumOf { it.pageCount ?: 0 }
    ProfileCard(modifier = modifier, spacing = 20.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            ProfileSectionTitle(text = stringResource(R.string.profile_section_reading))
            Row(modifier = Modifier.fillMaxWidth()) {
                ReadingStat(
                    count = books.count { it.bookStatus == BookStatus.LIBRARY },
                    label = stringResource(R.string.home_filter_want_to_read),
                    modifier = Modifier.weight(1f)
                )
                ReadingStat(
                    count = books.count { it.bookStatus == BookStatus.READING },
                    label = stringResource(R.string.home_filter_reading),
                    modifier = Modifier.weight(1f)
                )
                ReadingStat(
                    count = finished,
                    label = stringResource(R.string.home_filter_finished),
                    modifier = Modifier.weight(1f)
                )
            }
            if (pages > 0) {
                Text(
                    text = pluralStringResource(R.plurals.profile_pages_finished, pages, pages),
                    color = Muted,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        if (nowReading.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            NowReading(books = nowReading)
        }
    }
}

@Composable
private fun ReadingStat(
    count: Int,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = count.toString(),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = label,
            color = Muted,
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

/** Up to three of the books being read, with their authors. */
@Composable
private fun NowReading(books: List<MBook>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ProfileSectionTitle(text = stringResource(R.string.profile_section_now))
        books.take(3).forEach { book ->
            Column {
                Text(
                    text = book.title.orEmpty(),
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                val author = book.authors?.filter { it.isNotBlank() }?.joinToString(", ")
                if (!author.isNullOrBlank()) {
                    Text(
                        text = author,
                        color = Muted,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}

/** Shown in place of the reading stats when the saved books couldn't be loaded. */
@Composable
internal fun BooksErrorCard(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    ProfileCard(modifier = modifier) {
        ProfileSectionTitle(text = stringResource(R.string.profile_section_reading))
        Text(
            text = stringResource(R.string.profile_books_error),
            color = Muted,
            style = MaterialTheme.typography.bodyMedium
        )
        ProfilePillButton(
            text = stringResource(R.string.error_try_again),
            onClick = onRetry,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF7F4EF)
@Composable
private fun BooksErrorCardPreview() {
    BooksErrorCard(onRetry = {}, modifier = Modifier.padding(24.dp))
}
