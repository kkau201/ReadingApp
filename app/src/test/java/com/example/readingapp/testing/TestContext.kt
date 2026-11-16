package com.example.readingapp.testing

import android.content.Context
import androidx.annotation.StringRes
import com.example.readingapp.MainViewModel
import com.example.readingapp.nav.NavigationEvent
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher

/** What [fakeContext] returns for a string resource, so tests can assert which one was shown. */
fun str(@StringRes id: Int) = "res:$id"

/** A Context whose every string resource (with or without format args) resolves to [str]. */
fun fakeContext(): Context = mockk<Context>().also { context ->
    every { context.getString(any()) } answers { str(firstArg()) }
    every { context.getString(any(), *anyVararg()) } answers { str(firstArg()) }
}

/** Records every navigation event [main] emits into [into] for the rest of the test. */
@OptIn(ExperimentalCoroutinesApi::class)
fun TestScope.collectNavigation(main: MainViewModel, into: MutableList<NavigationEvent?>) {
    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
        main.navigationFlow.collect { into += it }
    }
}
