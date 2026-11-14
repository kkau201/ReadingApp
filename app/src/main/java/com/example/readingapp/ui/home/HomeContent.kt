package com.example.readingapp.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.readingapp.R
import com.example.readingapp.model.MBook
import com.example.readingapp.ui.components.BookStatus
import com.example.readingapp.ui.components.LibraryPageTitle
import com.example.readingapp.ui.components.ShelfSpineShape
import com.example.readingapp.ui.components.shelfKey
import com.example.readingapp.ui.components.shelfSwatches
import com.example.readingapp.ui.theme.AppTheme
import com.example.readingapp.ui.theme.HomeCover
import com.example.readingapp.ui.theme.HomeInk
import com.example.readingapp.ui.theme.HomeMuted
import com.example.readingapp.ui.user.update.Avatar

private val SpineGap = 6.dp
private val PulledWidth = 132.dp
private val ShelfEdge = Color(0xFFD3C3AB)
private val SpineWidths = listOf(36.dp, 42.dp, 48.dp, 54.dp)
private val SpineHeights = listOf(156.dp, 172.dp, 188.dp, 204.dp)

private enum class ShelfFilter {
    All, WantToRead, Reading, Finished
}

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
            .padding(top = 12.dp, bottom = 20.dp)
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
            modifier = Modifier.padding(start = AppTheme.spacing.mdSpacing, top = 28.dp, bottom = 16.dp)
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
private fun HomeHeader(
    displayName: String?,
    avatar: Avatar?,
    bookCount: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppTheme.spacing.mdSpacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)) {
            if (!displayName.isNullOrBlank()) {
                Text(
                    text = stringResource(R.string.home_greeting, displayName),
                    color = HomeMuted,
                    style = AppTheme.typography.labelLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            LibraryPageTitle(
                text = stringResource(R.string.home_title),
                modifier = Modifier.padding(top = 2.dp)
            )
            Text(
                text = pluralStringResource(R.plurals.home_book_count, bookCount, bookCount),
                color = HomeMuted,
                style = AppTheme.typography.bodyLarge,
            )
        }
        HomeAvatar(avatar = avatar, name = displayName)
    }
}

@Composable
private fun HomeAvatar(avatar: Avatar?, name: String?) {
    if (avatar != null) {
        Image(
            painter = painterResource(avatar.img),
            contentDescription = stringResource(R.string.cont_desc_profile_image),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .padding(start = 16.dp)
                .size(48.dp)
                .clip(CircleShape)
        )
    } else {
        val initial = name?.trim()?.firstOrNull()?.uppercase() ?: ""
        Column(
            modifier = Modifier
                .padding(start = 16.dp)
                .size(48.dp)
                .clip(CircleShape)
                .background(HomeCover),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = initial,
                color = HomeInk,
                style = AppTheme.typography.titleLarge
            )
        }
    }
}

@Composable
private fun HomeFilters(
    selected: ShelfFilter,
    onSelect: (ShelfFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = AppTheme.spacing.mdSpacing),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ShelfFilter.entries.forEach { filter ->
            val isSelected = filter == selected
            Text(
                text = filter.label(),
                color = if (isSelected) Color.White else HomeInk,
                style = AppTheme.typography.labelLarge,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isSelected) HomeInk else Color.White)
                    .clickable { onSelect(filter) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun ShelfFilter.label(): String = when (this) {
    ShelfFilter.All -> stringResource(R.string.home_filter_all)
    ShelfFilter.WantToRead -> stringResource(R.string.home_filter_want_to_read)
    ShelfFilter.Reading -> stringResource(R.string.home_filter_reading)
    ShelfFilter.Finished -> stringResource(R.string.home_filter_finished)
}

@Composable
private fun shelfTitle(filter: ShelfFilter): String = when (filter) {
    ShelfFilter.All -> stringResource(R.string.home_shelf)
    ShelfFilter.WantToRead -> stringResource(R.string.home_filter_want_to_read)
    ShelfFilter.Reading -> stringResource(R.string.home_filter_reading)
    ShelfFilter.Finished -> stringResource(R.string.home_filter_finished)
}

@Composable
private fun HomeSectionTitle(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        color = HomeInk,
        style = AppTheme.typography.titleLarge,
        modifier = modifier
    )
}

@Composable
private fun ShelfRow(
    books: List<MBook>,
    pulledKey: String?,
    onBookClick: (String) -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppTheme.spacing.mdSpacing)
    ) {
        val styles = shelfStyles(books)
        val shelves = packShelves(books, styles, pulledKey, maxWidth)
        Column(verticalArrangement = Arrangement.spacedBy(36.dp)) {
            shelves.forEach { indexes ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(SpineGap),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        indexes.forEach { index ->
                            val book = books[index]
                            ShelfVolume(
                                book = book,
                                style = styles[index],
                                pulled = pulledKey != null && book.shelfKey() == pulledKey,
                                onClick = { book.googleBookId?.let(onBookClick) }
                            )
                        }
                    }
                    ShelfLedge()
                }
            }
        }
    }
}

