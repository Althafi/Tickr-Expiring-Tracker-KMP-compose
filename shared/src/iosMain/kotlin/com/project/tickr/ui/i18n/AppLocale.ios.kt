package com.project.tickr.ui.i18n

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import com.project.tickr.domain.model.AppLanguage
import platform.Foundation.NSLocale
import platform.Foundation.NSUserDefaults
import platform.Foundation.preferredLanguages

private const val APPLE_LANGUAGES = "AppleLanguages"

/**
 * Bahasa perangkat yang asli. Override yang tersisa dari peluncuran sebelumnya dihapus dulu,
 * karena `AppleLanguages` bertahan di NSUserDefaults dan akan menyamarkan bahasa sistem.
 */
private val deviceLanguage: String by lazy {
    NSUserDefaults.standardUserDefaults.removeObjectForKey(APPLE_LANGUAGES)
    (NSLocale.preferredLanguages.firstOrNull() as? String) ?: "en"
}

internal actual fun deviceLanguageCode(): String = deviceLanguage

internal actual fun applyPlatformLocale(tag: String) {
    NSUserDefaults.standardUserDefaults.setObject(listOf(tag), forKey = APPLE_LANGUAGES)
}

@Composable
actual fun AppLocaleProvider(language: AppLanguage, content: @Composable () -> Unit) {
    val tag = remember(language) { resolveLanguageTag(language, deviceLanguageCode()).also { applyPlatformLocale(it) } }
    key(tag) { content() }
}
