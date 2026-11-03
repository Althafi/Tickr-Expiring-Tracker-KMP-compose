package com.project.tickr.data.local.settings

import android.content.Context
import android.os.Build
import com.project.tickr.data.repository.AndroidContextHolder
import com.project.tickr.domain.model.AppInfo
import com.project.tickr.domain.provider.AppInfoProvider

internal actual fun createKeyValueStore(): KeyValueStore = object : KeyValueStore {
    private val prefs by lazy {
        AndroidContextHolder.getContext().getSharedPreferences("tickr_settings", Context.MODE_PRIVATE)
    }

    override fun getString(key: String): String? = prefs.getString(key, null)

    override fun putString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }
}

internal actual fun createAppInfoProvider(): AppInfoProvider = object : AppInfoProvider {
    @Suppress("DEPRECATION")
    override fun getAppInfo(): AppInfo {
        val context = AndroidContextHolder.getContext()
        val info = try {
            context.packageManager.getPackageInfo(context.packageName, 0)
        } catch (_: Exception) {
            null
        }
        val build = when {
            info == null -> "-"
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.P -> info.longVersionCode.toString()
            else -> info.versionCode.toString()
        }
        return AppInfo(name = APP_NAME, versionName = info?.versionName ?: "-", buildNumber = build)
    }
}
