package com.example.readingapp.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

fun Context.hasInternet(): Boolean {
    val connectivityManager = this.getSystemService(ConnectivityManager::class.java)
    val netInfo = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
    val hasInternet = netInfo?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    return hasInternet ?: false
}