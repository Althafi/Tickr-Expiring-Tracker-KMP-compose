package com.project.tickr

import android.app.Application
import com.project.tickr.core.network.ChuckerContext
import com.project.tickr.core.platform.AndroidPlatformInitializer
import com.project.tickr.di.initKoin

class TickrApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ChuckerContext.applicationContext = applicationContext
        AndroidPlatformInitializer.initialize(this)
        initKoin()
    }
}
