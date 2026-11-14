package com.example.readingapp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.readingapp.ui.theme.Ink
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun OutlineStar(
    modifier: Modifier = Modifier,
    color: Color = Ink.copy(alpha = 0.28f),
    filled: Boolean = false,
    size: Dp = 42.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val path = starPath()
        if (filled) {
            drawPath(path = path, color = color)
        } else {
            drawPath(
                path = path,
                color = color,
                style = Stroke(width = 1.6f, cap = StrokeCap.Round, join = StrokeJoin.Round)
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
