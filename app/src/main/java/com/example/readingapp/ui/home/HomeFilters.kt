package com.example.readingapp.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.readingapp.R
import com.example.readingapp.ui.theme.AppTheme
import com.example.readingapp.ui.theme.CardBackground
import com.example.readingapp.ui.theme.Ink
import com.example.readingapp.ui.theme.OnInk

internal enum class ShelfFilter {
    All, WantToRead, Reading, Finished
}

/** The All / Want to read / Reading / Finished chips above the shelf. */
@Composable
internal fun HomeFilters(
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

/** The heading over the shelf: "Your shelf", or the filter's name. */
@Composable
internal fun shelfTitle(filter: ShelfFilter): String = when (filter) {
    ShelfFilter.All -> stringResource(R.string.home_shelf)
    else -> filter.label()
}
