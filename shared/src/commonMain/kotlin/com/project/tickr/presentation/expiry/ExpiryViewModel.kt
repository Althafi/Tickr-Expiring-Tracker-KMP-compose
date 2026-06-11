package com.project.tickr.presentation.expiry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.tickr.core.result.onError
import com.project.tickr.core.result.onSuccess
import com.project.tickr.core.util.DateTimeUtil
import com.project.tickr.domain.usecase.auth.GetCurrentUserIdUseCase
import com.project.tickr.domain.usecase.expiry.GetConsumptionStatsUseCase
import com.project.tickr.domain.usecase.expiry.GetExpiryListItemsUseCase
import com.project.tickr.presentation.navigation.Destination
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

class ExpiryViewModel(
    private val getExpiryItems: GetExpiryListItemsUseCase,
    private val getConsumptionStats: GetConsumptionStatsUseCase,
    private val getCurrentUserId: GetCurrentUserIdUseCase,
    private val dateTime: DateTimeUtil,
) : ViewModel() {

    private val _state = MutableStateFlow(ExpiryUiState())
    val state: StateFlow<ExpiryUiState> = _state.asStateFlow()

    private val _events = Channel<ExpiryEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        startClockTicker()
        load(isRefresh = false)
    }

    fun onAction(action: ExpiryAction) {
        when (action) {
            ExpiryAction.Load -> load(isRefresh = false)
            ExpiryAction.Refresh -> load(isRefresh = true)

            is ExpiryAction.QueryChanged -> {
                _state.update { it.copy(activeFilter = it.activeFilter.copy(query = action.query)) }
                load(isRefresh = false)
            }

            is ExpiryAction.CategorySelected -> {
                _state.update { it.copy(activeFilter = it.activeFilter.copy(categoryName = action.categoryName)) }
                load(isRefresh = false)
            }

            ExpiryAction.OpenFilterSheet -> _state.update {
                it.copy(
                    isFilterSheetVisible = true,
                    pendingUrgencies = it.activeFilter.urgencies,
                    pendingTimeRange = it.activeFilter.timeRange,
                )
            }

            ExpiryAction.DismissFilterSheet -> _state.update { it.copy(isFilterSheetVisible = false) }

            is ExpiryAction.ToggleUrgency -> _state.update {
                val updated = if (action.urgency in it.pendingUrgencies)
                    it.pendingUrgencies - action.urgency
                else
                    it.pendingUrgencies + action.urgency
                it.copy(pendingUrgencies = updated)
            }

            is ExpiryAction.SelectTimeRange -> _state.update {
                it.copy(pendingTimeRange = action.timeRange)
            }

            ExpiryAction.ApplyFilter -> {
                _state.update {
                    it.copy(
                        activeFilter = it.activeFilter.copy(
                            urgencies = it.pendingUrgencies,
                            timeRange = it.pendingTimeRange,
                        ),
                        isFilterSheetVisible = false,
                    )
                }
                load(isRefresh = false)
            }

            ExpiryAction.ResetFilter -> {
                _state.update {
                    it.copy(
                        activeFilter = it.activeFilter.copy(urgencies = emptySet(), timeRange = null),
                        pendingUrgencies = emptySet(),
                        pendingTimeRange = null,
                        isFilterSheetVisible = false,
                    )
                }
                load(isRefresh = false)
            }

            is ExpiryAction.ItemClicked -> viewModelScope.launch {
                _events.send(ExpiryEvent.NavigateToDetail(action.id))
            }

            ExpiryAction.AddClicked -> viewModelScope.launch {
                _events.send(ExpiryEvent.NavigateToAddItem)
            }
        }
    }

    private fun startClockTicker() {
        viewModelScope.launch {
            while (true) {
                val now = dateTime.nowEpochMillis()
                _state.update {
                    it.copy(
                        nowEpochMillis = now,
                        displayDate = dateTime.formatDisplayDate(now),
                        displayTime = dateTime.formatDisplayTime(now),
                    )
                }
                delay(1_000L)
            }
        }
    }

    private fun load(isRefresh: Boolean) {
        val userId = getCurrentUserId() ?: run {
            _state.update { it.copy(loadState = ExpiryLoadState.Error("Sesi habis, silakan masuk kembali")) }
            return
        }

        _state.update {
            it.copy(
                loadState = ExpiryLoadState.Loading,
                isRefreshing = isRefresh,
            )
        }

        viewModelScope.launch {
            val filter = _state.value.activeFilter
            val jobs = listOf(
                launch {
                    getConsumptionStats(userId).onSuccess { stats ->
                        _state.update { it.copy(stats = stats) }
                    }
                },
                launch {
                    getExpiryItems(userId, filter)
                        .onSuccess { items ->
                            _state.update {
                                it.copy(
                                    items = items,
                                    loadState = if (items.isEmpty()) ExpiryLoadState.Empty
                                    else ExpiryLoadState.Content,
                                )
                            }
                        }
                        .onError { err ->
                            _state.update { it.copy(loadState = ExpiryLoadState.Error(err.message)) }
                        }
                },
            )
            jobs.joinAll()
            _state.update { it.copy(isRefreshing = false) }
        }
    }
}
