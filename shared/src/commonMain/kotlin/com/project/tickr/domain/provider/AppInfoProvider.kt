package com.project.tickr.domain.provider

import com.project.tickr.domain.model.AppInfo

/** Info aplikasi dari platform (bukan string tulis-tangan). */
interface AppInfoProvider {
    fun getAppInfo(): AppInfo
}
