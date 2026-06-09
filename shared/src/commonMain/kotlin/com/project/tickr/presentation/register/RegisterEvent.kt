package com.project.tickr.presentation.register

import com.project.tickr.presentation.common.UiText
import com.project.tickr.presentation.common.mvi.UiEvent

sealed interface RegisterEvent : UiEvent {
    data object NavigateToSuccess : RegisterEvent
    data object NavigateToLogin : RegisterEvent
    data class ShowError(val message: UiText) : RegisterEvent
}
