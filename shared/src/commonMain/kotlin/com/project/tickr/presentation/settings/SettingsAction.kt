package com.project.tickr.presentation.settings

import com.project.tickr.domain.model.AppLanguage
import com.project.tickr.domain.model.ThemeMode
import com.project.tickr.presentation.common.mvi.UiAction

sealed interface SettingsAction : UiAction {
    data class SelectTheme(val mode: ThemeMode) : SettingsAction
    data class SelectLanguage(val language: AppLanguage) : SettingsAction
    data object OpenHelp : SettingsAction
    data object OpenAbout : SettingsAction
    data object ExportData : SettingsAction
    /** Pengguna sudah mengonfirmasi bahwa impor akan mengganti data; lanjutkan memilih file. */
    data object ImportData : SettingsAction
    /** Hasil membaca file cadangan dari pemilih file. */
    data class BackupFileRead(val content: String) : SettingsAction
    /** File cadangan tersimpan di lokasi pilihan pengguna. */
    data object BackupSaved : SettingsAction
    /** Pengguna menutup pemilih file tanpa memilih. */
    data object BackupFileCancelled : SettingsAction
    /** Menulis atau membaca file gagal. [forExport] = true bila gagalnya saat menyimpan. */
    data class BackupFileFailed(val forExport: Boolean) : SettingsAction
}
