package com.kaylakautai.inkle.testing

import androidx.annotation.StringRes
import com.kaylakautai.inkle.MainViewModel
import com.kaylakautai.inkle.R
import com.kaylakautai.inkle.nav.NavigationEvent
import com.kaylakautai.inkle.ui.components.DialogState
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