package com.kaylakautai.inkle.data

/** A read's result: [data], or the error [e] it failed with. */
data class DataOrException<T, E : Exception>(
    var data: T? = null,
    var e: E? = null
) {
    fun getOrThrow(): T {
        if (e != null) throw e!!
        return data!!
    }
}