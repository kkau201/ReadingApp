package com.example.readingapp.ui.user

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.readingapp.R
import com.example.readingapp.model.MBook
import com.example.readingapp.model.asReadingGoal
import com.example.readingapp.ui.components.shelfKey
import com.example.readingapp.ui.components.shelfSwatches
import com.example.readingapp.ui.theme.AppTheme
import com.example.readingapp.ui.theme.HomeInk
import com.example.readingapp.ui.theme.HomeMuted
import com.example.readingapp.ui.theme.Yellow
import com.google.firebase.Timestamp
import java.util.Calendar
import kotlin.math.cos
import kotlin.math.sin

private const val StampColumns = 4
private val CardShape = RoundedCornerShape(20.dp)

@Composable
fun ReadingStampCard(
    books: List<MBook>,
    goal: Int,
    modifier: Modifier = Modifier
) {
    val year = Calendar.getInstance().get(Calendar.YEAR)
    val stampGoal = goal.asReadingGoal()
    val finished = books.finishedIn(year)
    val stamped = finished.take(stampGoal)
    val cloths = shelfSwatches(stamped.map { it.shelfKey() }).map { it.cloth }
    val filled = stamped.size
    val extra = (finished.size - stampGoal).coerceAtLeast(0)
    val complete = filled == stampGoal
    val description = stringResource(R.string.profile_stamp_progress, filled, stampGoal)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(Yellow)
            .semantics { contentDescription = description }
            .padding(horizontal = 20.dp, vertical = 22.dp)
    ) {
        Text(
            text = year.toString(),
            color = HomeMuted,
            style = AppTheme.typography.labelSmall
        )
        Text(
            text = stringResource(R.string.profile_stamp_title),
            color = HomeInk,
            style = AppTheme.typography.headlineSmall,
            modifier = Modifier.padding(top = 4.dp)
        )
        Text(
            text = if (complete) {
                stringResource(R.string.profile_stamp_complete)
            } else {
                stringResource(R.string.profile_stamp_goal, stampGoal)
            },
            color = HomeMuted,
            style = AppTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            (0 until stampGoal).chunked(StampColumns).forEach { indexes ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    indexes.forEach { index ->
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            ReadingStamp(cloth = cloths.getOrNull(index))
                        }
                    }
                    repeat(StampColumns - indexes.size) {
                        Box(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
        Text(
            text = description,
            color = HomeInk,
            style = AppTheme.typography.labelLarge,
            modifier = Modifier
                .padding(top = 18.dp)
                .align(Alignment.End)
        )
        if (extra > 0) {
            Text(
                text = pluralStringResource(R.plurals.profile_stamp_more, extra, extra),
                color = HomeMuted,
                style = AppTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
private fun ReadingStamp(cloth: Color?) {
    Box(
        modifier = Modifier.size(56.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(42.dp)) {
            drawPath(
                path = starPath(),
                color = HomeInk.copy(alpha = 0.28f),
                style = Stroke(width = 1.6f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }
        if (cloth != null) {
            Image(
                painter = painterResource(R.drawable.book_logo_three_no_bg),
                contentDescription = null,
                modifier = Modifier.size(52.dp)
            )
        }
    }
}

private fun DrawScope.starPath(points: Int = 8): Path {
    val center = Offset(size.width / 2f, size.height / 2f)
    val outer = size.minDimension / 2f
    val inner = outer * 0.46f
    val path = Path()
    val vertices = points * 2
    for (index in 0 until vertices) {
        val radius = if (index % 2 == 0) outer else inner
        val angle = -Math.PI / 2 + (Math.PI * index / points)
        val point = Offset(
            x = center.x + (cos(angle) * radius).toFloat(),
            y = center.y + (sin(angle) * radius).toFloat()
        )
        if (index == 0) path.moveTo(point.x, point.y) else path.lineTo(point.x, point.y)
    }
    path.close()
    return path
}

private fun List<MBook>.finishedIn(year: Int): List<MBook> {
    return mapNotNull { book ->
        val finished = book.finishedReading ?: return@mapNotNull null
        if (finished.year() != year) return@mapNotNull null
        book
    }.sortedBy { it.finishedReading?.seconds }
}

private fun Timestamp.year(): Int {
    return Calendar.getInstance().apply { time = toDate() }.get(Calendar.YEAR)
}
