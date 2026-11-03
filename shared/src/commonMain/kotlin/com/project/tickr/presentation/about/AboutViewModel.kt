package com.project.tickr.presentation.about

import androidx.lifecycle.ViewModel
import com.project.tickr.domain.model.AppInfo
import com.project.tickr.domain.usecase.settings.GetAppInfoUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AboutUiState(val info: AppInfo)

class AboutViewModel(getAppInfo: GetAppInfoUseCase) : ViewModel() {
    private val _state = MutableStateFlow(AboutUiState(getAppInfo()))
    val state: StateFlow<AboutUiState> = _state.asStateFlow()
}
