package com.example.readingapp.ui.details

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.readingapp.R
import com.example.readingapp.common.ViewModelBinding
import com.example.readingapp.common.observeLifecycle
import com.example.readingapp.mainActivity
import com.example.readingapp.model.MBook
import com.example.readingapp.ui.components.ReadingAppBarNav
import com.example.readingapp.ui.components.ReadingAppButton
import com.example.readingapp.ui.theme.AppTheme
import com.example.readingapp.ui.theme.allColors
import com.example.readingapp.utils.fromHtmlToSpanned
import com.example.readingapp.utils.toAnnotatedString
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@Suppress("UNUSED_PARAMETER")
@Destination
@Composable
fun DetailsScreen(
    bookId: String,
    viewModel: DetailsViewModel = hiltViewModel(),
    navigator: DestinationsNavigator,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
) {
    ViewModelBinding(viewModel = viewModel, navigator = navigator)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) viewModel.loadBook()
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    viewModel.observeLifecycle(lifecycleOwner)
    Scaffold(
        backgroundColor = AppTheme.colors.background,
        modifier = Modifier.fillMaxSize(),
        topBar = { ReadingAppBarNav(navIconTint = Color.Black, onNavIconClick = viewModel::navigateBack) }
    ) { padding ->
        uiState.book?.let {
            DetailsScreenContent(
                book = it,
                modifier = Modifier.padding(padding),
                onSaveBookClick = viewModel::saveBook
            )
        }
    }
}

@Composable
fun DetailsScreenContent(
    book: MBook,
    modifier: Modifier = Modifier,
    context: Context = mainActivity(),
    onSaveBookClick: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(AppTheme.spacing.lgSpacing)
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
        book.authors?.let {
            Text(
                text = it.firstOrNull() ?: "Unknown",
                style = AppTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(bottom = AppTheme.spacing.xxsmSpacing)
            )
        }
        book.title?.let {
            Text(
                text = it,
                style = AppTheme.typography.displayMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(bottom = AppTheme.spacing.xxsmSpacing)
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

        ReadingAppButton(
            text = stringResource(R.string.save_button),
            modifier = Modifier.fillMaxWidth(),
            onClick = onSaveBookClick
        )
    }
}