package com.project.tickr.core.platform

import android.content.Context
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.network.ktor3.KtorNetworkFetcherFactory
import com.project.tickr.data.repository.AndroidContextHolder

object AndroidPlatformInitializer {
    fun initialize(context: Context) {
        AndroidContextHolder.initialize(context)
        SingletonImageLoader.setSafe { ctx ->
            ImageLoader.Builder(ctx)
                .components { add(KtorNetworkFetcherFactory()) }
                .build()
        }
    }
}

actual fun initializePlatform() {
    // Platform initialization happens via AndroidPlatformInitializer.initialize(context)
    // Called from the Android app
}
