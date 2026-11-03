package com.project.tickr.presentation.insight

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.tickr.domain.usecase.insight.ObserveInsightsUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Layar Wawasan: seluruh isinya dihitung dari database lokal, jadi selalu tersedia tanpa internet. */
class InsightViewModel(
    observeInsights: ObserveInsightsUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(InsightUiState())
    val state: StateFlow<InsightUiState> = _state.asStateFlow()

    private val _events = Channel<InsightEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            observeInsights()
                .catch { _state.update { it.copy(isLoading = false) } }
                .collect { data -> _state.value = InsightUiState(isLoading = false, data = data) }
        }
    }

    fun onAction(action: InsightAction) {
        viewModelScope.launch {
            when (action) {
                is InsightAction.ItemClicked -> _events.send(InsightEvent.NavigateToDetail(action.id))
                InsightAction.AddItemClicked -> _events.send(InsightEvent.NavigateToAddItem)
            }
        }
    }
}
