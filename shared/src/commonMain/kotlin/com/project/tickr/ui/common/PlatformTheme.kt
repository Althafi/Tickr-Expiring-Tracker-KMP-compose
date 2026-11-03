package com.project.tickr.ui.common

import androidx.compose.runtime.Composable
import com.project.tickr.domain.model.ThemeMode

/**
 * Menyelaraskan elemen native dengan tema pilihan pengguna:
 * - Android: ikon status bar dan navigation bar (terbaca di atas latar terang/gelap).
 * - iOS: `overrideUserInterfaceStyle` window (keyboard, alert, picker native).
 */
@Composable
expect fun ApplyPlatformTheme(themeMode: ThemeMode, darkTheme: Boolean)
