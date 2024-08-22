package com.example.readingapp.ui.components

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.StarHalf
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material.icons.rounded.StarRate
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.readingapp.R
import com.example.readingapp.mainActivity
import com.example.readingapp.ui.theme.AppTheme
import com.example.readingapp.ui.theme.Beige
import com.example.readingapp.ui.theme.Purple
import com.example.readingapp.ui.theme.allColors
import kotlin.math.ceil
import kotlin.math.floor

@Composable
fun RowBookItem(
    modifier: Modifier = Modifier,
    context: Context = mainActivity(),
    title: String? = null,
    authors: String? = null,
    imgUrl: String? = null,
    rating: Double = 0.0,
    progress: BookStatus = BookStatus.LIBRARY,
    isSaved: Boolean = false,
    onClick: () -> Unit = {}
) {
    Card(
        backgroundColor = Color.Transparent,
        elevation = 0.dp,
        modifier = modifier
            .padding(vertical = AppTheme.spacing.smSpacing)
            .wrapContentHeight()
            .width(150.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .height(200.dp)
                    .fillMaxWidth()
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context).data(imgUrl).build(),
                    contentScale = ContentScale.Crop,
                    contentDescription = stringResource(R.string.cont_desc_book_image),
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.Center)
                )
                Icon(
                    imageVector = if (isSaved) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                    contentDescription = stringResource(R.string.cont_desc_favourite_icon),
                    tint = Beige,
                    modifier = Modifier
                        .size(40.dp)
                        .background(color = Purple.copy(alpha = 0.9f))
                        .align(Alignment.TopEnd)
                        .padding(AppTheme.spacing.xxsmSpacing)
                )
                BookProgressLabel(progress, Modifier.align(Alignment.BottomEnd))
            }
            title?.let {
                Text(
                    text = it,
                    style = AppTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = AppTheme.spacing.smSpacing)
                )
            }
            authors?.let {
                Text(
                    text = it,
                    style = AppTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = AppTheme.spacing.xxsmSpacing)
                )
            }
            BookRating(rating)
        }
    }
}

@Composable
fun ColumnBookItem(
    modifier: Modifier = Modifier,
    context: Context = mainActivity(),
    backgroundColor: Color = Color.White,
    title: String? = null,
    authors: List<String>? = null,
    imgUrl: String? = null,
    date: String? = null,
    genres: List<String>? = null,
    onClick: () -> Unit = {}
) {
    Card(
        elevation = 0.dp,
        backgroundColor = backgroundColor,
        modifier = modifier
            .padding(vertical = AppTheme.spacing.smSpacing)
            .wrapContentHeight()
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier
                .width(80.dp)
                .height(100.dp)
                .clip(RoundedCornerShape(AppTheme.spacing.xxsmSpacing))
            ) {
                Box(modifier = Modifier
                    .fillMaxSize()
                    .background(allColors.random()))
                AsyncImage(
                    model = ImageRequest.Builder(context).data(imgUrl).build(),
                    contentScale = ContentScale.Crop,
                    contentDescription = stringResource(R.string.cont_desc_book_image),
                    modifier = Modifier.fillMaxSize()
                )
            }
            Column(modifier = Modifier.padding(start = AppTheme.spacing.mdSpacing)) {
                title?.let {
                    Text(
                        text = it,
                        style = AppTheme.typography.titleMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(bottom = AppTheme.spacing.xxsmSpacing)
                    )
                }
                authors?.let {
                    Text(
                        text = buildAnnotatedString {
                            authors.forEachIndexed { i, a ->
                                if (i == 0) append(a)
                                else append(", $a")
                            }
                        },
                        style = AppTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(bottom = AppTheme.spacing.xxsmSpacing)
                    )
                }
                date?.let {
                    Text(
                        text = it,
                        style = AppTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(bottom = AppTheme.spacing.xxsmSpacing)
                    )
                }
                genres?.let {
                    Text(
                        text = buildAnnotatedString {
                            genres.forEachIndexed { i, g ->
                                if (i == 0) append(g)
                                else append(", $g")
                            }
                        },
                        style = AppTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun BookRating(
    rating: Double = 0.0,
    stars: Int = 5
) {
    val filledStars = floor(rating).toInt()
    val unfilledStars = (stars - ceil(rating)).toInt()
    val halfStar = !(rating.rem(1).equals(0.0))

    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = AppTheme.spacing.xsmSpacing)
    ) {
        repeat(filledStars) {
            Icon(
                imageVector = Icons.Rounded.StarRate,
                contentDescription = stringResource(R.string.cont_desc_filled_star_icon),
                modifier = Modifier.size(20.dp)
            )
        }
        if (halfStar) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.StarHalf,
                contentDescription = stringResource(R.string.cont_desc_half_filled_star_icon),
                modifier = Modifier.size(20.dp)
            )
        }
        repeat(unfilledStars) {
            Icon(
                imageVector = Icons.Rounded.StarBorder,
                contentDescription = stringResource(R.string.cont_desc_unfilled_star_icon),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun BookProgressLabel(progress: BookStatus, modifier: Modifier = Modifier) {
    Text(
        text = progress.statusText,
        style = AppTheme.typography.labelSmall,
        modifier = modifier
            .padding(top = AppTheme.spacing.xsmSpacing)
            .background(
                color = AppTheme.colors.tertiary,
                shape = RoundedCornerShape(topStart = AppTheme.spacing.lgSpacing)
            )
            .padding(vertical = AppTheme.spacing.xxsmSpacing, horizontal = AppTheme.spacing.smSpacing)
    )
}

@Preview
@Composable
fun RowBookItemPreview() {
    val context = LocalContext.current
    RowBookItem(context = context, title = "Book title", authors = "Author Name", rating = 3.5)
}

@Preview
@Composable
fun ColumnBookItemPreview() {
    val context = LocalContext.current
    ColumnBookItem(
        context = context,
        title = "Book title",
        authors = listOf("Author Name"),
        date = "24-02-2024",
        genres = listOf("Computers")
    )
}