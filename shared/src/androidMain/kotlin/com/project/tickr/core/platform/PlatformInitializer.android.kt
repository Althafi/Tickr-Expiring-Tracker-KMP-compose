package com.project.tickr.core.platform

import android.content.Context
import com.project.tickr.data.repository.AndroidContextHolder

object AndroidPlatformInitializer {
    fun initialize(context: Context) {
        AndroidContextHolder.initialize(context)
    }
}

actual fun initializePlatform() {
    // Platform initialization happens via AndroidPlatformInitializer.initialize(context)
    // Called from the Android app
}
