package com.project.tickr.data.remote

import com.project.tickr.domain.provider.ConnectivityProvider
import platform.Network.nw_path_get_status
import platform.Network.nw_path_monitor_create
import platform.Network.nw_path_monitor_set_queue
import platform.Network.nw_path_monitor_set_update_handler
import platform.Network.nw_path_monitor_start
import platform.Network.nw_path_status_satisfied
import platform.darwin.dispatch_get_main_queue
import kotlin.concurrent.Volatile

actual fun createConnectivityProvider(): ConnectivityProvider = IosConnectivityProvider()

internal class IosConnectivityProvider : ConnectivityProvider {
    @Volatile
    private var online = true // belum tahu = anggap online; permintaan yang gagal tetap ditangani

    init {
        val monitor = nw_path_monitor_create()
        nw_path_monitor_set_update_handler(monitor) { path ->
            online = path != null && nw_path_get_status(path) == nw_path_status_satisfied
        }
        nw_path_monitor_set_queue(monitor, dispatch_get_main_queue())
        nw_path_monitor_start(monitor)
    }

    override fun isOnline(): Boolean = online
}
