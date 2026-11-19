package com.kaylakautai.inkle.network

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Runs as an OkHttp network interceptor, so it rewrites responses before they're cached.
 *
 * Whatever caching Google Books asks for, successful responses are kept, so books looked at
 * before can still be shown offline (see [NetworkInterceptor]). `max-age=0` without
 * `must-revalidate` means they're never served without checking first while online, but may
 * be used stale when there's no connection.
 */
class CacheForOfflineInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        if (!response.isSuccessful) return response
        return response.newBuilder()
            .header("Cache-Control", "max-age=0")
            .removeHeader("Pragma")
            .removeHeader("Expires")
            .build()
    }
}
