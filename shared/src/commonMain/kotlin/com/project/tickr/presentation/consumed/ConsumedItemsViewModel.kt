package com.project.tickr.presentation.consumed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.tickr.core.result.onError
import com.project.tickr.core.result.onSuccess
import com.project.tickr.domain.usecase.auth.GetCurrentUserIdUseCase
import com.project.tickr.domain.usecase.consumed.GetConsumedItemsUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ConsumedItemsViewModel(
    private val getConsumedItems: GetConsumedItemsUseCase,
    private val getCurrentUserId: GetCurrentUserIdUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(ConsumedItemsUiState())
    val state: StateFlow<ConsumedItemsUiState> = _state.asStateFlow()

    private val _events = Channel<ConsumedItemsEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        load()
    }

    fun onAction(action: ConsumedItemsAction) {
        when (action) {
            ConsumedItemsAction.Load -> load()
            ConsumedItemsAction.Refresh -> refresh()
            ConsumedItemsAction.Back -> viewModelScope.launch {
                _events.send(ConsumedItemsEvent.NavigateBack)
            }
        }
    }

    private fun load() {
        val userId = getCurrentUserId() ?: run {
            _state.update { it.copy(loadState = ConsumedItemsLoadState.Error("Sesi habis, silakan masuk kembali")) }
            return
        }
        _state.update { it.copy(loadState = ConsumedItemsLoadState.Loading) }
        viewModelScope.launch {
            getConsumedItems(userId)
                .onSuccess { items ->
                    _state.update { it.copy(loadState = ConsumedItemsLoadState.Content, items = items) }
                }
                .onError { err ->
                    _state.update { it.copy(loadState = ConsumedItemsLoadState.Error(err.message)) }
                }
        }
    }

    private fun refresh() {
        val userId = getCurrentUserId() ?: return
        _state.update { it.copy(isRefreshing = true) }
        viewModelScope.launch {
            getConsumedItems(userId)
                .onSuccess { items ->
                    _state.update {
                        it.copy(loadState = ConsumedItemsLoadState.Content, items = items, isRefreshing = false)
                    }
                }
                .onError {
                    _state.update { it.copy(isRefreshing = false) }
                }
        }
    }
}
