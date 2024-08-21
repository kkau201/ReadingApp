package com.example.readingapp.ui.search

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.readingapp.R
import com.example.readingapp.common.LoadingState
import com.example.readingapp.common.ViewModelBinding
import com.example.readingapp.common.observeLifecycle
import com.example.readingapp.ui.components.ReadingAppBarNav
import com.example.readingapp.utils.keyboardAsState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private const val SEARCHBAR_OFFSET_MARGIN = 300f
private const val SEARCHBAR_MIN_Y_OFFSET = 50f
private const val DEFAULT_SEARCHBAR_Y_OFFSET = 900f
private const val TOP_MARGIN = 100

@Composable
fun SearchScreen(
    viewModel: SearchViewModel = hiltViewModel(),
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
) {
    ViewModelBinding(viewModel)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val keyboardController = LocalSoftwareKeyboardController.current

    // Clear focus if keyboard was hidden by user
    val isKeyboardOpen by keyboardAsState()
    val focusManager = LocalFocusManager.current
    if (!isKeyboardOpen) focusManager.clearFocus()

    ObserveLifecycleEvents(viewModel, lifecycleOwner)

    // Image height that acquired in runtime.
    var imageHeightInPx by remember { mutableIntStateOf(0) }

    // Offset value to move layout to top.
    var listOffset by remember { mutableFloatStateOf(0f) }
    var searchBarOffset by remember { mutableFloatStateOf(DEFAULT_SEARCHBAR_Y_OFFSET) }

    // Gap between image top and scrolling layout.
    val marginTopInPx = with(LocalDensity.current) { TOP_MARGIN.dp.toPx() }

    val coroutineScope = rememberCoroutineScope()

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                coroutineScope.launch(Dispatchers.Main) {
                    listOffset = (listOffset + delta).coerceIn(marginTopInPx, imageHeightInPx.toFloat())
                    searchBarOffset = (listOffset - SEARCHBAR_OFFSET_MARGIN).coerceIn(
                        SEARCHBAR_MIN_Y_OFFSET,
                        DEFAULT_SEARCHBAR_Y_OFFSET
                    )
                }
                return Offset(
                    0f,
                    if (listOffset < imageHeightInPx && listOffset > marginTopInPx) delta else 0f
                )
            }
        }
    }

    Box(
        modifier = Modifier
            .background(color = Color.White)
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection)
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    keyboardController?.hide()
                })
            },
    ) {
        Image(
            painter = painterResource(id = R.drawable.search_img),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned {
                    // Set the image height when composable positioned.
                    imageHeightInPx = it.size.height

                    // Set the offset according to image height
                    // To move layout to bottom during first composition.
                    listOffset = imageHeightInPx.toFloat()
                }
        )
        ReadingAppBarNav(title = stringResource(id = R.string.search_title), onNavIconClick = viewModel::navigateBack)
        SearchInput(
            currentInput = uiState.searchInput,
            keyboardController = keyboardController,
            onInputChange = viewModel::onInputChange,
            onDoneClick = viewModel::onDoneClick,
            modifier = Modifier.offset {
                IntOffset(0, searchBarOffset.roundToInt())
            }
        )

        when (val results = uiState.results) {
            is SearchResults.Success -> {
                viewModel.updateLoadingState(LoadingState.SUCCESS)
                SearchResults(
                    books = results.books,
                    offset = listOffset,
                    onBookClick = { bookId -> viewModel.onBookClick(bookId) }
                )
            }
            is SearchResults.Loading -> viewModel.updateLoadingState(LoadingState.LOADING)
            is SearchResults.Error -> {
                viewModel.updateLoadingState(LoadingState.FAILED)
                viewModel.showErrorDialog(results.exception)
            }
        }
    }
}

@Composable
fun ObserveLifecycleEvents(viewModel: SearchViewModel, lifecycleOwner: LifecycleOwner) {
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) viewModel.onLoad()
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    viewModel.observeLifecycle(lifecycleOwner)
}