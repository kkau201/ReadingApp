package com.example.readingapp.data

data class DataOrException<T, Boolean, E: Exception> (
    var data: T? = null,
    var loading: Boolean? = null,
    var e: E? = null
) {
    fun getOrThrow(): T {
        if (e != null) throw e!!
        return data!!
    }
}