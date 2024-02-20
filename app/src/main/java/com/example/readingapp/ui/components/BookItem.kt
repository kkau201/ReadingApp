package com.example.readingapp.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.readingapp.ui.theme.AppTheme
import kotlin.math.ceil
import kotlin.math.floor

@Composable
fun BookItem(
    title: String? = null,
    authors: String? = null,
    rating: Double = 0.0,
    onClick: () -> Unit = {}
) {
    val displayMetrics = LocalContext.current.resources.displayMetrics
    val screenWidth = displayMetrics.widthPixels / displayMetrics.density

    Card(
        backgroundColor = AppTheme.colors.surface,
        shape = RoundedCornerShape(AppTheme.spacing.mdSpacing),
        modifier = Modifier
            .padding(AppTheme.spacing.smSpacing)
            .height(250.dp)
            .width(200.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .width(screenWidth.dp - AppTheme.spacing.smSpacing * 2)
                .padding(AppTheme.spacing.mdSpacing)
        ) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Image(
                    painter = rememberAsyncImagePainter(""),
                    contentDescription = "Book Image",
                    modifier = Modifier
                        .height(120.dp)
                        .width(120.dp)
                        .padding(AppTheme.spacing.xxsmSpacing)
                )
                Icon(Icons.Rounded.FavoriteBorder, contentDescription = "Favourite Icon")
            }
            title?.let {
                Text(
                    text = it,
                    style = AppTheme.typography.titleSmall,
                    modifier = Modifier.padding(top = AppTheme.spacing.smSpacing)
                )
            }
            authors?.let {
                Text(
                    text = it,
                    style = AppTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = AppTheme.spacing.xxsmSpacing)
                )
            }
            BookRating(rating)
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
                contentDescription = "Filled star icon",
                modifier = Modifier.size(15.dp)
            )
        }
        if (halfStar) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.StarHalf,
                contentDescription = "Filled star icon",
                modifier = Modifier.size(15.dp)
            )
        }
        repeat(unfilledStars) {
            Icon(
                imageVector = Icons.Rounded.StarBorder,
                contentDescription = "Unfilled star icon",
                modifier = Modifier.size(15.dp)
            )
        }
    }
}

@Preview
@Composable
fun BookItemPreview() {
    BookItem(title = "The Hunger Games", authors = "Suzanne Collins", rating = 3.5)
}