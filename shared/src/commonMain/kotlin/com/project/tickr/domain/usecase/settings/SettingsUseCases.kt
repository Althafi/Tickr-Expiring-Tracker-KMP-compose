package com.project.tickr.domain.usecase.settings

import com.project.tickr.domain.model.AppInfo
import com.project.tickr.domain.model.AppLanguage
import com.project.tickr.domain.model.AppSettings
import com.project.tickr.domain.model.ThemeMode
import com.project.tickr.domain.provider.AppInfoProvider
import com.project.tickr.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.StateFlow

class ObserveSettingsUseCase(private val repository: SettingsRepository) {
    operator fun invoke(): StateFlow<AppSettings> = repository.settings
}

class SetThemeModeUseCase(private val repository: SettingsRepository) {
    suspend operator fun invoke(mode: ThemeMode) = repository.setThemeMode(mode)
}

class SetLanguageUseCase(private val repository: SettingsRepository) {
    suspend operator fun invoke(language: AppLanguage) = repository.setLanguage(language)
}

class GetAppInfoUseCase(private val provider: AppInfoProvider) {
    operator fun invoke(): AppInfo = provider.getAppInfo()
}
