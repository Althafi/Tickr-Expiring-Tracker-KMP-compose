package com.project.tickr.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.tickr.core.result.onError
import com.project.tickr.core.result.onSuccess
import com.project.tickr.core.util.DateTimeUtil
import com.project.tickr.domain.model.Urgency
import com.project.tickr.domain.usecase.auth.GetCurrentUserIdUseCase
import com.project.tickr.domain.usecase.home.GetCategoryConsumptionUseCase
import com.project.tickr.domain.usecase.home.GetExpiringItemsGroupedUseCase
import com.project.tickr.domain.usecase.home.GetWasteTrendUseCase
import com.project.tickr.domain.usecase.item.ObserveItemsUseCase
import com.project.tickr.presentation.navigation.Destination
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import com.project.tickr.presentation.common.ErrorMessageProvider

class HomeViewModel(
    private val observeItems: ObserveItemsUseCase,
    private val getExpiringGrouped: GetExpiringItemsGroupedUseCase,
    private val getCategoryConsumption: GetCategoryConsumptionUseCase,
    private val getWasteTrend: GetWasteTrendUseCase,
    private val getCurrentUserId: GetCurrentUserIdUseCase,
    private val dateTime: DateTimeUtil,
    private val errorMessages: ErrorMessageProvider,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    private val _events = Channel<HomeEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var loadJob: Job? = null

    init {
        startClockTicker()
        observeChanges()
    }

    fun onAction(action: HomeAction) {
        when (action) {
            HomeAction.Load, HomeAction.Refresh -> load(showLoading = true)
            HomeAction.OpenNotifications -> viewModelScope.launch {
                _events.send(HomeEvent.NavigateToNotifications)
            }
            HomeAction.OpenAddItem -> viewModelScope.launch {
                _events.send(HomeEvent.ShowAddItemSheet)
            }
            HomeAction.SeeAllCategories -> viewModelScope.launch {
                _events.send(HomeEvent.NavigateToConsumedItems)
            }
            is HomeAction.ClickItem -> viewModelScope.launch {
                _events.send(HomeEvent.NavigateToItemDetail(Destination.ItemDetail(action.id)))
            }
            is HomeAction.TabSelected -> { /* handled by MainShell */ }
        }
    }

    private fun startClockTicker() {
        viewModelScope.launch {
            while (true) {
                val now = dateTime.nowEpochMillis()
                _state.update {
                    it.copy(
                        nowEpochMillis = now,
                        date = dateTime.dateParts(now),
                        displayTime = dateTime.formatDisplayTime(now),
                    )
                }
                delay(1_000L)
            }
        }
    }

    // The first emission is the initial load (with skeleton). Every later emission means an item
    // changed somewhere (add sheet, detail screen, ...), so reload quietly without flashing the skeleton.
    private fun observeChanges() {
        viewModelScope.launch {
            var first = true
            observeItems()
                .catch { /* pull-to-refresh still works */ }
                .collect {
                    load(showLoading = first)
                    first = false
                }
        }
    }

    private fun load(showLoading: Boolean) {
        val userId = getCurrentUserId()
        if (showLoading) _state.update { it.copy(isLoading = true, error = null) }

        // A newer load supersedes an older one, so a slow earlier result can't overwrite fresher data.
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            // isLoading is cleared only after every fetch completes.
            coroutineScope {
                launch {
                    getExpiringGrouped(userId).onSuccess { groups ->
                        val criticalCount = groups.sumOf { g ->
                            g.items.count { it.urgency == Urgency.CRITICAL }
                        }
                        _state.update {
                            it.copy(expiringGroups = groups, criticalCount = criticalCount, error = null)
                        }
                    }.onError { err ->
                        _state.update { it.copy(error = errorMessages.provide(err)) }
                    }
                }
                launch {
                    getCategoryConsumption(userId).onSuccess { slices ->
                        val total = slices.sumOf { it.count }
                        _state.update { it.copy(slices = slices, totalItems = total) }
                    }
                }
                launch {
                    getWasteTrend(userId).onSuccess { trend ->
                        _state.update { it.copy(wasteTrend = trend) }
                    }
                }
            }
            _state.update { it.copy(isLoading = false) }
        }
    }
}
