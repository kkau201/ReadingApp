package com.kaylakautai.inkle.ui.search

import androidx.lifecycle.SavedStateHandle
import com.kaylakautai.inkle.common.DependencyContextWrapper
import com.kaylakautai.inkle.common.ErrorType
import com.kaylakautai.inkle.data.RemoteResult
import com.kaylakautai.inkle.model.MBook
import com.kaylakautai.inkle.nav.NavigateTo
import com.kaylakautai.inkle.nav.NavigationEvent
import com.kaylakautai.inkle.repo.BookRepository
import com.kaylakautai.inkle.testing.BaseViewModelTest
import com.kaylakautai.inkle.testing.TestData
import com.kaylakautai.inkle.testing.collectNavigation
import com.kaylakautai.inkle.testing.fakeContext
import com.kaylakautai.inkle.ui.destinations.DetailsScreenDestination
import com.kaylakautai.inkle.ui.search.SearchViewModel.Companion.SEARCH_DEBOUNCE_MS
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest : BaseViewModelTest() {

    private val bookRepository = mockk<BookRepository>()
    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var viewModel: SearchViewModel

    private val results get() = viewModel.results.value

    private val austenResults = listOf(TestData.readingBook, TestData.otherReadingBook)

    @Before
    fun setUp() {
        // Any query a test doesn't stub returns the full sample library.
        coEvery { bookRepository.getBooksByQuery(any()) } returns
            flowOf(RemoteResult.Loading, RemoteResult.Success(TestData.books))
        createViewModel(SavedStateHandle())
    }

    private fun createViewModel(handle: SavedStateHandle) {
        savedStateHandle = handle
        viewModel = SearchViewModel(
            DependencyContextWrapper(fakeContext()),
            bookRepository,
            dispatcherProvider,
            handle
        )
        viewModel.setMainViewModel(mainViewModel)
    }

    /** Collects results like the screen does; cancel the returned job to "leave" the screen. */
    private fun TestScope.showScreen(): Job =
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.results.collect {} }

    private fun TestScope.waitForTypingPause() = advanceTimeBy((SEARCH_DEBOUNCE_MS + 1).milliseconds)

    private fun TestScope.typeAndPause(text: String) {
        viewModel.onQueryChange(text)
        waitForTypingPause()
    }

    private fun givenResults(query: String, results: Flow<RemoteResult<List<MBook>>>) {
        coEvery { bookRepository.getBooksByQuery(query) } returns results
    }

    private fun givenResults(query: String, vararg results: RemoteResult<List<MBook>>) =
        givenResults(query, flowOf(*results))

    private fun assertSearchCount(count: Int) =
        coVerify(exactly = count) { bookRepository.getBooksByQuery(any()) }

    // region Idle

    @Test
    fun initialState_emptyQueryIdleAndNoSearch() = runTest {
        showScreen()
        waitForTypingPause()

        assertEquals("", viewModel.query.value)
        assertEquals(SearchResults.Idle, results)
        assertSearchCount(0)
    }

    @Test
    fun tooShortQuery_staysIdleWithoutSearching() = runTest {
        showScreen()

        typeAndPause("a")

        assertEquals(SearchResults.Idle, results)
        assertSearchCount(0)
    }

    @Test
    fun clearingTheQuery_goesBackToIdleImmediately() = runTest {
        showScreen()
        typeAndPause("austen")

        viewModel.onQueryChange("")
        runCurrent()

        assertEquals(SearchResults.Idle, results)
    }

    // endregion

    // region Typing

    @Test
    fun typing_searchesOnlyOnceTypingPauses() = runTest {
        givenResults("austen", RemoteResult.Success(austenResults))
        showScreen()

        viewModel.onQueryChange("austen")
        advanceTimeBy((SEARCH_DEBOUNCE_MS - 1).milliseconds)
        assertSearchCount(0)

        advanceTimeBy(2.milliseconds)
        coVerify(exactly = 1) { bookRepository.getBooksByQuery("austen") }
        assertEquals(SearchResults.Success(austenResults), results)
    }

    @Test
    fun typingQuickly_searchesOnlyTheFinalText() = runTest {
        showScreen()

        listOf("au", "aus", "aust", "austen").forEach {
            viewModel.onQueryChange(it)
            advanceTimeBy(100.milliseconds)
        }
        waitForTypingPause()

        assertSearchCount(1)
        coVerify { bookRepository.getBooksByQuery("austen") }
    }

    @Test
    fun query_isTrimmedBeforeSearching() = runTest {
        showScreen()

        typeAndPause("  austen ")

        coVerify { bookRepository.getBooksByQuery("austen") }
        assertEquals("  austen ", viewModel.query.value)
    }

    @Test
    fun addingOnlyWhitespace_doesNotSearchAgain() = runTest {
        showScreen()
        typeAndPause("austen")

        typeAndPause("austen ")

        assertSearchCount(1)
    }

    @Test
    fun query_isSavedForProcessDeath() {
        viewModel.onQueryChange("austen")

        assertEquals("austen", savedStateHandle.get<String>("query"))
        assertEquals("austen", viewModel.query.value)
    }

    @Test
    fun restoredQuery_isSearchedAgain() = runTest {
        givenResults("austen", RemoteResult.Success(austenResults))
        createViewModel(SavedStateHandle(mapOf("query" to "austen")))
        showScreen()

        waitForTypingPause()

        assertEquals("austen", viewModel.query.value)
        assertEquals(SearchResults.Success(austenResults), results)
    }

    // endregion

    // region Search key

    @Test
    fun searchKey_searchesWithoutWaiting() = runTest {
        showScreen()
        viewModel.onQueryChange("austen")

        viewModel.onSearchClick()
        runCurrent()

        assertSearchCount(1)
        // ...and the typing pause that follows doesn't search a second time.
        waitForTypingPause()
        assertSearchCount(1)
    }

    @Test
    fun searchKey_afterResultsShown_doesNotSearchAgain() = runTest {
        showScreen()
        typeAndPause("austen")

        viewModel.onSearchClick()
        runCurrent()

        assertSearchCount(1)
    }

    // endregion

    // region Results

    @Test
    fun search_showsLoadingThenBooks() = runTest {
        val response = CompletableDeferred<List<MBook>>()
        givenResults("austen", flow {
            emit(RemoteResult.Loading)
            emit(RemoteResult.Success(response.await()))
        })
        showScreen()

        typeAndPause("austen")
        assertEquals(SearchResults.Loading, results)

        response.complete(austenResults)
        runCurrent()
        assertEquals(SearchResults.Success(austenResults), results)
    }

    @Test
    fun noMatches_showsEmptySuccess() = runTest {
        givenResults("zzzz", RemoteResult.Success(emptyList()))
        showScreen()

        typeAndPause("zzzz")

        assertEquals(SearchResults.Success(emptyList()), results)
    }

    @Test
    fun requestFails_showsError() = runTest {
        val error = ErrorType.NetworkException(null)
        givenResults("austen", RemoteResult.Loading, RemoteResult.Error(error))
        showScreen()

        typeAndPause("austen")

        assertEquals(SearchResults.Error(error), results)
    }

    @Test
    fun repositoryThrows_showsErrorAndKeepsSearching() = runTest {
        val error = IllegalStateException("boom")
        coEvery { bookRepository.getBooksByQuery("austen") } throws error
        givenResults("emma", RemoteResult.Success(listOf(TestData.libraryBook)))
        showScreen()

        typeAndPause("austen")
        assertEquals(SearchResults.Error(error), results)

        // The failure doesn't break later searches.
        typeAndPause("emma")
        assertEquals(SearchResults.Success(listOf(TestData.libraryBook)), results)
    }

    @Test
    fun retry_runsTheSameQueryAgain() = runTest {
        givenResults("austen", RemoteResult.Error(ErrorType.NoInternetException))
        showScreen()
        typeAndPause("austen")
        assertEquals(SearchResults.Error(ErrorType.NoInternetException), results)

        givenResults("austen", RemoteResult.Success(austenResults))
        viewModel.onRetryClick()
        runCurrent()

        coVerify(exactly = 2) { bookRepository.getBooksByQuery("austen") }
        assertEquals(SearchResults.Success(austenResults), results)
    }

    @Test
    fun newQuery_cancelsTheSearchStillInFlight() = runTest {
        val slowResponse = CompletableDeferred<List<MBook>>()
        var emmaCancelled = false
        givenResults("emma", flow {
            emit(RemoteResult.Loading)
            try {
                emit(RemoteResult.Success(slowResponse.await()))
            } finally {
                emmaCancelled = !slowResponse.isCompleted
            }
        })
        givenResults("austen", RemoteResult.Success(austenResults))
        showScreen()

        typeAndPause("emma")
        typeAndPause("austen")
        slowResponse.complete(listOf(TestData.libraryBook))
        runCurrent()

        assertTrue(emmaCancelled)
        assertEquals(SearchResults.Success(austenResults), results)
    }

    @Test
    fun returningFromDetails_keepsResultsWithoutSearchingAgain() = runTest {
        givenResults("austen", RemoteResult.Success(austenResults))
        val screen = showScreen()
        typeAndPause("austen")

        // Open Details, stay a while, come back.
        screen.cancel()
        advanceTimeBy(60_000.milliseconds)
        showScreen()
        waitForTypingPause()

        assertSearchCount(1)
        assertEquals(SearchResults.Success(austenResults), results)
    }

    // endregion

    @Test
    fun onBookClick_navigatesToThatBooksDetails() = runTest {
        val events = mutableListOf<NavigationEvent?>()
        collectNavigation(mainViewModel, into = events)

        viewModel.onBookClick(TestData.readingBook.googleBookId!!)

        val event = events.filterIsInstance<NavigateTo>().single()
        assertEquals(
            DetailsScreenDestination(TestData.readingBook.googleBookId!!).route,
            event.to.route
        )
    }
}
