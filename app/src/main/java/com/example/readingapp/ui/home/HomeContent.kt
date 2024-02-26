package com.example.readingapp.ui.home

import androidx.annotation.StringRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.readingapp.R
import com.example.readingapp.model.MBook
import com.example.readingapp.ui.components.RowBookItem
import com.example.readingapp.ui.theme.AppTheme
import com.example.readingapp.ui.theme.allColors

@Composable
fun HomeUserIntro(displayName: String?, modifier: Modifier = Modifier) {
    Text(
        text = buildAnnotatedString {
            append(stringResource(R.string.hello))
            displayName?.forEachIndexed { pos, char ->
                pushStyle(SpanStyle(color = allColors[pos % allColors.size]))
                append(char)
            }
        },
        fontWeight = FontWeight.Bold,
        style = AppTheme.typography.displaySmall,
        letterSpacing = 1.sp,
        lineHeight = 32.sp,
        modifier = modifier.padding(start = AppTheme.spacing.lgSpacing)
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeReadingRow(
    @StringRes title: Int,
    books: List<MBook>,
    onBookClick: (MBook) -> Unit
) {
    val listState = rememberLazyListState()
    val snap = rememberSnapFlingBehavior(lazyListState = listState)
    Column {
        Text(text = stringResource(title), style = AppTheme.typography.titleLarge, modifier = Modifier.padding(start = AppTheme.spacing.lgSpacing))
        if(books.isNotEmpty()){
            LazyRow(
                state = listState,
                flingBehavior = snap
            ) {
                item { Spacer(modifier = Modifier.width(AppTheme.spacing.lgSpacing)) }
                items(books) { book ->
                    RowBookItem(
                        title = book.title,
                        authors = book.authors,
                        imgUrl = book.imgUrl,
                        onClick = { onBookClick(book) },
                        modifier = Modifier.padding(end = AppTheme.spacing.smSpacing)
                    )
                }
            }
        }
    }
}
