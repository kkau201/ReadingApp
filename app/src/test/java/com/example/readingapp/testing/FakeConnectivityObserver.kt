package com.example.readingapp.testing

import com.example.readingapp.network.ConnectivityObserver
import kotlinx.coroutines.flow.MutableStateFlow

/** Set [online] to simulate the connection dropping or returning. */
class FakeConnectivityObserver(initiallyOnline: Boolean = true) : ConnectivityObserver {
    val online = MutableStateFlow(initiallyOnline)
    override val isOnline = online
}
