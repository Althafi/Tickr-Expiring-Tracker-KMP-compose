package com.project.tickr.presentation.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.tickr.core.result.getOrNull
import com.project.tickr.domain.usecase.reminder.GetNotificationHistoryUseCase
import com.project.tickr.domain.usecase.reminder.HasNotificationPermissionUseCase
import com.project.tickr.presentation.navigation.Destination
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NotificationsViewModel(
    private val getHistory: GetNotificationHistoryUseCase,
    private val hasPermission: HasNotificationPermissionUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(NotificationsUiState())
    val state: StateFlow<NotificationsUiState> = _state.asStateFlow()

    private val _events = Channel<NotificationsEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        refresh()
    }

    fun onAction(action: NotificationsAction) {
        when (action) {
            NotificationsAction.Refresh -> refresh()
            is NotificationsAction.OpenItem -> viewModelScope.launch {
                _events.send(NotificationsEvent.Navigate(Destination.ItemDetail(action.itemId)))
            }
            NotificationsAction.Back -> viewModelScope.launch { _events.send(NotificationsEvent.NavigateBack) }
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            val granted = hasPermission()
            val entries = getHistory().getOrNull().orEmpty()
            _state.update { it.copy(isLoading = false, entries = entries, permissionGranted = granted) }
        }
    }
}
