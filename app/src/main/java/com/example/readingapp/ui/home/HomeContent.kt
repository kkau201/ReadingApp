package com.example.readingapp.ui.home

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.readingapp.model.MBook
import com.example.readingapp.ui.components.BookItem
import com.example.readingapp.ui.theme.AppTheme
import com.example.readingapp.ui.theme.allColors

@Composable
fun HomeUserIntro(displayName: String?, modifier: Modifier = Modifier) {
    Text(
        text = buildAnnotatedString {
            append("Hello, ")
            displayName?.forEachIndexed { pos, char ->
                pushStyle(SpanStyle(color = allColors[pos % allColors.size]))
                append(char)
            }
        },
        fontWeight = FontWeight.Bold,
        style = AppTheme.typography.displaySmall,
        letterSpacing = 1.sp,
        lineHeight = 32.sp,
        modifier = modifier
    )
}

@Composable
fun HomeReadingRow(
    @StringRes title: Int,
    books: List<MBook>?
) {
    Column {
        Text(text = stringResource(title), style = AppTheme.typography.titleLarge)
        books?.let {
            LazyRow {
                items(books) {
                    BookItem(
                        title = it.title,
                        authors = it.authors
                    )
                }
            }
        }
    }
}
