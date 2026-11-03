package com.project.tickr.domain.model

enum class ThemeMode { SYSTEM, LIGHT, DARK }

/** [tag] = kode bahasa BCP-47; `null` berarti mengikuti bahasa perangkat. */
enum class AppLanguage(val tag: String?) {
    SYSTEM(null),
    INDONESIAN("id"),
    ENGLISH("en"),
}

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val language: AppLanguage = AppLanguage.SYSTEM,
)

data class AppInfo(
    val name: String,
    val versionName: String,
    val buildNumber: String,
)
