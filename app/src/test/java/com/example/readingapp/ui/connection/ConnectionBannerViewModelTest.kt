package com.example.readingapp.ui.connection

import com.example.readingapp.testing.BaseViewModelTest
import com.example.readingapp.testing.FakeConnectivityObserver
import com.example.readingapp.ui.connection.ConnectionBannerViewModel.Companion.BACK_ONLINE_SHOWN_FOR
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class ConnectionBannerViewModelTest : BaseViewModelTest() {

    private val connectivity = FakeConnectivityObserver()

    private fun TestScope.createViewModel(): ConnectionBannerViewModel =
        ConnectionBannerViewModel(connectivity).also { viewModel ->
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.banner.collect {} }
        }

    @Test
    fun online_showsNothing() = runTest {
        val viewModel = createViewModel()

        assertEquals(ConnectionBanner.HIDDEN, viewModel.banner.value)
    }

    @Test
    fun startingOffline_showsOfflineStraightAway() = runTest {
        connectivity.online.value = false

        val viewModel = createViewModel()

        assertEquals(ConnectionBanner.OFFLINE, viewModel.banner.value)
    }

    @Test
    fun goingOffline_showsOffline() = runTest {
        val viewModel = createViewModel()

        connectivity.online.value = false

        assertEquals(ConnectionBanner.OFFLINE, viewModel.banner.value)
    }

    @Test
    fun comingBackOnline_saysSoBrieflyThenHides() = runTest {
        val viewModel = createViewModel()
        connectivity.online.value = false

        connectivity.online.value = true
        assertEquals(ConnectionBanner.BACK_ONLINE, viewModel.banner.value)

        advanceTimeBy(BACK_ONLINE_SHOWN_FOR - 1.milliseconds)
        assertEquals(ConnectionBanner.BACK_ONLINE, viewModel.banner.value)

        advanceTimeBy(2.milliseconds)
        assertEquals(ConnectionBanner.HIDDEN, viewModel.banner.value)
    }

    @Test
    fun droppingAgainWhileSayingBackOnline_showsOfflineAndStaysThere() = runTest {
        val viewModel = createViewModel()
        connectivity.online.value = false
        connectivity.online.value = true

        connectivity.online.value = false
        advanceTimeBy(BACK_ONLINE_SHOWN_FOR * 2)

        assertEquals(ConnectionBanner.OFFLINE, viewModel.banner.value)
    }

    @Test
    fun secondReconnect_saysBackOnlineAgain() = runTest {
        val viewModel = createViewModel()
        connectivity.online.value = false
        connectivity.online.value = true
        advanceTimeBy(BACK_ONLINE_SHOWN_FOR * 2)

        connectivity.online.value = false
        connectivity.online.value = true

        assertEquals(ConnectionBanner.BACK_ONLINE, viewModel.banner.value)
    }
}
