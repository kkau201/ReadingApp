package com.example.readingapp.testing

import com.example.readingapp.coroutines.CoroutineDispatcherProvider
import kotlinx.coroutines.CoroutineDispatcher

/** Runs every "background" and "main" block on the same test dispatcher, so work finishes synchronously. */
class TestDispatcherProvider(private val dispatcher: CoroutineDispatcher) : CoroutineDispatcherProvider {
    override fun getMain() = dispatcher
    override fun getDefault() = dispatcher
    override fun getIo() = dispatcher
}
