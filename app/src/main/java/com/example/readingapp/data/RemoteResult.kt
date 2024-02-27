package com.example.readingapp.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import javax.annotation.concurrent.Immutable

@Immutable
sealed interface RemoteResult<out T> {
    data class Success<T>(val data: T) : RemoteResult<T>
    data class Error(val exception: Throwable? = null) : RemoteResult<Nothing>
    data object Loading : RemoteResult<Nothing>
}

fun <T> Flow<T>.asResult(): Flow<RemoteResult<T>> {
    return this
        .map<T, RemoteResult<T>> { RemoteResult.Success(it) }
        .onStart { emit(RemoteResult.Loading) }
        .catch { emit(RemoteResult.Error(it)) }
}