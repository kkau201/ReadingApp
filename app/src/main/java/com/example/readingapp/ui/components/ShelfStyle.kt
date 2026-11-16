package com.example.readingapp.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.readingapp.model.MBook
import com.example.readingapp.ui.theme.Ink
import com.example.readingapp.ui.theme.allColors
import java.util.ArrayDeque

val ShelfSpineShape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)

fun MBook.shelfKey(): String = googleBookId ?: id ?: title.orEmpty()

data class ShelfSwatch(
    val cloth: Color,
    val hash: Int
)

fun shelfSwatches(keys: List<String>): List<ShelfSwatch> {
    val recent = ArrayDeque<Color>()
    return keys.map { key ->
        val hash = key.fold(0) { acc, char -> 31 * acc + char.code }
        val start = hash.mod(allColors.size)
        var cloth = allColors[start]
        var step = 0
        while (cloth in recent && step < allColors.size) {
            step++
            cloth = allColors[(start + step).mod(allColors.size)]
        }
        recent.addLast(cloth)
        if (recent.size > 2) recent.removeFirst()
        ShelfSwatch(cloth = cloth, hash = hash)
    }
}

@Composable
fun LibraryPageTitle(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.onBackground,
        style = MaterialTheme.typography.displaySmall,
        modifier = modifier
    )
}
