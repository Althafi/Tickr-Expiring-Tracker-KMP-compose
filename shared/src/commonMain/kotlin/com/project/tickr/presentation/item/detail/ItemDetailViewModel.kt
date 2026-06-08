package com.project.tickr.presentation.item.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.tickr.core.result.onError
import com.project.tickr.core.result.onSuccess
import com.project.tickr.domain.error.ErrorMessageProvider
import com.project.tickr.domain.usecase.item.DeleteItemUseCase
import com.project.tickr.domain.usecase.item.GetItemUseCase
import com.project.tickr.domain.usecase.item.MarkItemConsumedUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ItemDetailViewModel(
    private val getItem: GetItemUseCase,
    private val deleteItem: DeleteItemUseCase,
    private val markItemConsumed: MarkItemConsumedUseCase,
    private val errorMessages: ErrorMessageProvider
) : ViewModel() {

    private val _state = MutableStateFlow(ItemDetailUiState())
    val state: StateFlow<ItemDetailUiState> = _state.asStateFlow()

    private val _events = Channel<ItemDetailEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onAction(action: ItemDetailAction) {
        when (action) {
            is ItemDetailAction.Load -> load(action.id)
            ItemDetailAction.Delete -> delete()
            ItemDetailAction.MarkConsumed -> markConsumed()
            ItemDetailAction.ClickEdit -> {
                _state.value.item?.let {
                    viewModelScope.launch { _events.send(ItemDetailEvent.NavigateToEdit(it.id)) }
                }
            }
        }
    }

    private fun load(id: Long) {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            getItem(id)
                .onSuccess { item -> _state.update { it.copy(isLoading = false, item = item) } }
                .onError { error -> _state.update { it.copy(isLoading = false, error = errorMessages.provide(error)) } }
        }
    }

    private fun delete() {
        _state.value.item?.let { item ->
            viewModelScope.launch {
                deleteItem(item.id)
                    .onSuccess { _events.send(ItemDetailEvent.NavigateBack) }
                    .onError { error -> _events.send(ItemDetailEvent.ShowSnackbar(errorMessages.provide(error))) }
            }
        }
    }

    private fun markConsumed() {
        val currentItem = _state.value.item
        currentItem?.let {
            viewModelScope.launch {
                markItemConsumed(currentItem, true)
                    .onSuccess { _ ->
                        _state.update { state -> state.copy(item = currentItem.copy(isConsumed = true)) }
                    }
                    .onError { error -> _events.send(ItemDetailEvent.ShowSnackbar(errorMessages.provide(error))) }
            }
        }
    }
}
