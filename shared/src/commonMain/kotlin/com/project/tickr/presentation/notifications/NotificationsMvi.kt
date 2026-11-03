package com.project.tickr.presentation.notifications

import com.project.tickr.domain.model.NotificationEntry
import com.project.tickr.presentation.common.mvi.UiAction
import com.project.tickr.presentation.common.mvi.UiEvent
import com.project.tickr.presentation.navigation.Destination

data class NotificationsUiState(
    val isLoading: Boolean = true,
    val entries: List<NotificationEntry> = emptyList(),
    /** False → tampilkan petunjuk untuk mengaktifkan notifikasi di pengaturan perangkat. */
    val permissionGranted: Boolean = true,
)

sealed interface NotificationsAction : UiAction {
    /** Dipanggil saat layar dibuka dan saat kembali dari pengaturan sistem. */
    data object Refresh : NotificationsAction
    data class OpenItem(val itemId: Long) : NotificationsAction
    data object Back : NotificationsAction
}

sealed interface NotificationsEvent : UiEvent {
    data class Navigate(val destination: Destination) : NotificationsEvent
    data object NavigateBack : NotificationsEvent
}
