package com.project.tickr.presentation.category.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.tickr.core.result.onError
import com.project.tickr.core.result.onSuccess
import com.project.tickr.domain.error.ErrorMessageProvider
import com.project.tickr.domain.usecase.auth.GetCurrentUserIdUseCase
import com.project.tickr.domain.usecase.category.DeleteCategoryUseCase
import com.project.tickr.domain.usecase.category.GetCategoriesByUserUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CategoryListViewModel(
    private val getCategories: GetCategoriesByUserUseCase,
    private val deleteCategory: DeleteCategoryUseCase,
    private val getCurrentUserId: GetCurrentUserIdUseCase,
    private val errorMessages: ErrorMessageProvider
) : ViewModel() {

    private val _state = MutableStateFlow(CategoryListUiState())
    val state: StateFlow<CategoryListUiState> = _state.asStateFlow()

    private val _events = Channel<CategoryListEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onAction(action: CategoryListAction) {
        when (action) {
            CategoryListAction.Load -> load()
            is CategoryListAction.Delete -> delete(action.id)
            CategoryListAction.ClickAdd -> viewModelScope.launch { _events.send(CategoryListEvent.NavigateToForm(null)) }
            is CategoryListAction.ClickCategory -> viewModelScope.launch { _events.send(CategoryListEvent.NavigateToForm(action.id)) }
        }
    }

    private fun load() {
        val userId = getCurrentUserId() ?: return
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            getCategories(userId)
                .onSuccess { items -> _state.update { it.copy(isLoading = false, categories = items) } }
                .onError { error -> _state.update { it.copy(isLoading = false, error = errorMessages.provide(error)) } }
        }
    }

    private fun delete(id: Long) {
        viewModelScope.launch {
            deleteCategory(id)
                .onSuccess { load() }
                .onError { e -> _events.send(CategoryListEvent.ShowSnackbar(errorMessages.provide(e))) }
        }
    }
}
