package com.project.tickr.domain.provider

/** Status koneksi internet perangkat. Dipakai untuk mencegah fitur online dipakai saat offline. */
interface ConnectivityProvider {
    /** True bila perangkat saat ini punya jalur internet. Bila platform tidak bisa memastikan, kembalikan true. */
    fun isOnline(): Boolean
}
