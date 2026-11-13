package com.example.readingapp.utils

import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import java.security.MessageDigest

fun Context.hasInternet(): Boolean {
    val connectivityManager = this.getSystemService(ConnectivityManager::class.java)
    val netInfo = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
    val hasInternet = netInfo?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    return hasInternet ?: false
}

fun Context.signingCertSha1(): String {
    val signature = signingSignature() ?: return ""
    val digest = MessageDigest.getInstance("SHA-1").digest(signature.toByteArray())
    return digest.joinToString("") { "%02x".format(it) }
}

private fun Context.signingSignature(): Signature? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val info = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
        info.signingInfo?.apkContentsSigners?.firstOrNull()
    } else {
        @Suppress("DEPRECATION")
        val info = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
        @Suppress("DEPRECATION")
        info.signatures?.firstOrNull()
    }
}