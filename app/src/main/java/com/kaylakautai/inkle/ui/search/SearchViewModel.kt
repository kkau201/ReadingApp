package com.kaylakautai.inkle.ui.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.kaylakautai.inkle.common.BaseViewModel
import com.kaylakautai.inkle.common.DependencyContextWrapper
import com.kaylakautai.inkle.coroutines.CoroutineDispatcherProvider
import com.kaylakautai.inkle.data.RemoteResult
import com.kaylakautai.inkle.nav.NavigateTo
import com.kaylakautai.inkle.repo.BookRepository
import com.kaylakautai.inkle.ui.destinations.DetailsScreenDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    dependencyContextWrapper: DependencyContextWrapper,
    private val bookRepository: BookRepository,
    dispatchers: CoroutineDispatcherProvider,
    private val savedStateHandle: SavedStateHandle
) : BaseViewModel(dependencyContextWrapper) {

    /** The text in the search box, exactly as typed. Survives process death. */
    val query: StateFlow<String> = savedStateHandle.getStateFlow(KEY_QUERY, "")

    /** Fired by the keyboard's Search key to skip the typing delay. */
    private val searchNow = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    /** Bumped by Retry so the same query runs again. */
    private val retries = MutableStateFlow(0)

    /** The query to search for: once typing pauses, or straight away on the Search key. */
    @OptIn(FlowPreview::class)
    private val submittedQuery: Flow<String> = merge(
        query.debounce { if (it.isBlank()) 0L else SEARCH_DEBOUNCE_MS },
        searchNow.map { query.value }
    )
        .map { it.trim() }
        .distinctUntilChanged()

    @OptIn(ExperimentalCoroutinesApi::class)
    val results: StateFlow<SearchResults> = combine(submittedQuery, retries) { query, _ -> query }
        // A newer query (or retry) cancels the request still in flight.
        .flatMapLatest { query ->
            if (query.length < MIN_QUERY_LENGTH) flowOf(SearchResults.Idle) else search(query)
        }
        .flowOn(dispatchers.getIo())
        // Lazily (not WhileSubscribed): keeps the last query and its results while the screen
        // is hidden, e.g. behind Details, so coming back doesn't search again.
        .stateIn(viewModelScope, SharingStarted.Lazily, SearchResults.Idle)

    private fun search(query: String): Flow<SearchResults> =
        flow { emitAll(bookRepository.getBooksByQuery(query)) }
            .map { result ->
                when (result) {
                    is RemoteResult.Loading -> SearchResults.Loading
                    is RemoteResult.Success -> SearchResults.Success(result.data)
                    is RemoteResult.Error -> SearchResults.Error(result.exception)
                }
            }
            .catch { emit(SearchResults.Error(it)) }

    fun onQueryChange(input: String) {
        savedStateHandle[KEY_QUERY] = input
    }

    fun onSearchClick() {
        searchNow.tryEmit(Unit)
    }

    fun onRetryClick() {
        retries.value++
    }

    fun onBookClick(bookId: String) = navigate(NavigateTo(DetailsScreenDestination(bookId)))

    companion object {
        const val SEARCH_DEBOUNCE_MS = 400L
        const val MIN_QUERY_LENGTH = 2
        private const val KEY_QUERY = "query"
    }
}