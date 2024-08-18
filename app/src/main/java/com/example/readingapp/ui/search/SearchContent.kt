package com.example.readingapp.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.readingapp.R
import com.example.readingapp.model.MBook
import com.example.readingapp.ui.components.ColumnBookItem
import com.example.readingapp.ui.components.NoResultsLottie
import com.example.readingapp.ui.theme.AppTheme
import kotlin.math.roundToInt

@Composable
fun SearchInput(
    currentInput: String,
    keyboardController: SoftwareKeyboardController?,
    onInputChange: (String) -> Unit,
    onDoneClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = currentInput,
        onValueChange = onInputChange,
        leadingIcon = { Icon(Icons.Rounded.Search, stringResource(id = R.string.cont_desc_search_icon)) },
        placeholder = { Text(text = stringResource(R.string.start_searching_here)) },
        shape = RoundedCornerShape(AppTheme.spacing.lgSpacing),
        colors = TextFieldDefaults.textFieldColors(
            backgroundColor = Color.White,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        ),
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(
            onDone = {
                keyboardController?.hide()
                onDoneClick()
            }
        ),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppTheme.spacing.mdSpacing, vertical = AppTheme.spacing.smSpacing)
    )
}

@Composable
fun SearchResults(
    books: List<MBook>,
    offset: Float,
    onBookClick: (String) -> Unit
) {
    if (books.isEmpty()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxHeight()
        ) {
            NoResultsLottie(
                modifier = Modifier
                    .padding(top = AppTheme.spacing.lgSpacing)
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.CenterHorizontally)
            )
            Text(
                text = stringResource(R.string.no_results),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(top = AppTheme.spacing.smSpacing)
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.CenterHorizontally)
            )
        }
    }
    else {
        LazyColumn(
            contentPadding = PaddingValues(
                top = AppTheme.spacing.mdSpacing,
                bottom = 96.dp
            ),
            modifier = Modifier
                .fillMaxSize()
                .offset {
                    IntOffset(0, offset.roundToInt())
                }
                .background(Color.White, shape = RoundedCornerShape(AppTheme.spacing.lgSpacing))
                .padding(horizontal = AppTheme.spacing.mdSpacing)
        ) {
            items(
                items = books,
                key = { it.id }
            ) { book ->
                ColumnBookItem(
                    title = book.title,
                    authors = book.authors,
                    imgUrl = book.imgUrl,
                    date = book.pubDate,
                    genres = book.genres,
                    onClick = { book.googleBookId?.let(onBookClick) }
                )
            }
        }
    }
}