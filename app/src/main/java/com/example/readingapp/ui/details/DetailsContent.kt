package com.example.readingapp.ui.details

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.readingapp.R
import com.example.readingapp.mainActivity
import com.example.readingapp.model.MBook
import com.example.readingapp.ui.components.ReadingAppButton
import com.example.readingapp.ui.theme.AppTheme
import com.example.readingapp.ui.theme.allColors
import com.example.readingapp.utils.fromHtmlToSpanned
import com.example.readingapp.utils.toAnnotatedString

@Composable
fun DetailsScreenContent(
    book: MBook,
    modifier: Modifier = Modifier,
    context: Context = mainActivity(),
    onUpdateBookClick: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = AppTheme.spacing.lgSpacing)
            .scrollable(scrollState, Orientation.Vertical)
    ) {
        Box(
            modifier = Modifier
                .width(160.dp)
                .height(280.dp)
                .padding(bottom = AppTheme.spacing.lgSpacing)
                .clip(RoundedCornerShape(AppTheme.spacing.smSpacing))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(allColors.random())
            )
            AsyncImage(
                model = ImageRequest.Builder(context).data(book.imgUrl).build(),
                contentScale = ContentScale.Crop,
                contentDescription = stringResource(R.string.cont_desc_book_image),
                modifier = Modifier.fillMaxSize()
                    .background(shape = RoundedCornerShape(AppTheme.spacing.smSpacing), color = Color.Transparent)
            )
        }
        book.title?.let {
            Text(
                text = it,
                textAlign = TextAlign.Center,
                style = AppTheme.typography.headlineMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(bottom = AppTheme.spacing.xxsmSpacing)
            )
        }
        book.authors?.let {
            Text(
                text = it.firstOrNull() ?: "Unknown",
                style = AppTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(bottom = AppTheme.spacing.lgSpacing)
            )
        }
        book.description?.let {
            Text(
                text = it.fromHtmlToSpanned().toAnnotatedString(),
                style = AppTheme.typography.bodyMedium,
                maxLines = 7,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(bottom = AppTheme.spacing.xxsmSpacing)
            )
        }
    }
}