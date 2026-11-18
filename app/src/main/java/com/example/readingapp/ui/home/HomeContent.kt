package com.example.readingapp.ui.home

import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.readingapp.R
import com.example.readingapp.model.MBook
import com.example.readingapp.ui.model.BookStatus
import com.example.readingapp.ui.components.GhostSpine
import com.example.readingapp.ui.components.LibraryPageTitle
import com.example.readingapp.ui.components.PulledHeight
import com.example.readingapp.ui.components.PulledWidth
import com.example.readingapp.ui.components.ShelfVolume
import com.example.readingapp.ui.components.onCloth
import com.example.readingapp.ui.components.shelfKey
import com.example.readingapp.ui.components.shelfSwatches
import com.example.readingapp.ui.theme.AppTheme
import com.example.readingapp.ui.theme.CardBackground
import com.example.readingapp.ui.theme.Cover
import com.example.readingapp.ui.theme.OnInk
import com.example.readingapp.ui.theme.PaperBase
import com.example.readingapp.ui.theme.Ink
import com.example.readingapp.ui.theme.Muted
import com.example.readingapp.ui.model.Avatar
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

private val SpineGap = 6.dp
private val ShelfHeight = PulledHeight
private val ShelfEdge = Color(0xFFD3C3AB)
private val SpineWidths = listOf(36.dp, 42.dp, 48.dp, 54.dp)
private val SpineHeights = listOf(156.dp, 172.dp, 188.dp, 204.dp)

// Books dropping onto the shelf. Tune the feel here.
/** How far above the shelf a book starts its drop. */
private val DropDistance = 56.dp
/** Gap between books landing, the first time the shelf fills in. */
private const val DropStaggerMillis = 100L
private val DropSpring = spring<Float>(
    dampingRatio = Spring.DampingRatioLowBouncy,
    stiffness = Spring.StiffnessMediumLow
)

/**
 * Books already shown on the shelf this app launch, so each drops in once: the shelf fills in
 * on first open, and later only newly saved books drop in (not every return to Home).
 */
private object ShelfArrivals {
    private val seen = mutableSetOf<String>()
    val noneYet: Boolean get() = seen.isEmpty()
    fun isNew(key: String) = key !in seen
    fun markSeen(key: String) {
        seen += key
    }
}

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
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            if (!displayName.isNullOrBlank()) {
                Text(
                    text = stringResource(R.string.home_greeting, displayName),
                    color = Muted,
                    style = MaterialTheme.typography.labelLarge,
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
                color = Muted,
                style = MaterialTheme.typography.bodyLarge,
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
                // Avatars are black line drawings on transparent: keep them on paper in dark theme.
                .background(PaperBase)
        )
    } else {
        val initial = name?.trim()?.firstOrNull()?.uppercase() ?: ""
        Column(
            modifier = Modifier
                .padding(start = 16.dp)
                .size(48.dp)
                .clip(CircleShape)
                .background(Cover),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = initial,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleLarge
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
                color = if (isSelected) OnInk else Ink,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isSelected) Ink else CardBackground)
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
        color = MaterialTheme.colorScheme.onBackground,
        style = MaterialTheme.typography.titleLarge,
        modifier = modifier
    )
}

@Composable
private fun ShelfRow(
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
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(ShelfHeight)
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(SpineGap),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        indexes.forEach { index ->
                            val book = books[index]
                            val bookKey = book.shelfKey()
                            // Keyed so a book keeps its drop state as others come and go.
                            key(bookKey) {
                                val drop = rememberShelfDrop(
                                    key = bookKey,
                                    // Shelves are packed in order, so index = landing order.
                                    delayMillis = if (firstFill) index * DropStaggerMillis else 0L,
                                    animate = animate
                                )
                                ShelfVolume(
                                    book = book,
                                    cloth = styles[index].cloth,
                                    ink = styles[index].ink,
                                    width = styles[index].width,
                                    height = styles[index].height,
                                    pulled = pulledKey != null && bookKey == pulledKey,
                                    modifier = Modifier.graphicsLayer {
                                        // Read here (not in composition), so the drop only
                                        // redraws, never recomposes.
                                        val progress = drop.value
                                        translationY = -progress * DropDistance.toPx()
                                        alpha = (1f - progress * 1.5f).coerceIn(0f, 1f)
                                    },
                                    onClick = { book.googleBookId?.let(onBookClick) }
                                )
                            }
                        }
                    }
                    ShelfLedge()
                }
            }
        }
    }
}

/**
 * 1 while a book is still above the shelf, settling to 0 once it has landed. Books already
 * shown this launch start at 0, so they don't drop again.
 */
@Composable
private fun rememberShelfDrop(
    key: String,
    delayMillis: Long,
    animate: Boolean
): Animatable<Float, AnimationVector1D> {
    val drop = remember(key) { Animatable(if (animate && ShelfArrivals.isNew(key)) 1f else 0f) }
    LaunchedEffect(key) {
        ShelfArrivals.markSeen(key)
        if (drop.value > 0f) {
            delay(delayMillis.milliseconds)
            drop.animateTo(0f, DropSpring)
        }
    }
    return drop
}

/** Off when the user has turned animations off (Accessibility), and in previews. */
@Composable
private fun rememberShelfMotionEnabled(): Boolean {
    val context = LocalContext.current
    val inPreview = LocalInspectionMode.current
    return remember(context, inPreview) {
        !inPreview && Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f
        ) != 0f
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
