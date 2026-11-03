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
import com.project.tickr.domain.usecase.reminder.SyncRemindersUseCase

class SettingsViewModel(
    observeSettings: ObserveSettingsUseCase,
    private val setThemeMode: SetThemeModeUseCase,
    private val setLanguage: SetLanguageUseCase,
    getAppInfo: GetAppInfoUseCase,
    private val syncReminders: SyncRemindersUseCase,
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
        }
    }
}
