package com.project.tickr.ui.i18n

import androidx.compose.runtime.staticCompositionLocalOf
import com.project.tickr.domain.model.AppInfo

/** Info aplikasi untuk footer versi. Default aman untuk `@Preview` (tanpa Koin). */
val LocalAppInfo = staticCompositionLocalOf { AppInfo(name = "Tickr", versionName = "-", buildNumber = "-") }
