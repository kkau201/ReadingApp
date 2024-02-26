package com.example.readingapp.ui.components

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.StarHalf
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material.icons.rounded.StarRate
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.readingapp.R
import com.example.readingapp.mainActivity
import com.example.readingapp.ui.theme.AppTheme
import kotlin.math.ceil
import kotlin.math.floor

enum class BookProgress(val text: String) {
    NOT_STARTED("Not started"),
    IN_PROGRESS("In progress"),
    FINISHED("Finished")
}

@Composable
fun BookItem(
    modifier: Modifier = Modifier,
    context: Context = mainActivity(),
    title: String? = null,
    authors: String? = null,
    imgUrl: String? = null,
    rating: Double = 0.0,
    progress: BookProgress = BookProgress.NOT_STARTED,
    onClick: () -> Unit = {}
) {
    val displayMetrics = context.resources.displayMetrics
    val screenWidth = displayMetrics.widthPixels / displayMetrics.density

        Card(
            backgroundColor = AppTheme.colors.surface,
            shape = RoundedCornerShape(AppTheme.spacing.mdSpacing),
            modifier = modifier
                .padding(vertical = AppTheme.spacing.smSpacing)
                .height(280.dp)
                .width(200.dp)
                .clickable { onClick() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .width(screenWidth.dp - AppTheme.spacing.smSpacing * 2)
                    .padding(AppTheme.spacing.mdSpacing)
            ) {
                Box {
                    AsyncImage(
                        model = ImageRequest.Builder(context).data(imgUrl).build(),
                        contentScale = ContentScale.Crop,
                        contentDescription = stringResource(R.string.cont_desc_book_image),
                        modifier = Modifier
                            .height(150.dp)
                            .fillMaxWidth()
                            .align(Alignment.Center)
                            .clip(RoundedCornerShape(AppTheme.spacing.xsmSpacing))
                    )
                    Icon(
                        imageVector = Icons.Rounded.FavoriteBorder,
                        contentDescription = stringResource(R.string.cont_desc_favourite_icon),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(AppTheme.spacing.xxsmSpacing)
                    )
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
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.End) {
                BookProgressLabel(progress)
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
                modifier = Modifier.size(15.dp)
            )
        }
        if (halfStar) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.StarHalf,
                contentDescription = stringResource(R.string.cont_desc_half_filled_star_icon),
                modifier = Modifier.size(15.dp)
            )
        }
        repeat(unfilledStars) {
            Icon(
                imageVector = Icons.Rounded.StarBorder,
                contentDescription = stringResource(R.string.cont_desc_unfilled_star_icon),
                modifier = Modifier.size(15.dp)
            )
        }
    }
}

@Composable
fun BookProgressLabel(progress: BookProgress) {
    Text(
        text = progress.text,
        style = AppTheme.typography.labelSmall,
        modifier = Modifier
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
fun BookItemPreview() {
    BookItem(title = "Book title", authors = "Author Name", rating = 3.5)
}