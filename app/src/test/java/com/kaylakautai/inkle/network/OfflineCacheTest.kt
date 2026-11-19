package com.kaylakautai.inkle.network

import android.content.Context
import com.kaylakautai.inkle.common.ErrorType
import com.kaylakautai.inkle.utils.hasInternet
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

/** The Google Books client's cache, set up as in AppModule, against a local server. */
class OfflineCacheTest {

    @get:Rule
    val folder = TemporaryFolder()

    private val server = MockWebServer()
    private val context = mockk<Context>()
    private var online = true
    private lateinit var client: OkHttpClient

    @Before
    fun setUp() {
        mockkStatic("com.kaylakautai.inkle.utils.ContextExtensionsKt")
        every { context.hasInternet() } answers { online }
        server.start()
        client = OkHttpClient.Builder()
            .cache(Cache(folder.newFolder("cache"), 1024L * 1024))
            .addInterceptor(NetworkInterceptor(context))
            .addNetworkInterceptor(CacheForOfflineInterceptor())
            .build()
    }

    @After
    fun tearDown() {
        server.close()
        unmockkStatic("com.kaylakautai.inkle.utils.ContextExtensionsKt")
    }

    private fun get(path: String = "/volumes/abc"): String =
        client.newCall(Request.Builder().url(server.url(path)).build()).execute()
            .use { it.body.string() }

    private fun respond(body: String, cacheControl: String = "private, max-age=0, must-revalidate") {
        server.enqueue(MockResponse.Builder().body(body).addHeader("Cache-Control", cacheControl).build())
    }

    @Test
    fun offline_bookLoadedBefore_isServedFromCache() {
        respond("book")
        get()

        online = false

        assertEquals("book", get())
        assertEquals(1, server.requestCount)
    }

    @Test
    fun offline_serverForbadeStoring_isStillServedFromCache() {
        respond("book", cacheControl = "no-store, no-cache")
        get()

        online = false

        assertEquals("book", get())
    }

    @Test
    fun offline_bookNeverLoaded_failsAsNoInternet() {
        respond("book")
        get("/volumes/abc")

        online = false

        assertThrows(ErrorType.NoInternetException::class.java) { get("/volumes/other") }
    }

    @Test
    fun online_alwaysAsksTheServer() {
        respond("old")
        respond("new")
        get()

        assertEquals("new", get())
        assertEquals(2, server.requestCount)
    }

    @Test
    fun failedResponse_isNotServedOffline() {
        server.enqueue(MockResponse.Builder().code(503).build())
        assertThrows(ErrorType.ServiceException::class.java) { get() }

        online = false

        assertThrows(ErrorType.NoInternetException::class.java) { get() }
    }
}
