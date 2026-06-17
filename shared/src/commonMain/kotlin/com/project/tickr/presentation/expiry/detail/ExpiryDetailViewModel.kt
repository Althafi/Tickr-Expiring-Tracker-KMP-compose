package com.project.tickr.presentation.expiry.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.tickr.core.result.onError
import com.project.tickr.core.result.onSuccess
import com.project.tickr.core.util.DateTimeUtil
import com.project.tickr.domain.error.ErrorMessageProvider
import com.project.tickr.domain.model.daysToUrgency
import com.project.tickr.domain.usecase.auth.GetCurrentUserIdUseCase
import com.project.tickr.domain.usecase.category.GetCategoriesUseCase
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

class ExpiryDetailViewModel(
    private val getItem: GetItemUseCase,
    private val deleteItem: DeleteItemUseCase,
    private val markItemConsumed: MarkItemConsumedUseCase,
    private val getCurrentUserId: GetCurrentUserIdUseCase,
    private val getCategories: GetCategoriesUseCase,
    private val dateTime: DateTimeUtil,
    private val errorMessages: ErrorMessageProvider,
) : ViewModel() {

    private val _state = MutableStateFlow(ExpiryDetailUiState())
    val state: StateFlow<ExpiryDetailUiState> = _state.asStateFlow()

    private val _events = Channel<ExpiryDetailEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onAction(action: ExpiryDetailAction) {
        when (action) {
            is ExpiryDetailAction.Load -> load(action.id)
            ExpiryDetailAction.Back -> viewModelScope.launch { _events.send(ExpiryDetailEvent.NavigateBack) }
            ExpiryDetailAction.Delete -> _state.update { it.copy(showDeleteDialog = true) }
            ExpiryDetailAction.DismissDeleteDialog -> _state.update { it.copy(showDeleteDialog = false) }
            ExpiryDetailAction.ConfirmDelete -> confirmDelete()
            ExpiryDetailAction.MarkConsumed -> markConsumed()
        }
    }

    private fun load(id: Long) {
        _state.update { it.copy(loadState = ExpiryDetailLoadState.Loading) }
        val userId = getCurrentUserId() ?: run {
            _state.update { it.copy(loadState = ExpiryDetailLoadState.Error("Sesi habis, silakan masuk kembali")) }
            return
        }

        viewModelScope.launch {
            getItem(id)
                .onSuccess { item ->
                    val categoryName = when (val r = getCategories()) {
                        is com.project.tickr.core.result.DataResult.Success ->
                            r.data.find { it.id == item.categoryId }?.name ?: "Lainnya"
                        is com.project.tickr.core.result.DataResult.Error -> "Lainnya"
                    }
                    val days = dateTime.daysUntil(item.expiryDate)
                    _state.update {
                        it.copy(
                            loadState = ExpiryDetailLoadState.Content,
                            item = item,
                            categoryName = categoryName,
                            daysUntilExpiry = days,
                            urgency = daysToUrgency(days),
                        )
                    }
                }
                .onError { err ->
                    _state.update { it.copy(loadState = ExpiryDetailLoadState.Error(errorMessages.provide(err))) }
                }
        }
    }

    private fun markConsumed() {
        val item = _state.value.item ?: return
        _state.update { it.copy(isProcessing = true) }
        viewModelScope.launch {
            markItemConsumed(item, true)
                .onSuccess { _events.send(ExpiryDetailEvent.ItemRemoved) }
                .onError { err ->
                    _state.update { it.copy(isProcessing = false) }
                    _events.send(ExpiryDetailEvent.ShowError(errorMessages.provide(err)))
                }
        }
    }

    private fun confirmDelete() {
        val item = _state.value.item ?: return
        _state.update { it.copy(showDeleteDialog = false, isProcessing = true) }
        viewModelScope.launch {
            deleteItem(item.id)
                .onSuccess { _events.send(ExpiryDetailEvent.NavigateBack) }
                .onError { err ->
                    _state.update { it.copy(isProcessing = false) }
                    _events.send(ExpiryDetailEvent.ShowError(errorMessages.provide(err)))
                }
        }
    }
}
