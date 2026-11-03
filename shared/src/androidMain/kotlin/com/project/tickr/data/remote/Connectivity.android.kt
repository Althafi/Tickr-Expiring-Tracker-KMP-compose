package com.project.tickr.data.remote

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.project.tickr.data.repository.AndroidContextHolder
import com.project.tickr.domain.provider.ConnectivityProvider

actual fun createConnectivityProvider(): ConnectivityProvider = AndroidConnectivityProvider()

internal class AndroidConnectivityProvider : ConnectivityProvider {
    override fun isOnline(): Boolean = try {
        val manager = AndroidContextHolder.getContext()
            .getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val capabilities = manager?.getNetworkCapabilities(manager.activeNetwork)
        // VALIDATED = Android sudah memastikan jalur ini benar-benar sampai ke internet (bukan sekadar terhubung ke Wi-Fi).
        capabilities != null &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    } catch (e: SecurityException) {
        // Izin ACCESS_NETWORK_STATE hilang: jangan memblokir fitur, biarkan permintaan yang menentukan.
        true
    }
}
