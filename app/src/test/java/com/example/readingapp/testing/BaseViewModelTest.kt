package com.example.readingapp.testing

import androidx.annotation.StringRes
import com.example.readingapp.MainViewModel
import com.example.readingapp.R
import com.example.readingapp.nav.NavigationEvent
import com.example.readingapp.ui.components.DialogState
import kotlinx.coroutines.test.TestScope
import org.junit.Assert.assertEquals
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

    /** The dialog currently showing; fails the test if there isn't one. */
    protected fun dialog(): DialogState = mainViewModel.dialogFlow.value ?: error("No dialog shown")

    /** Asserts the standard error dialog (from BaseViewModel.showErrorDialog) is showing [bodyRes]. */
    protected fun assertErrorDialog(@StringRes bodyRes: Int) {
        assertEquals(str(R.string.error_title), dialog().title)
        assertEquals(str(bodyRes), dialog().message)
    }
}