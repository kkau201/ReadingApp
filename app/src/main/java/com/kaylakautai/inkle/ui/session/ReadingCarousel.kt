package com.kaylakautai.inkle.ui.session

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kaylakautai.inkle.model.MBook
import com.kaylakautai.inkle.ui.components.PulledBook
import com.kaylakautai.inkle.ui.components.PulledHeight
import com.kaylakautai.inkle.ui.components.PulledWidth
import com.kaylakautai.inkle.ui.components.shelfKey
import com.kaylakautai.inkle.ui.components.shelfSwatches
import kotlinx.coroutines.launch

/**
 * The books being read, one centred at a time. Swiping, or tapping a book at the side, selects
 * it; tapping the centred book opens it ([onBookClick], with its Google Books id). A selection
 * made elsewhere scrolls it into view.
 */
@Composable
internal fun ReadingCarousel(
    books: List<MBook>,
    selectedBookId: String?,
    onBookSelected: (String) -> Unit,
    onBookClick: (String) -> Unit
) {
    val start = books.indexOfFirst { it.shelfId() == selectedBookId }.coerceAtLeast(0)
    val pagerState = rememberPagerState(initialPage = start, pageCount = { books.size })
    val scope = rememberCoroutineScope()
    val swatches = shelfSwatches(books.map { it.shelfKey() })
    LaunchedEffect(pagerState, books) {
        snapshotFlow { pagerState.settledPage }.collect { page ->
            books.getOrNull(page)?.shelfId()?.let(onBookSelected)
        }
    }
    LaunchedEffect(selectedBookId, books) {
        val index = books.indexOfFirst { it.shelfId() == selectedBookId }
        if (index >= 0 && index != pagerState.currentPage && !pagerState.isScrollInProgress) {
            pagerState.scrollToPage(index)
        }
    }
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val side = ((maxWidth - PulledWidth) / 2).coerceAtLeast(0.dp)
        HorizontalPager(
            state = pagerState,
            pageSize = PageSize.Fixed(PulledWidth),
            contentPadding = PaddingValues(horizontal = side),
            pageSpacing = 16.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(PulledHeight + 16.dp)
        ) { page ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                val book = books[page]
                PulledBook(
                    book = book,
                    cloth = swatches[page].cloth,
                    onClick = {
                        if (page == pagerState.currentPage) {
                            book.googleBookId?.let(onBookClick)
                        } else {
                            scope.launch { pagerState.animateScrollToPage(page) }
                        }
                    }
                )
            }
        }
    }
}
