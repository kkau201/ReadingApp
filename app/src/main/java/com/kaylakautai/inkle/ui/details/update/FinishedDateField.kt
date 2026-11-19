package com.kaylakautai.inkle.ui.details.update

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.kaylakautai.inkle.R
import com.kaylakautai.inkle.ui.components.InkleFieldLabel
import com.kaylakautai.inkle.ui.components.InkleTextField
import com.kaylakautai.inkle.ui.theme.CardBackground
import com.kaylakautai.inkle.ui.theme.Cover
import com.kaylakautai.inkle.ui.theme.Ink
import com.kaylakautai.inkle.ui.theme.Muted
import com.kaylakautai.inkle.ui.theme.OnInk
import java.text.DateFormatSymbols

private val FieldShape = RoundedCornerShape(20.dp)

@Composable
internal fun FinishedDateField(
    year: Int,
    month: Int?,
    day: String,
    dayError: Boolean,
    onYearChanged: (Int) -> Unit,
    onMonthChanged: (Int?) -> Unit,
    onDayChanged: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        YearField(year = year, onYearChanged = onYearChanged)
        MonthField(month = month, onMonthChanged = onMonthChanged)
        if (month != null) {
            DayField(day = day, dayError = dayError, onDayChanged = onDayChanged)
        }
    }
}

@Composable
private fun YearField(
    year: Int,
    onYearChanged: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        InkleFieldLabel(text = stringResource(R.string.update_book_finished_year))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Cover, FieldShape)
                .clip(FieldShape)
                .background(CardBackground),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            IconButton(
                onClick = { onYearChanged(year - 1) },
                enabled = year > MIN_FINISHED_YEAR
            ) {
                Icon(
                    imageVector = Icons.Rounded.Remove,
                    contentDescription = stringResource(R.string.update_book_year_earlier),
                    tint = if (year > MIN_FINISHED_YEAR) Ink else Muted
                )
            }
            Text(
                text = year.toString(),
                color = Ink,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            IconButton(
                onClick = { onYearChanged(year + 1) },
                enabled = year < maxFinishedYear()
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = stringResource(R.string.update_book_year_later),
                    tint = if (year < maxFinishedYear()) Ink else Muted
                )
            }
        }
    }
}

@Composable
private fun MonthField(
    month: Int?,
    onMonthChanged: (Int?) -> Unit
) {
    val months =
        DateFormatSymbols.getInstance(LocalLocale.current.platformLocale).shortMonths.take(12)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        InkleFieldLabel(text = stringResource(R.string.update_book_finished_month))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ChoiceChip(
                text = stringResource(R.string.update_book_finished_not_sure),
                selected = month == null,
                onClick = { onMonthChanged(null) }
            )
            months.forEachIndexed { index, name ->
                val monthNumber = index + 1
                ChoiceChip(
                    text = name.trimEnd('.'),
                    selected = month == monthNumber,
                    onClick = { onMonthChanged(monthNumber) }
                )
            }
        }
    }
}

@Composable
private fun DayField(
    day: String,
    dayError: Boolean,
    onDayChanged: (String) -> Unit
) {
    InkleTextField(
        value = day,
        onValueChange = onDayChanged,
        label = stringResource(R.string.update_book_finished_day),
        placeholder = stringResource(R.string.update_book_finished_optional),
        error = if (dayError) stringResource(R.string.update_book_finished_error) else null,
        bordered = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
}

@Composable
private fun ChoiceChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Text(
        text = text,
        color = if (selected) OnInk else Ink,
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier
            .clip(CircleShape)
            .background(if (selected) Ink else CardBackground)
            .border(
                width = if (selected) 0.dp else 1.dp,
                color = Cover,
                shape = CircleShape
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    )
}

