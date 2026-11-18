package com.example.readingapp.network

import android.content.Context
import com.example.readingapp.common.ErrorType
import com.example.readingapp.utils.hasInternet
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class NetworkInterceptor @Inject constructor(
    @ApplicationContext val context: Context
) : Interceptor {
    @Throws(Exception::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        if (context.hasInternet().not()) {
            return fromCacheOrThrow(chain)
        }
        try {
            val builder: Request.Builder = chain.request().newBuilder()
            val response = chain.proceed(builder.build())

            val url = response.request.url.newBuilder().removeAllQueryParameters("key").build()
            val message = "code= ${response.code}, url= $url"

            if (response.code >= 500) {
                throw ErrorType.ServiceException(message)
            }
            else if (response.code >= 400) {
                throw ErrorType.DataRequestException(message)
            }
            return response
        } catch (ex: SocketTimeoutException) {
            throw ErrorType.TimeoutException(ex)
        } catch (ex: ErrorType.ServiceException) {
            throw ex
        } catch (ex: ErrorType.DataRequestException) {
            throw ex
        } catch (ex: IOException) {
            throw ErrorType.NetworkException(ex)
        } catch (ex: Exception) {
            throw ErrorType.UnknownNetworkException(ex)
        }
    }

    /** Offline: answers with the copy saved when it was last loaded, if there is one. */
    private fun fromCacheOrThrow(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder().cacheControl(OFFLINE).build()
        val response = try {
            chain.proceed(request)
        } catch (_: IOException) {
            throw ErrorType.NoInternetException
        }
        // OkHttp answers 504 when nothing is saved for this request.
        if (response.isSuccessful) return response
        response.close()
        throw ErrorType.NoInternetException
    }

    private companion object {
        /** Never touches the network, and accepts a saved copy up to this old. */
        val OFFLINE = CacheControl.Builder()
            .onlyIfCached()
            .maxStale(30, TimeUnit.DAYS)
            .build()
    }
}