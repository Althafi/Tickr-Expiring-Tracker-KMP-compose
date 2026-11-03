package com.project.tickr.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.tickr.domain.usecase.settings.GetAppInfoUseCase
import com.project.tickr.domain.usecase.settings.ObserveSettingsUseCase
import com.project.tickr.domain.usecase.settings.SetLanguageUseCase
import com.project.tickr.domain.usecase.settings.SetThemeModeUseCase
import com.project.tickr.presentation.navigation.Destination
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.project.tickr.core.result.onError
import com.project.tickr.core.result.onSuccess
import com.project.tickr.domain.usecase.backup.ExportBackupUseCase
import com.project.tickr.domain.usecase.backup.ImportBackupUseCase
import com.project.tickr.domain.usecase.reminder.SyncRemindersUseCase
import com.project.tickr.presentation.common.AppMessenger
import com.project.tickr.presentation.common.ErrorMessageProvider
import com.project.tickr.presentation.common.UiText
import com.project.tickr.presentation.common.asUiText
import tickrapp.shared.generated.resources.*

class SettingsViewModel(
    observeSettings: ObserveSettingsUseCase,
    private val setThemeMode: SetThemeModeUseCase,
    private val setLanguage: SetLanguageUseCase,
    getAppInfo: GetAppInfoUseCase,
    private val syncReminders: SyncRemindersUseCase,
    private val exportBackup: ExportBackupUseCase,
    private val importBackup: ImportBackupUseCase,
    private val errorMessages: ErrorMessageProvider,
    private val messenger: AppMessenger,
) : ViewModel() {

    private val _state = MutableStateFlow(
        getAppInfo().let { info ->
            SettingsUiState(
                appName = info.name,
                appVersion = info.versionName,
            )
        }
    )
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    private val _events = Channel<SettingsEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        observeSettings()
            .onEach { s -> _state.update { it.copy(themeMode = s.themeMode, language = s.language) } }
            .launchIn(viewModelScope)
    }

    fun onAction(action: SettingsAction) {
        when (action) {
            is SettingsAction.SelectTheme -> viewModelScope.launch { setThemeMode(action.mode) }
            is SettingsAction.SelectLanguage -> viewModelScope.launch {
                setLanguage(action.language)
                // Teks notifikasi yang sudah terjadwal (iOS) harus dibuat ulang dalam bahasa baru.
                syncReminders.requestSync()
            }
            SettingsAction.OpenHelp -> viewModelScope.launch {
                _events.send(SettingsEvent.Navigate(Destination.Help))
            }
            SettingsAction.OpenAbout -> viewModelScope.launch {
                _events.send(SettingsEvent.Navigate(Destination.About))
            }
            SettingsAction.ExportData -> export()
            SettingsAction.ImportData -> if (beginBackup()) viewModelScope.launch {
                _events.send(SettingsEvent.PickBackupFile)
            }
            is SettingsAction.BackupFileRead -> import(action.content)
            SettingsAction.BackupSaved -> {
                endBackup()
                messenger.success(Res.string.backup_exported.asUiText())
            }
            SettingsAction.BackupFileCancelled -> endBackup()
            is SettingsAction.BackupFileFailed -> {
                endBackup()
                messenger.error(
                    (if (action.forExport) Res.string.error_backup_write else Res.string.error_backup_read).asUiText()
                )
            }
        }
    }

    /** `false` bila ekspor/impor lain masih berjalan. */
    private fun beginBackup(): Boolean {
        var started = false
        _state.update { current ->
            if (current.isBackupBusy) current else current.copy(isBackupBusy = true).also { started = true }
        }
        return started
    }

    private fun endBackup() = _state.update { it.copy(isBackupBusy = false) }

    private fun export() {
        if (!beginBackup()) return
        viewModelScope.launch {
            exportBackup()
                .onSuccess { _events.send(SettingsEvent.SaveBackupFile(it.fileName, it.content)) }
                .onError { err ->
                    endBackup()
                    messenger.error(errorMessages.provide(err))
                }
        }
    }

    private fun import(content: String) {
        viewModelScope.launch {
            importBackup(content)
                .onSuccess { summary ->
                    // Barang berubah total: jadwal pengingat harus dibangun ulang dari data baru.
                    syncReminders.requestSync()
                    messenger.success(UiText.Plural(Res.plurals.backup_imported, summary.itemCount, listOf(summary.itemCount)))
                }
                .onError { err -> messenger.error(errorMessages.provide(err)) }
            endBackup()
        }
    }
}
