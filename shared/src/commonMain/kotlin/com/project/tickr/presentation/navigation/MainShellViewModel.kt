package com.project.tickr.presentation.navigation

import androidx.lifecycle.ViewModel
import com.project.tickr.domain.usecase.reminder.MarkNotificationPermissionAskedUseCase
import com.project.tickr.domain.usecase.reminder.ShouldRequestNotificationPermissionUseCase
import com.project.tickr.domain.usecase.reminder.SyncRemindersUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Menyimpan tab terpilih di luar composable, supaya tidak kembali ke Beranda
 * ketika UI dibuat ulang (mis. setelah bahasa diganti). Juga mengatur kapan izin notifikasi diminta.
 */
class MainShellViewModel(
    private val shouldRequestNotificationPermission: ShouldRequestNotificationPermissionUseCase,
    private val markPermissionAsked: MarkNotificationPermissionAskedUseCase,
    private val syncReminders: SyncRemindersUseCase,
) : ViewModel() {
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    fun select(index: Int) {
        _selectedTab.value = index
    }

    /**
     * Dipanggil setelah barang pertama berhasil disimpan (momen yang punya konteks). True hanya sekali:
     * bila izin belum diberikan dan belum pernah ditanyakan.
     */
    suspend fun consumeNotificationPermissionPrompt(): Boolean {
        if (!shouldRequestNotificationPermission()) return false
        markPermissionAsked()
        return true
    }

    fun onNotificationPermissionResult() = syncReminders.requestSync()
}
