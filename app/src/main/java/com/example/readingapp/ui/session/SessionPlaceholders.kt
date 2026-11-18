package com.example.readingapp.ui.session

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.readingapp.R
import com.example.readingapp.ui.theme.CardBackground
import com.example.readingapp.ui.theme.Ink
import com.example.readingapp.ui.theme.Muted
import com.example.readingapp.ui.theme.OnInk

/** What the Session screen shows instead of books: a load error, or nothing being read. */

/** Shown when the books and journal couldn't load, instead of leaving the screen blank. */
@Composable
internal fun LoadErrorCard(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(CardBackground)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.session_load_error),
            color = Muted,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = stringResource(R.string.error_try_again),
            color = OnInk,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier
                .padding(top = 8.dp)
                .clip(CircleShape)
                .background(Ink)
                .clickable(onClick = onRetry)
                .padding(horizontal = 18.dp, vertical = 10.dp)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF7F4EF)
@Composable
private fun LoadErrorCardPreview() {
    LoadErrorCard(onRetry = {}, modifier = Modifier.padding(24.dp))
}

/** Empty state when no book is marked as reading: the diver illustration plus a hint. */
@Composable
internal fun NoReadingBooks(modifier: Modifier = Modifier) {
    // Fade in when first shown, so it eases in after loading instead of popping in.
    val visible = remember { MutableTransitionState(false).apply { targetState = true } }
    AnimatedVisibility(visibleState = visible, enter = fadeIn(), modifier = modifier) {
        NoReadingBooksContent()
    }
}

@Composable
private fun NoReadingBooksContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(R.drawable.illustration_dive_into_book),
            // Decorative: the text below says what this screen needs.
            contentDescription = null,
            colorFilter = ColorFilter.tint(Ink),
            modifier = Modifier.width(150.dp)
        )
        Text(
            text = stringResource(R.string.session_no_books_title),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 28.dp)
        )
        Text(
            text = stringResource(R.string.session_no_books),
            color = Muted,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .widthIn(max = 320.dp)
                .padding(top = 8.dp)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF7F4EF)
@Composable
private fun NoReadingBooksPreview() {
    NoReadingBooksContent(modifier = Modifier
        .fillMaxWidth()
        .padding(24.dp))
}
