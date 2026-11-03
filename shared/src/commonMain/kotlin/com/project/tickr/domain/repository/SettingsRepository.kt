package com.project.tickr.domain.repository

import com.project.tickr.domain.model.AppLanguage
import com.project.tickr.domain.model.AppSettings
import com.project.tickr.domain.model.ThemeMode
import kotlinx.coroutines.flow.StateFlow

interface SettingsRepository {
    /** Selalu punya nilai (dibaca sinkron saat dibuat) sehingga UI tidak berkedip dari tema/bahasa yang salah. */
    val settings: StateFlow<AppSettings>

    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setLanguage(language: AppLanguage)
}
