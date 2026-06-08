package com.project.tickr.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.tickr.core.result.onError
import com.project.tickr.core.result.onSuccess
import com.project.tickr.domain.error.ErrorMessageProvider
import com.project.tickr.domain.usecase.auth.GetCurrentUserIdUseCase
import com.project.tickr.domain.usecase.item.GetExpiringItemsUseCase
import com.project.tickr.presentation.navigation.Destination
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val getExpiringItems: GetExpiringItemsUseCase,
    private val getCurrentUserId: GetCurrentUserIdUseCase,
    private val errorMessages: ErrorMessageProvider
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    private val _events = Channel<HomeEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onAction(action: HomeAction) {
        when (action) {
            HomeAction.Load, HomeAction.Refresh -> load()
            is HomeAction.ClickItem -> viewModelScope.launch {
                _events.send(HomeEvent.NavigateToDetail(Destination.ItemDetail(action.id)))
            }
        }
    }

    private fun load() {
        val userId = getCurrentUserId() ?: run {
            _state.update { it.copy(isLoading = false, error = "Session expired") }
            return
        }
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            getExpiringItems(userId)
                .onSuccess { items ->
                    _state.update {
                        it.copy(isLoading = false, expiringItems = items, expiringCount = items.size)
                    }
                }
                .onError { error ->
                    _state.update { it.copy(isLoading = false, error = errorMessages.provide(error)) }
                }
        }
    }
}
