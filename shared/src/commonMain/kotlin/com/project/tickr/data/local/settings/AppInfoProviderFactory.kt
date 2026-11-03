package com.project.tickr.data.local.settings

import com.project.tickr.domain.provider.AppInfoProvider

internal expect fun createAppInfoProvider(): AppInfoProvider

internal const val APP_NAME = "Tickr"
