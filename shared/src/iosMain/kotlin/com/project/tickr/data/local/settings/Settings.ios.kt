package com.project.tickr.data.local.settings

import com.project.tickr.domain.model.AppInfo
import com.project.tickr.domain.provider.AppInfoProvider
import platform.Foundation.NSBundle
import platform.Foundation.NSUserDefaults

internal actual fun createKeyValueStore(): KeyValueStore = object : KeyValueStore {
    private val defaults = NSUserDefaults.standardUserDefaults

    override fun getString(key: String): String? = defaults.stringForKey(key)

    override fun putString(key: String, value: String) {
        defaults.setObject(value, forKey = key)
    }
}

internal actual fun createAppInfoProvider(): AppInfoProvider = object : AppInfoProvider {
    override fun getAppInfo(): AppInfo {
        val dict = NSBundle.mainBundle.infoDictionary
        return AppInfo(
            name = APP_NAME,
            versionName = dict?.get("CFBundleShortVersionString") as? String ?: "-",
            buildNumber = dict?.get("CFBundleVersion") as? String ?: "-",
        )
    }
}
