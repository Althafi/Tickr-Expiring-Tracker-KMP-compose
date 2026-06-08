package com.project.tickr.presentation.profile

import com.project.tickr.presentation.common.mvi.UiEvent

sealed interface ProfileEvent : UiEvent {
    data object Saved : ProfileEvent
    data object NavigateToAuth : ProfileEvent
    data class ShowSnackbar(val message: String) : ProfileEvent
}
