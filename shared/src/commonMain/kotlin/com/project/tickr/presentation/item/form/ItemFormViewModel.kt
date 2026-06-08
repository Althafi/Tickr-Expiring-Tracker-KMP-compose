package com.project.tickr.presentation.item.form

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.tickr.core.result.onError
import com.project.tickr.core.result.onSuccess
import com.project.tickr.domain.error.ErrorMessageProvider
import com.project.tickr.domain.model.Item
import com.project.tickr.domain.usecase.category.GetCategoriesByUserUseCase
import com.project.tickr.domain.usecase.item.CreateItemUseCase
import com.project.tickr.domain.usecase.item.GetItemUseCase
import com.project.tickr.domain.usecase.item.UpdateItemUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ItemFormViewModel(
    private val createItem: CreateItemUseCase,
    private val updateItem: UpdateItemUseCase,
    private val getItem: GetItemUseCase,
    private val getCategories: GetCategoriesByUserUseCase,
    private val errorMessages: ErrorMessageProvider
) : ViewModel() {

    private val _state = MutableStateFlow(ItemFormUiState())
    val state: StateFlow<ItemFormUiState> = _state.asStateFlow()

    private val _events = Channel<ItemFormEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onAction(action: ItemFormAction) {
        when (action) {
            is ItemFormAction.NameChanged -> _state.update { it.copy(name = action.value, nameError = null) }
            is ItemFormAction.ExpiryDateChanged -> _state.update { it.copy(expiryDate = action.value, expiryDateError = null) }
            is ItemFormAction.CategoryChanged -> _state.update { it.copy(categoryId = action.categoryId) }
            is ItemFormAction.NotesChanged -> _state.update { it.copy(notes = action.value) }
            ItemFormAction.Submit -> submit()
        }
    }

    private fun submit() {
        val current = _state.value
        _state.update { it.copy(isSaving = true, nameError = null, expiryDateError = null, generalError = null) }
        // TODO: implement after auth/userId available
        _state.update { it.copy(isSaving = false) }
    }
}