private fun packShelves(
    books: List<MBook>,
    styles: List<SpineStyle>,
    pulledKey: String?,
    maxWidth: Dp
): List<List<Int>> {
    val shelves = mutableListOf<List<Int>>()
    val current = mutableListOf<Int>()
    var used = 0.dp
    books.indices.forEach { index ->
        val width = if (pulledKey != null && books[index].shelfKey() == pulledKey) {
            PulledWidth
        } else {
            styles[index].width
        }
        if (current.isNotEmpty() && used + SpineGap + width > maxWidth) {
            shelves += current.toList()
            current.clear()
            used = 0.dp
        }
        if (current.isNotEmpty()) used += SpineGap
        current += index
        used += width
    }
    if (current.isNotEmpty()) shelves += current.toList()
    return shelves
}

@Composable
private fun ShelfVolume(
    book: MBook,
    style: SpineStyle,
    pulled: Boolean,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (pulled) {
            PulledBook(book = book, style = style, onClick = onClick)
        } else {
            BookSpine(book = book, style = style, onClick = onClick)
        }
    }
}

@Composable
private fun BookSpine(
    book: MBook,
    style: SpineStyle,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(style.width)
            .height(style.height)
            .shadow(elevation = 3.dp, shape = ShelfSpineShape, ambientColor = Color(0x221C1917), spotColor = Color(0x331C1917))
            .clip(ShelfSpineShape)
            .background(style.cloth)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxHeight()
                .width(3.dp)
                .background(style.ink.copy(alpha = 0.16f))
        )
        Text(
            text = book.title.orEmpty(),
            color = style.ink,
            style = AppTheme.typography.labelLarge,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .requiredWidth(style.height - 20.dp)
                .rotate(-90f)
        )
    }
}

@Composable
private fun PulledBook(
    book: MBook,
    style: SpineStyle,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(PulledWidth)
            .height(206.dp)
            .shadow(elevation = 10.dp, shape = RoundedCornerShape(6.dp), ambientColor = Color(0x221C1917), spotColor = Color(0x401C1917))
            .clip(RoundedCornerShape(6.dp))
            .background(style.cloth)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = stringResource(R.string.home_filter_reading).uppercase(),
            color = style.ink.copy(alpha = 0.72f),
            style = AppTheme.typography.labelSmall
        )
        Column {
            Text(
                text = book.title.orEmpty(),
                color = style.ink,
                style = AppTheme.typography.headlineSmall,
                maxLines = 5,
                overflow = TextOverflow.Ellipsis
            )
            book.authors?.firstOrNull()?.let { author ->
                Text(
                    text = author,
                    color = style.ink.copy(alpha = 0.75f),
                    style = AppTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

private fun shelfStyles(books: List<MBook>): List<SpineStyle> {
    return shelfSwatches(books.map { it.shelfKey() }).map { swatch ->
        SpineStyle(
            cloth = swatch.cloth,
            ink = swatch.cloth.onCloth(),
            width = SpineWidths[swatch.hash.mod(SpineWidths.size)],
            height = SpineHeights[(swatch.hash / 7).mod(SpineHeights.size)]
        )
    }
}

private fun Color.onCloth(): Color {
    val luminance = 0.299f * red + 0.587f * green + 0.114f * blue
    return if (luminance > 0.55f) HomeInk else Color.White
}

private data class SpineStyle(
    val cloth: Color,
    val ink: Color,
    val width: Dp,
    val height: Dp
)

@Composable
private fun EmptyShelf(
    title: String?,
    body: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppTheme.spacing.mdSpacing)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(SpineGap),
            verticalAlignment = Alignment.Bottom
        ) {
            GhostSpine(height = 124.dp)
            GhostSpine(height = 156.dp)
            GhostSpine(height = 138.dp)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 10.dp, bottom = 8.dp)
            ) {
                title?.let {
                    Text(
                        text = it,
                        color = HomeInk,
                        style = AppTheme.typography.titleLarge
                    )
                }
                Text(
                    text = body,
                    color = HomeMuted,
                    style = AppTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = if (title == null) 0.dp else 4.dp)
                )
            }
        }
        ShelfLedge()
    }
}

@Composable
private fun GhostSpine(height: Dp) {
    Box(
        modifier = Modifier
            .width(34.dp)
            .height(height)
            .clip(ShelfSpineShape)
            .background(HomeInk.copy(alpha = 0.06f))
    )
}

@Composable
private fun ShelfLedge() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .background(
                color = ShelfEdge,
                shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)
            )
    )
}
