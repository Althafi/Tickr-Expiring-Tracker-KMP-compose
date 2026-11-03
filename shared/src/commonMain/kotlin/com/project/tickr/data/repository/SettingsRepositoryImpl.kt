package com.project.tickr.data.repository

import com.project.tickr.data.local.settings.KeyValueStore
import com.project.tickr.domain.model.AppLanguage
import com.project.tickr.domain.model.AppSettings
import com.project.tickr.domain.model.ThemeMode
import com.project.tickr.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SettingsRepositoryImpl(private val store: KeyValueStore) : SettingsRepository {

    private val _settings = MutableStateFlow(load())
    override val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    override suspend fun setThemeMode(mode: ThemeMode) {
        store.putString(KEY_THEME, mode.name)
        _settings.update { it.copy(themeMode = mode) }
    }

    override suspend fun setLanguage(language: AppLanguage) {
        store.putString(KEY_LANGUAGE, language.name)
        _settings.update { it.copy(language = language) }
    }

    // Nilai tak dikenal (mis. dari versi app lain) jatuh ke default, bukan crash.
    private fun load() = AppSettings(
        themeMode = store.getString(KEY_THEME)
            ?.let { name -> ThemeMode.entries.firstOrNull { it.name == name } } ?: ThemeMode.SYSTEM,
        language = store.getString(KEY_LANGUAGE)
            ?.let { name -> AppLanguage.entries.firstOrNull { it.name == name } } ?: AppLanguage.SYSTEM,
    )

    private companion object {
        const val KEY_THEME = "theme_mode"
        const val KEY_LANGUAGE = "language"
    }
}
