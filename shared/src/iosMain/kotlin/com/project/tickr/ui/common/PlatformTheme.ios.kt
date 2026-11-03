package com.project.tickr.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import com.project.tickr.domain.model.ThemeMode
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIApplication
import platform.UIKit.UIUserInterfaceStyle

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun ApplyPlatformTheme(themeMode: ThemeMode, darkTheme: Boolean) {
    SideEffect {
        val style = when (themeMode) {
            // Unspecified = ikuti sistem, sehingga isSystemInDarkTheme() tetap akurat.
            ThemeMode.SYSTEM -> UIUserInterfaceStyle.UIUserInterfaceStyleUnspecified
            ThemeMode.LIGHT -> UIUserInterfaceStyle.UIUserInterfaceStyleLight
            ThemeMode.DARK -> UIUserInterfaceStyle.UIUserInterfaceStyleDark
        }
        UIApplication.sharedApplication.keyWindow?.overrideUserInterfaceStyle = style
    }
}
