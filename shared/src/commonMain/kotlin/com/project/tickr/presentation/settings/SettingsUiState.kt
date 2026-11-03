package com.project.tickr.presentation.settings

import com.project.tickr.domain.model.AppLanguage
import com.project.tickr.domain.model.ThemeMode

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val language: AppLanguage = AppLanguage.SYSTEM,
    val appName: String = "",
    val appVersion: String = "",
    /** Ekspor/impor sedang berjalan; tombolnya dinonaktifkan agar tidak dobel. */
    val isBackupBusy: Boolean = false,
)
