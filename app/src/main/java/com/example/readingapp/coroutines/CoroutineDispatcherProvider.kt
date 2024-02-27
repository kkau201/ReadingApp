package com.example.readingapp.coroutines

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

interface CoroutineDispatcherProvider {

    fun getMain(): CoroutineDispatcher

    fun getDefault(): CoroutineDispatcher

    fun getIo(): CoroutineDispatcher
}

class RealCoroutineDispatcherProvider @Inject constructor() : CoroutineDispatcherProvider {

    override fun getMain(): CoroutineDispatcher {
        return Dispatchers.Main
    }

    override fun getDefault(): CoroutineDispatcher {
        return Dispatchers.Default
    }

    override fun getIo(): CoroutineDispatcher {
        return Dispatchers.IO
    }
}