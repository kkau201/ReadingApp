package com.example.readingapp.network

import android.content.Context
import com.example.readingapp.common.ErrorType
import com.example.readingapp.utils.hasInternet
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.net.SocketTimeoutException
import javax.inject.Inject

class NetworkInterceptor @Inject constructor(
    @ApplicationContext val context: Context
) : Interceptor {
    @Throws(Exception::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        if (context.hasInternet().not()) {
            throw ErrorType.NoInternetException
        }
        try {
            val builder: Request.Builder = chain.request().newBuilder()
            val response = chain.proceed(builder.build())

            val message = "code= ${response.code}, url= ${response.request.url}"

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
}