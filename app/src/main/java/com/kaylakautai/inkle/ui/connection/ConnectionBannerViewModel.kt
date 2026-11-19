package com.kaylakautai.inkle.ui.connection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaylakautai.inkle.network.ConnectivityObserver
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

enum class ConnectionBanner {
    HIDDEN,

    /** No connection: the app is showing cached data and saving changes on the device. */
    OFFLINE,

    /** Briefly, after coming back from [OFFLINE]. */
    BACK_ONLINE
}

@HiltViewModel
class ConnectionBannerViewModel @Inject constructor(
    connectivity: ConnectivityObserver
) : ViewModel() {

    private var wasOffline = false

    @OptIn(ExperimentalCoroutinesApi::class)
    val banner: StateFlow<ConnectionBanner> = connectivity.isOnline
        .transformLatest { online ->
            when {
                !online -> {
                    wasOffline = true
                    emit(ConnectionBanner.OFFLINE)
                }
                wasOffline -> {
                    wasOffline = false
                    emit(ConnectionBanner.BACK_ONLINE)
                    delay(BACK_ONLINE_SHOWN_FOR)
                    emit(ConnectionBanner.HIDDEN)
                }
                else -> emit(ConnectionBanner.HIDDEN)
            }
        }
        // Lazily: keep tracking while the app is in the background, so "Back online" still
        // makes sense on return, and a screen change doesn't restart it.
        .stateIn(viewModelScope, SharingStarted.Lazily, ConnectionBanner.HIDDEN)

    companion object {
        val BACK_ONLINE_SHOWN_FOR = 2.seconds
    }
}