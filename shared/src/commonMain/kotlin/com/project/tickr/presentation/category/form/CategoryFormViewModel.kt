package com.project.tickr.presentation.category.form

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.tickr.core.result.onError
import com.project.tickr.core.result.onSuccess
import com.project.tickr.domain.error.ErrorMessageProvider
import com.project.tickr.domain.usecase.category.CreateCategoryUseCase
import com.project.tickr.domain.usecase.category.GetCategoryUseCase
import com.project.tickr.domain.usecase.category.UpdateCategoryUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CategoryFormViewModel(
    private val createCategory: CreateCategoryUseCase,
    private val updateCategory: UpdateCategoryUseCase,
    private val getCategory: GetCategoryUseCase,
    private val errorMessages: ErrorMessageProvider
) : ViewModel() {

    private val _state = MutableStateFlow(CategoryFormUiState())
    val state: StateFlow<CategoryFormUiState> = _state.asStateFlow()

    private val _events = Channel<CategoryFormEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onAction(action: CategoryFormAction) {
        when (action) {
            is CategoryFormAction.NameChanged -> _state.update { it.copy(name = action.value) }
            is CategoryFormAction.IconNameChanged -> _state.update { it.copy(iconName = action.value) }
            is CategoryFormAction.ColorHexChanged -> _state.update { it.copy(colorHex = action.value) }
            CategoryFormAction.Submit -> submit()
        }
    }

    private fun submit() {
        _state.update { it.copy(isSaving = true) }
        // TODO: implement after auth/userId available
        _state.update { it.copy(isSaving = false) }
    }
}
