package com.project.tickr.presentation.settings

import com.project.tickr.domain.model.AppLanguage
import com.project.tickr.domain.model.ThemeMode
import com.project.tickr.presentation.common.mvi.UiAction

sealed interface SettingsAction : UiAction {
    data class SelectTheme(val mode: ThemeMode) : SettingsAction
    data class SelectLanguage(val language: AppLanguage) : SettingsAction
    data object OpenHelp : SettingsAction
    data object OpenAbout : SettingsAction
}
