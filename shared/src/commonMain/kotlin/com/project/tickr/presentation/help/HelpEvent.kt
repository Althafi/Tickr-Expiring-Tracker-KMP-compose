package com.project.tickr.presentation.help

import com.project.tickr.presentation.common.mvi.UiEvent

sealed interface HelpEvent : UiEvent {
    data class OpenExternal(val url: String) : HelpEvent
    data object NavigateBack : HelpEvent
}
