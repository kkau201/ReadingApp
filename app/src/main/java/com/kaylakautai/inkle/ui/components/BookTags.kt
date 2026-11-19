package com.kaylakautai.inkle.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kaylakautai.inkle.R
import com.kaylakautai.inkle.model.MBook
import com.kaylakautai.inkle.ui.theme.AppTheme
import com.kaylakautai.inkle.ui.theme.CardBackground
import com.kaylakautai.inkle.ui.theme.Cover

/** Tags shown before the rest are folded into a "+N" chip. */
internal const val MAX_COLLAPSED_TAGS = 3

/** A book's tags: Google Books categories, with combined ones ("Fiction / Romance") split. */
fun MBook.tags(): List<String> = genres.orEmpty().toBookTags()

internal fun List<String>.toBookTags(): List<String> =
    flatMap { it.split("/") }
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .distinctBy { it.lowercase() }

/**
 * A book's tags as chips. Up to [MAX_COLLAPSED_TAGS] show on one line; beyond that, a "+N"
 * chip expands to show every tag (wrapping) and collapses again.
 *
 * Collapsed, the chips always stay on a single line (long tags are shortened with "…"), so
 * every collapsed row in a list is the same height. Expanded state survives scrolling and
 * configuration changes; in a lazy list, give items stable keys so each row keeps its own.
 *
 * @param dense Smaller chips, for list rows.
 */
@Composable
fun BookTags(
    tags: List<String>,
    modifier: Modifier = Modifier,
    dense: Boolean = false,
    initiallyExpanded: Boolean = false
) {
    if (tags.isEmpty()) return
    var expanded by rememberSaveable { mutableStateOf(initiallyExpanded) }
    val hiddenCount = tags.size - MAX_COLLAPSED_TAGS
    val gap = if (dense) 6.dp else 8.dp

    Box(modifier = modifier.animateContentSize()) {
        when {
            hiddenCount <= 0 -> OneLineChips(gap = gap, chips = {
                tags.forEach { TagChip(it, dense) }
            })
            expanded -> FlowRow(
                horizontalArrangement = Arrangement.spacedBy(gap),
                verticalArrangement = Arrangement.spacedBy(gap),
                itemVerticalAlignment = Alignment.CenterVertically
            ) {
                tags.forEach { TagChip(it, dense) }
                ExpandChip(expanded = true, hiddenCount = hiddenCount, dense = dense) {
                    expanded = false
                }
            }
            else -> OneLineChips(
                gap = gap,
                chips = { tags.take(MAX_COLLAPSED_TAGS).forEach { TagChip(it, dense) } },
                trailing = {
                    ExpandChip(expanded = false, hiddenCount = hiddenCount, dense = dense) {
                        expanded = true
                    }
                }
            )
        }
    }
}

@Composable
private fun TagChip(text: String, dense: Boolean) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.onBackground,
        style = chipTextStyle(dense),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .clip(CircleShape)
            .background(CardBackground)
            .padding(chipPadding(dense))
    )
}

/** "+N" while collapsed, "Show less" while expanded. */
@Composable
private fun ExpandChip(
    expanded: Boolean,
    hiddenCount: Int,
    dense: Boolean,
    onClick: () -> Unit
) {
    val label = if (expanded) {
        stringResource(R.string.tags_show_less)
    } else {
        stringResource(R.string.tags_more_count, hiddenCount)
    }
    val description = if (expanded) {
        stringResource(R.string.tags_show_fewer_description)
    } else {
        pluralStringResource(R.plurals.tags_show_more_description, hiddenCount, hiddenCount)
    }
    val state = stringResource(
        if (expanded) R.string.tags_state_expanded else R.string.tags_state_collapsed
    )
    Text(
        text = label,
        color = MaterialTheme.colorScheme.onBackground,
        style = chipTextStyle(dense),
        maxLines = 1,
        modifier = Modifier
            // 48dp touch target; the chip itself stays chip-sized.
            .minimumInteractiveComponentSize()
            .clip(CircleShape)
            .border(1.dp, Cover, CircleShape)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics {
                contentDescription = description
                stateDescription = state
            }
            .padding(chipPadding(dense))
    )
}

