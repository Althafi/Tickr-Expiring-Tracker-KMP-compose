package com.project.tickr.presentation.item.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.tickr.core.result.onError
import com.project.tickr.core.result.onSuccess
import com.project.tickr.domain.error.ErrorMessageProvider
import com.project.tickr.domain.usecase.auth.GetCurrentUserIdUseCase
import com.project.tickr.domain.usecase.item.DeleteItemUseCase
import com.project.tickr.domain.usecase.item.GetItemsByUserUseCase
import com.project.tickr.domain.usecase.item.MarkItemConsumedUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ItemListViewModel(
    private val getItemsByUser: GetItemsByUserUseCase,
    private val deleteItem: DeleteItemUseCase,
    private val markItemConsumed: MarkItemConsumedUseCase,
    private val getCurrentUserId: GetCurrentUserIdUseCase,
    private val errorMessages: ErrorMessageProvider
) : ViewModel() {

    private val _state = MutableStateFlow(ItemListUiState())
    val state: StateFlow<ItemListUiState> = _state.asStateFlow()

    private val _events = Channel<ItemListEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onAction(action: ItemListAction) {
        when (action) {
            ItemListAction.Load, ItemListAction.Refresh -> load()
            is ItemListAction.Delete -> delete(action.id)
            is ItemListAction.MarkConsumed -> markConsumed(action.id)
            is ItemListAction.ClickItem -> viewModelScope.launch {
                _events.send(ItemListEvent.NavigateToDetail(action.id))
            }
            ItemListAction.ClickAdd -> viewModelScope.launch {
                _events.send(ItemListEvent.NavigateToCreate("items/new"))
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
            getItemsByUser(userId)
                .onSuccess { items -> _state.update { it.copy(isLoading = false, items = items) } }
                .onError { e -> _state.update { it.copy(isLoading = false, error = errorMessages.provide(e)) } }
        }
    }

    private fun delete(id: Long) {
        viewModelScope.launch {
            deleteItem(id)
                .onSuccess { load() }
                .onError { e -> _events.send(ItemListEvent.ShowSnackbar(errorMessages.provide(e))) }
        }
    }

    private fun markConsumed(id: Long) {
        val item = _state.value.items.find { it.id == id } ?: return
        viewModelScope.launch {
            markItemConsumed(item, true)
                .onSuccess { load() }
                .onError { e -> _events.send(ItemListEvent.ShowSnackbar(errorMessages.provide(e))) }
        }
    }
}
