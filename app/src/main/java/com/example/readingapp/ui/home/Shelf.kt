package com.example.readingapp.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.readingapp.model.MBook
import com.example.readingapp.ui.components.GhostSpine
import com.example.readingapp.ui.components.PulledHeight
import com.example.readingapp.ui.components.PulledWidth
import com.example.readingapp.ui.components.ShelfVolume
import com.example.readingapp.ui.components.onCloth
import com.example.readingapp.ui.components.shelfKey
import com.example.readingapp.ui.components.shelfSwatches
import com.example.readingapp.ui.theme.AppTheme
import com.example.readingapp.ui.theme.Muted

private val SpineGap = 6.dp
private val ShelfHeight = PulledHeight
private val ShelfEdge = Color(0xFFD3C3AB)
private val SpineWidths = listOf(36.dp, 42.dp, 48.dp, 54.dp)
private val SpineHeights = listOf(156.dp, 172.dp, 188.dp, 204.dp)

/**
 * The books as spines on shelves, filling each shelf before starting the next. The book
 * matching [pulledKey] is pulled out to show its cover.
 */
@Composable
internal fun ShelfRow(
    books: List<MBook>,
    pulledKey: String?,
    onBookClick: (String) -> Unit
) {
    val animate = rememberShelfMotionEnabled()
    // The very first time the shelf fills in, books land one after another.
    val firstFill = remember { ShelfArrivals.noneYet }
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppTheme.spacing.mdSpacing)
    ) {
        val styles = shelfStyles(books)
        val shelves = packShelves(books, styles, pulledKey, maxWidth)
        Column(verticalArrangement = Arrangement.spacedBy(36.dp)) {
            shelves.forEach { indexes ->
                Shelf {
                    indexes.forEach { index ->
                        val book = books[index]
                        // Keyed so a book keeps its drop state as others come and go.
                        key(book.shelfKey()) {
                            ShelfBook(
                                book = book,
                                style = styles[index],
                                pulled = pulledKey != null && book.shelfKey() == pulledKey,
                                // Shelves are packed in order, so index = landing order.
                                dropDelayMillis = if (firstFill) index * DropStaggerMillis else 0L,
                                animate = animate,
                                onBookClick = onBookClick
                            )
                        }
                    }
                }
            }
        }
    }
}

/** One shelf: whatever sits on it, bottom-aligned above the ledge. */
@Composable
private fun Shelf(content: @Composable RowScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(ShelfHeight)
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(SpineGap),
            verticalAlignment = Alignment.Bottom,
            content = content
        )
        ShelfLedge()
    }
}

/** A book on the shelf, dropping into place the first time it's shown. */
@Composable
private fun ShelfBook(
    book: MBook,
    style: SpineStyle,
    pulled: Boolean,
    dropDelayMillis: Long,
    animate: Boolean,
    onBookClick: (String) -> Unit
) {
    val drop = rememberShelfDrop(key = book.shelfKey(), delayMillis = dropDelayMillis, animate = animate)
    ShelfVolume(
        book = book,
        cloth = style.cloth,
        ink = style.ink,
        width = style.width,
        height = style.height,
        pulled = pulled,
        modifier = Modifier.graphicsLayer {
            // Read here (not in composition), so the drop only redraws, never recomposes.
            val progress = drop.value
            translationY = -progress * DropDistance.toPx()
            alpha = (1f - progress * 1.5f).coerceIn(0f, 1f)
        },
        onClick = { book.googleBookId?.let(onBookClick) }
    )
}

/** Ghost spines and a hint, for an empty shelf or a filter with no books. */
@Composable
internal fun EmptyShelf(
    title: String?,
    body: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppTheme.spacing.mdSpacing)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(ShelfHeight),
            horizontalArrangement = Arrangement.spacedBy(SpineGap),
            verticalAlignment = Alignment.Bottom
        ) {
            GhostSpine(height = 124.dp)
            GhostSpine(height = 156.dp)
            GhostSpine(height = 138.dp)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 10.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                title?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                Text(
                    text = body,
                    color = Muted,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = if (title == null) 0.dp else 4.dp)
                )
            }
        }
        ShelfLedge()
    }
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

/** Splits the books into shelves that each fit [maxWidth], keeping their order. */
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

/** Each book's colour and size, stable for the same book so the shelf doesn't reshuffle. */
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

private data class SpineStyle(
    val cloth: Color,
    val ink: Color,
    val width: Dp,
    val height: Dp
)
