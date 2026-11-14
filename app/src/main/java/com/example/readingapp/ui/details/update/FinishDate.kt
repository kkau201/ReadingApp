package com.example.readingapp.ui.details.update

import com.example.readingapp.model.MBook
import com.example.readingapp.ui.components.BookStatus
import com.google.firebase.Timestamp
import java.text.DateFormatSymbols
import java.util.Calendar
import java.util.Date
import java.util.Locale

internal data class FinishedOn(
    val year: Int,
    val month: Int?,
    val day: Int?,
    val display: String
) {
    fun toTimestamp(): Timestamp {
        return Timestamp(Date(localNoon(year, (month ?: 1) - 1, day ?: 1)))
    }
}

internal data class FinishedParts(
    val year: Int,
    val month: Int?,
    val day: String
)

internal const val MIN_FINISHED_YEAR = 1900

internal fun maxFinishedYear(): Int = Calendar.getInstance().get(Calendar.YEAR)

internal fun currentFinishedParts(): FinishedParts {
    return FinishedParts(year = maxFinishedYear(), month = null, day = "")
}

internal fun MBook?.finishedParts(): FinishedParts {
    val stored = this?.finishedOn?.takeIf { it.isNotBlank() }?.let(::parseFinishedOn)?.toParts()
    return stored ?: this?.finishedReading?.toFinishedParts() ?: currentFinishedParts()
}

internal fun MBook.finishedDateLabel(): String? {
    if (bookStatus != BookStatus.FINISHED) return null
    val parts = finishedParts()
    val monthName = parts.month?.let { month ->
        DateFormatSymbols.getInstance(Locale.getDefault()).months
            .getOrNull(month - 1)
            ?.takeIf { it.isNotBlank() }
            ?.trimEnd('.')
    }
    val dayNumber = parts.day.toIntOrNull()
    return when {
        monthName == null -> parts.year.toString()
        dayNumber == null -> "$monthName ${parts.year}"
        else -> "$dayNumber $monthName ${parts.year}"
    }
}

internal fun Timestamp.toFinishedParts(): FinishedParts {
    val date = Calendar.getInstance().apply { time = toDate() }
    return FinishedParts(
        year = date.get(Calendar.YEAR),
        month = date.get(Calendar.MONTH) + 1,
        day = date.get(Calendar.DAY_OF_MONTH).toString()
    )
}

internal fun FinishedOn.toParts(): FinishedParts {
    return FinishedParts(year = year, month = month, day = day?.toString().orEmpty())
}

internal fun FinishedParts.toFinishedOn(): FinishedOn? {
    val chosenMonth = month
    val dayNumber = when {
        chosenMonth == null || day.isBlank() -> null
        else -> day.toIntOrNull() ?: return null
    }
    if (dayNumber != null && chosenMonth != null && !isValidDay(dayNumber, chosenMonth, year)) {
        return null
    }
    return finishedOn(year, chosenMonth, dayNumber)
}

internal fun finishedOn(year: Int, month: Int?, day: Int?): FinishedOn {
    val display = when {
        month == null -> year.toString()
        day == null -> "%02d/%d".format(Locale.UK, month, year)
        else -> "%02d/%02d/%d".format(Locale.UK, day, month, year)
    }
    return FinishedOn(year = year, month = month, day = day, display = display)
}

internal fun parseFinishedOn(input: String): FinishedOn? {
    val parts = input.trim().split("/").map { it.trim() }.filter { it.isNotEmpty() }
    return when (parts.size) {
        1 -> {
            val year = parts[0].toYear() ?: return null
            FinishedOn(year = year, month = null, day = null, display = year.toString())
        }
        2 -> {
            val month = parts[0].toMonth() ?: return null
            val year = parts[1].toYear() ?: return null
            FinishedOn(
                year = year,
                month = month,
                day = null,
                display = "%02d/%d".format(Locale.UK, month, year)
            )
        }
        3 -> {
            val day = parts[0].toIntOrNull() ?: return null
            val month = parts[1].toMonth() ?: return null
            val year = parts[2].toYear() ?: return null
            if (!isValidDay(day, month, year)) return null
            FinishedOn(
                year = year,
                month = month,
                day = day,
                display = "%02d/%02d/%d".format(Locale.UK, day, month, year)
            )
        }
        else -> null
    }
}

internal fun sameLocalDay(first: Timestamp?, second: Timestamp?): Boolean {
    if (first == null || second == null) return first == null && second == null
    val left = Calendar.getInstance().apply { time = first.toDate() }
    val right = Calendar.getInstance().apply { time = second.toDate() }
    return left.get(Calendar.YEAR) == right.get(Calendar.YEAR) &&
        left.get(Calendar.DAY_OF_YEAR) == right.get(Calendar.DAY_OF_YEAR)
}

private fun String.toYear(): Int? {
    val year = toIntOrNull() ?: return null
    return year.takeIf { length == 4 && it in 1000..9999 }
}

private fun String.toMonth(): Int? = toIntOrNull()?.takeIf { it in 1..12 }

private fun isValidDay(day: Int, month: Int, year: Int): Boolean {
    if (day !in 1..31) return false
    val calendar = Calendar.getInstance().apply {
        clear()
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month - 1)
        set(Calendar.DAY_OF_MONTH, 1)
    }
    return day <= calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
}

private fun localNoon(year: Int, month: Int, day: Int): Long {
    return Calendar.getInstance().apply {
        set(year, month, day, 12, 0, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}
