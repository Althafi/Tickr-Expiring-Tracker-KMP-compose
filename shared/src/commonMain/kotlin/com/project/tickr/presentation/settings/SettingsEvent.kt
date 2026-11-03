package com.project.tickr.presentation.settings

import com.project.tickr.presentation.common.mvi.UiEvent
import com.project.tickr.presentation.navigation.Destination

sealed interface SettingsEvent : UiEvent {
    data class Navigate(val destination: Destination) : SettingsEvent
}
