package com.project.tickr.presentation.login

import com.project.tickr.presentation.common.UiText
import com.project.tickr.presentation.common.mvi.UiEvent

sealed interface LoginEvent : UiEvent {
    data object NavigateToHome : LoginEvent
    data object NavigateToRegister : LoginEvent
    data class ShowError(val message: UiText) : LoginEvent
}
