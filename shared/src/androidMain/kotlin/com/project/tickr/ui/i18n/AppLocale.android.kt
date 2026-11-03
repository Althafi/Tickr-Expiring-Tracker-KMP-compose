package com.project.tickr.ui.i18n

import android.content.res.Configuration
import android.content.res.Resources
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import com.project.tickr.domain.model.AppLanguage
import java.util.Locale

// Resources.getSystem() tidak terpengaruh Locale.setDefault, jadi selalu bahasa perangkat yang asli.
internal actual fun deviceLanguageCode(): String =
    Resources.getSystem().configuration.locales.get(0)?.language ?: "en"

internal actual fun applyPlatformLocale(tag: String) {
    Locale.setDefault(Locale.forLanguageTag(tag))
}

@Composable
actual fun AppLocaleProvider(language: AppLanguage, content: @Composable () -> Unit) {
    val configuration = LocalConfiguration.current
    val tag = remember(language) { resolveLanguageTag(language, deviceLanguageCode()) }
    val locale = remember(tag) { Locale.forLanguageTag(tag).also { applyPlatformLocale(tag) } }
    val localizedConfiguration = remember(configuration, locale) {
        Configuration(configuration).apply { setLocale(locale) }
    }
    CompositionLocalProvider(LocalConfiguration provides localizedConfiguration) {
        key(tag) { content() }
    }
}
