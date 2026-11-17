package com.example.readingapp.testing

import com.example.readingapp.MainViewModel
import com.example.readingapp.nav.NavigationEvent
import kotlinx.coroutines.test.TestScope
import org.junit.Rule

open class BaseViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    val dispatcherProvider: TestDispatcherProvider
        get() = TestDispatcherProvider(mainDispatcherRule.testDispatcher)

    val mainViewModel = MainViewModel()

    val navigationEvents = mutableListOf<NavigationEvent?>()

    /** Records navigation events from [mainViewModel] into [navigationEvents] (or specified list). */
    protected fun TestScope.collectNavigation(into: MutableList<NavigationEvent?> = navigationEvents) {
        collectNavigation(mainViewModel, into)
    }
}