package com.project.tickr.presentation.consumed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.tickr.core.result.onError
import com.project.tickr.core.result.onSuccess
import com.project.tickr.domain.usecase.auth.GetCurrentUserIdUseCase
import com.project.tickr.domain.usecase.consumed.GetConsumedItemsUseCase
import com.project.tickr.domain.usecase.item.ObserveItemsUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.project.tickr.presentation.common.ErrorMessageProvider

class ConsumedItemsViewModel(
    private val observeItems: ObserveItemsUseCase,
    private val getConsumedItems: GetConsumedItemsUseCase,
    private val getCurrentUserId: GetCurrentUserIdUseCase,
    private val errorMessages: ErrorMessageProvider,
) : ViewModel() {

    private val _state = MutableStateFlow(ConsumedItemsUiState())
    val state: StateFlow<ConsumedItemsUiState> = _state.asStateFlow()

    private val _events = Channel<ConsumedItemsEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var loadJob: Job? = null

    init {
        observeChanges()
    }

    fun onAction(action: ConsumedItemsAction) {
        when (action) {
            ConsumedItemsAction.Load -> load(isRefresh = false, silent = false)
            ConsumedItemsAction.Refresh -> load(isRefresh = true, silent = true)
            ConsumedItemsAction.Back -> viewModelScope.launch {
                _events.send(ConsumedItemsEvent.NavigateBack)
            }
        }
    }

    // First emission = initial load with skeleton; later ones (item consumed/restored/deleted) reload quietly.
    private fun observeChanges() {
        viewModelScope.launch {
            var first = true
            observeItems()
                .catch { /* pull-to-refresh still works */ }
                .collect {
                    load(isRefresh = false, silent = !first)
                    first = false
                }
        }
    }

    private fun load(isRefresh: Boolean, silent: Boolean) {
        val userId = getCurrentUserId()
        if (!silent) _state.update { it.copy(loadState = ConsumedItemsLoadState.Loading) }
        if (isRefresh) _state.update { it.copy(isRefreshing = true) }
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            getConsumedItems(userId)
                .onSuccess { items ->
                    _state.update {
                        it.copy(loadState = ConsumedItemsLoadState.Content, items = items, isRefreshing = false)
                    }
                }
                .onError { err ->
                    _state.update {
                        if (silent) it.copy(isRefreshing = false)
                        else it.copy(loadState = ConsumedItemsLoadState.Error(errorMessages.provide(err)), isRefreshing = false)
                    }
                }
        }
    }
}
