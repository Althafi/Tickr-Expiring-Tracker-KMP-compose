package com.project.tickr.presentation.auth

import com.project.tickr.presentation.common.mvi.UiEvent

sealed interface AuthEvent : UiEvent {
    data object NavigateToHome : AuthEvent
    data object NavigateToAuth : AuthEvent
    data class ShowError(val message: String) : AuthEvent
}
