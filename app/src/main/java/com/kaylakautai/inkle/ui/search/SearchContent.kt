package com.kaylakautai.inkle.ui.search

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kaylakautai.inkle.R
import com.kaylakautai.inkle.ui.components.InkleTextField
import com.kaylakautai.inkle.common.ErrorType
import com.kaylakautai.inkle.model.MBook
import com.kaylakautai.inkle.ui.components.InkleButton
import com.kaylakautai.inkle.ui.components.ShelfSpineShape
import com.kaylakautai.inkle.ui.components.shelfKey
import com.kaylakautai.inkle.ui.components.shelfSwatches
import com.kaylakautai.inkle.ui.theme.AppTheme
import com.kaylakautai.inkle.ui.theme.Ink
import com.kaylakautai.inkle.ui.theme.Muted

@Composable
fun SearchInput(
    currentInput: String,
    keyboardController: SoftwareKeyboardController?,
    onInputChange: (String) -> Unit,
    onDoneClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    InkleTextField(
        value = currentInput,
        onValueChange = onInputChange,
        leadingIcon = {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = stringResource(id = R.string.cont_desc_search_icon)
            )
        },
        placeholder = stringResource(R.string.start_searching_here),
        // The search bar is the one pill-shaped field.
        shape = CircleShape,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(
            onSearch = {
                keyboardController?.hide()
                onDoneClick()
            }
        ),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun SearchResultsContent(
    results: SearchResults,
    onBookClick: (String) -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (results) {
        SearchResults.Idle -> SearchIdleContent(modifier)
        SearchResults.Loading -> Box(
            contentAlignment = Alignment.TopCenter,
            modifier = modifier
                .fillMaxSize()
                .padding(top = 48.dp)
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.onBackground)
        }
        is SearchResults.Success -> SearchResults(results.books, onBookClick, modifier)
        is SearchResults.Error -> {
            val error = results.exception as? ErrorType
                ?: ErrorType.UnknownNetworkException(results.exception)
            SearchMessage(stringResource(error.body), modifier) {
                InkleButton(
                    text = stringResource(R.string.error_try_again),
                    onClick = onRetryClick,
                    modifier = Modifier.padding(top = 20.dp)
                )
            }
        }
    }
}

@Composable
private fun SearchIdleContent(modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            // The keyboard leaves little room; scroll rather than clip.
            .verticalScroll(rememberScrollState())
            .padding(horizontal = AppTheme.spacing.mdSpacing, vertical = 48.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.illustration_browse_shelves),
            // Decorative: the text below says what to do.
            contentDescription = null,
            colorFilter = ColorFilter.tint(Ink),
            // Wider than Session's diver: this art is square and has finer lines.
            modifier = Modifier.width(200.dp)
        )
        Text(
            text = stringResource(R.string.search_idle),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 28.dp)
        )
        Text(
            text = stringResource(R.string.search_idle_body),
            color = Muted,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .widthIn(max = 320.dp)
                .padding(top = 8.dp)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF7F4EF)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SearchIdlePreview() {
    AppTheme {
        SearchIdleContent(Modifier.background(MaterialTheme.colorScheme.background))
    }
}

@Composable
private fun SearchMessage(
    text: String,
    modifier: Modifier = Modifier,
    action: @Composable () -> Unit = {}
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = AppTheme.spacing.mdSpacing, vertical = 48.dp)
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        action()
    }
}

@Composable
fun SearchResults(
    books: List<MBook>,
    onBookClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (books.isEmpty()) {
        SearchMessage(stringResource(R.string.no_results), modifier)
        return
    }

    val cloths = shelfSwatches(books.map { it.shelfKey() }).map { it.cloth }
    LazyColumn(
        contentPadding = PaddingValues(top = 8.dp, bottom = AppTheme.spacing.mdSpacing),
        modifier = modifier.fillMaxSize()
    ) {
        itemsIndexed(books) { index, book ->
            SearchResultRow(
                book = book,
                cloth = cloths[index],
                onClick = { book.googleBookId?.let(onBookClick) }
            )
        }
    }
}

@Composable
private fun SearchResultRow(
    book: MBook,
    cloth: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = AppTheme.spacing.mdSpacing, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(14.dp)
                .height(56.dp)
                .clip(ShelfSpineShape)
                .background(cloth)
        )
        Column(
            modifier = Modifier.padding(start = 16.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
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
                    overflow = TextOverflow.Ellipsis
                )
            }
            val meta = listOfNotNull(
                book.pubDate?.publishedYear(),
                book.genres?.firstOrNull { it.isNotBlank() }
            ).joinToString(" · ")
            if (meta.isNotBlank()) {
                Text(
                    text = meta,
                    color = Muted,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private fun String.publishedYear(): String {
    val year = take(4)
    return if (year.length == 4 && year.all { it.isDigit() }) year else this
}