@Composable
private fun chipTextStyle(dense: Boolean): TextStyle =
    if (dense) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelLarge

private fun chipPadding(dense: Boolean) =
    if (dense) PaddingValues(horizontal = 10.dp, vertical = 5.dp)
    else PaddingValues(horizontal = 14.dp, vertical = 8.dp)

/**
 * Lays [chips] out on exactly one line, followed by [trailing]. If they don't all fit, the
 * longest are shortened (each down to the same width) so every chip still shows.
 */
@Composable
private fun OneLineChips(
    gap: Dp,
    chips: @Composable () -> Unit,
    trailing: @Composable () -> Unit = {}
) {
    Layout(contents = listOf(chips, trailing)) { (chipMeasurables, trailingMeasurables), constraints ->
        val loose = constraints.copy(minWidth = 0, minHeight = 0)
        val gapPx = gap.roundToPx()
        val trailingPlaceable = trailingMeasurables.firstOrNull()?.measure(loose)
        val reserved = trailingPlaceable?.let { it.width + gapPx } ?: 0
        val gaps = (chipMeasurables.size - 1).coerceAtLeast(0) * gapPx
        val natural = chipMeasurables.map { it.maxIntrinsicWidth(loose.maxHeight) }
        val widths = if (constraints.hasBoundedWidth) {
            fitChipWidths(natural, (constraints.maxWidth - reserved - gaps).coerceAtLeast(0))
        } else {
            natural
        }
        val placeables = chipMeasurables.mapIndexed { i, chip ->
            chip.measure(loose.copy(maxWidth = widths[i]))
        }
        val all = placeables + listOfNotNull(trailingPlaceable)
        val height = all.maxOfOrNull { it.height } ?: 0
        val width = (placeables.sumOf { it.width } + gaps + reserved)
            .coerceAtMost(constraints.maxWidth)
        layout(width, height) {
            var x = 0
            placeables.forEach {
                it.placeRelative(x, (height - it.height) / 2)
                x += it.width + gapPx
            }
            trailingPlaceable?.placeRelative(x, (height - trailingPlaceable.height) / 2)
        }
    }
}

/**
 * Widths for chips sharing [available] px on one line. Chips that fit keep their natural
 * width; if they don't all fit, the widest are capped at one shared width (the largest that
 * fits), so short tags stay whole and long ones are shortened evenly.
 */
internal fun fitChipWidths(natural: List<Int>, available: Int): List<Int> {
    if (natural.sum() <= available) return natural
    var remaining = available
    var count = natural.size
    var cap = 0
    for (width in natural.sorted()) {
        val share = remaining / count
        if (width <= share) {
            remaining -= width
            count--
        } else {
            cap = share
            break
        }
    }
    return natural.map { minOf(it, cap) }
}

// region Previews

private val PreviewTags = listOf(
    "History", "Civilization", "Biography & Autobiography", "Anthropology", "Evolution",
    "Social Science", "Philosophy", "Political", "Natural History", "Time", "World"
)

@Composable
private fun TagsPreview(tags: List<String>, dense: Boolean = false, expanded: Boolean = false) {
    AppTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .width(340.dp)
                .padding(16.dp)
        ) {
            BookTags(tags = tags, dense = dense, initiallyExpanded = expanded)
        }
    }
}

@PreviewLightDark
@Composable
private fun BookTagsNonePreview() = TagsPreview(emptyList())

@PreviewLightDark
@Composable
private fun BookTagsThreePreview() = TagsPreview(PreviewTags.take(3))

@PreviewLightDark
@Composable
private fun BookTagsFourPreview() = TagsPreview(PreviewTags.take(4))

@PreviewLightDark
@Composable
private fun BookTagsManyPreview() = TagsPreview(PreviewTags)

@PreviewLightDark
@Composable
private fun BookTagsManyExpandedPreview() = TagsPreview(PreviewTags, expanded = true)

@PreviewLightDark
@Composable
private fun BookTagsDenseManyPreview() = TagsPreview(PreviewTags, dense = true)

// endregion