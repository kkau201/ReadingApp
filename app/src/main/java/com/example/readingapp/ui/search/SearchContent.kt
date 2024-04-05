package com.example.readingapp.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import com.example.readingapp.R
import com.example.readingapp.model.MBook
import com.example.readingapp.ui.components.ColumnBookItem
import com.example.readingapp.ui.components.NoResultsLottie
import com.example.readingapp.ui.theme.AppTheme

@Composable
fun SearchInput(
    currentInput: String,
    keyboardController: SoftwareKeyboardController?,
    onInputChange: (String) -> Unit,
    onDoneClick: () -> Unit,
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppTheme.spacing.mdSpacing, vertical = AppTheme.spacing.smSpacing)
    )
}

@Composable
fun SearchResults(
    results: List<MBook>?,
    onBookClick: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(color = AppTheme.colors.onSurface, shape = RoundedCornerShape(AppTheme.spacing.lgSpacing))
            .padding(horizontal = AppTheme.spacing.mdSpacing)
    ) {
        if (results.isNullOrEmpty()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                NoResultsLottie(
                    modifier = Modifier
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
            LazyColumn {
                item { Spacer(modifier = Modifier.height(AppTheme.spacing.mdSpacing)) }
                items(
                    items = results,
                    key = { it.id }
                ) { book ->
                    ColumnBookItem(
                        title = book.title,
                        authors = book.authors,
                        imgUrl = book.imgUrl,
                        date = book.pubDate,
                        genres = book.genres,
                        onClick = { onBookClick(book.id) }
                    )
                }
            }
        }
    }
}