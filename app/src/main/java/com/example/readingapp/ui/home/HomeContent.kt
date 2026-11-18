package com.example.readingapp.ui.home

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.readingapp.R
import com.example.readingapp.model.MBook
import com.example.readingapp.ui.components.shelfKey
import com.example.readingapp.ui.model.Avatar
import com.example.readingapp.ui.model.BookStatus
import com.example.readingapp.ui.theme.AppTheme

/**
 * The Home screen: a greeting, shelf filters, and the books on the shelf. Each part lives in
 * its own file in this package.
 */
@Composable
fun HomeContent(
    padding: PaddingValues,
    displayName: String?,
    avatar: Avatar?,
    readingList: List<MBook>,
    currentReadingList: List<MBook>,
    onBookClick: (String) -> Unit
) {
    var filterName by rememberSaveable { mutableStateOf(ShelfFilter.All.name) }
    val filter = ShelfFilter.valueOf(filterName)
    val shelf = when (filter) {
        ShelfFilter.All -> readingList
        ShelfFilter.WantToRead -> readingList.filter { it.bookStatus == BookStatus.LIBRARY }
        ShelfFilter.Reading -> currentReadingList
        ShelfFilter.Finished -> readingList.filter { it.bookStatus == BookStatus.FINISHED }
    }
    val featured = currentReadingList.firstOrNull()?.takeIf { filter == ShelfFilter.All }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(padding)
            .padding(top = 24.dp, bottom = 20.dp)
    ) {
        HomeHeader(
            displayName = displayName,
            avatar = avatar,
            bookCount = readingList.size
        )
        HomeFilters(
            selected = filter,
            onSelect = { filterName = it.name },
            modifier = Modifier.padding(top = 22.dp)
        )
        HomeSectionTitle(
            title = shelfTitle(filter),
            modifier = Modifier.padding(
                start = AppTheme.spacing.mdSpacing,
                top = 28.dp,
                bottom = 16.dp
            )
        )
        if (shelf.isEmpty()) {
            EmptyShelf(
                title = if (readingList.isEmpty()) stringResource(R.string.home_empty_title) else null,
                body = stringResource(
                    if (readingList.isEmpty()) R.string.home_empty_body else R.string.home_filter_empty
                )
            )
        } else {
            ShelfRow(
                books = shelf,
                pulledKey = featured?.shelfKey(),
                onBookClick = onBookClick
            )
        }
    }
}

@Composable
private fun HomeSectionTitle(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        color = MaterialTheme.colorScheme.onBackground,
        style = MaterialTheme.typography.titleLarge,
        modifier = modifier
    )
}
