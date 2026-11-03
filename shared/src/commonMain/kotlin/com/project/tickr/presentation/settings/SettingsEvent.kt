package com.project.tickr.presentation.settings

import com.project.tickr.presentation.common.mvi.UiEvent
import com.project.tickr.presentation.navigation.Destination

sealed interface SettingsEvent : UiEvent {
    data class Navigate(val destination: Destination) : SettingsEvent
    /** Minta platform menyimpan [content] ke file bernama [fileName]. */
    data class SaveBackupFile(val fileName: String, val content: String) : SettingsEvent
    data object PickBackupFile : SettingsEvent
}
