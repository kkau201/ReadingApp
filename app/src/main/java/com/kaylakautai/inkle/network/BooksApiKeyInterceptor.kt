package com.kaylakautai.inkle.network

import android.content.Context
import com.kaylakautai.inkle.BuildConfig
import com.kaylakautai.inkle.utils.signingCertSha1
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class BooksApiKeyInterceptor @Inject constructor(
    @ApplicationContext private val context: Context,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val apiKey = BuildConfig.BOOKS_API_KEY
        if (apiKey.isBlank()) return chain.proceed(chain.request())

        val request = chain.request().newBuilder()
            .url(
                chain.request().url.newBuilder()
                    .addQueryParameter("key", apiKey)
                    .build()
            )
            .header("X-Android-Package", context.packageName)
            .header("X-Android-Cert", context.signingCertSha1())
            .build()
        return chain.proceed(request)
    }
}
