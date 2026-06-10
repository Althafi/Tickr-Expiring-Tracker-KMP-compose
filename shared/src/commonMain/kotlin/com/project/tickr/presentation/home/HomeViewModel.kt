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
import com.project.tickr.domain.usecase.profile.GetProfileUseCase
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

class HomeViewModel(
    private val getExpiringGrouped: GetExpiringItemsGroupedUseCase,
    private val getCategoryConsumption: GetCategoryConsumptionUseCase,
    private val getWasteTrend: GetWasteTrendUseCase,
    private val getProfile: GetProfileUseCase,
    private val getCurrentUserId: GetCurrentUserIdUseCase,
    private val dateTime: DateTimeUtil,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    private val _events = Channel<HomeEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        startClockTicker()
        load()
    }

    fun onAction(action: HomeAction) {
        when (action) {
            HomeAction.Load, HomeAction.Refresh -> load()
            HomeAction.OpenNotifications -> viewModelScope.launch {
                _events.send(HomeEvent.NavigateToNotifications)
            }
            HomeAction.OpenAddItem -> viewModelScope.launch {
                _events.send(HomeEvent.ShowAddItemSheet)
            }
            HomeAction.SeeAllCategories -> { /* navigate to categories — TODO */ }
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
                        displayDate = dateTime.formatDisplayDate(now),
                        displayTime = dateTime.formatDisplayTime(now),
                    )
                }
                delay(1_000L)
            }
        }
    }

    private fun load() {
        val userId = getCurrentUserId() ?: run {
            _state.update { it.copy(isLoading = false, error = "Sesi habis, silakan masuk kembali") }
            return
        }
        _state.update { it.copy(isLoading = true, error = null) }

        // All data fetches run in parallel inside a single parent coroutine.
        // isLoading is cleared only after every child job completes, so the
        // skeleton stays visible for the full duration of the load/refresh.
        viewModelScope.launch {
            val jobs = listOf(
                launch {
                    getProfile(userId).onSuccess { profile ->
                        _state.update { it.copy(userName = profile.fullName ?: "") }
                    }
                },
                launch {
                    getExpiringGrouped(userId).onSuccess { groups ->
                        val criticalCount = groups.sumOf { g ->
                            g.items.count { it.urgency == Urgency.CRITICAL }
                        }
                        _state.update {
                            it.copy(expiringGroups = groups, criticalCount = criticalCount)
                        }
                    }.onError { err ->
                        _state.update { it.copy(error = err.message) }
                    }
                },
                launch {
                    getCategoryConsumption(userId).onSuccess { slices ->
                        val total = slices.sumOf { it.count }
                        _state.update { it.copy(slices = slices, totalItems = total) }
                    }
                },
                launch {
                    getWasteTrend(userId).onSuccess { trend ->
                        _state.update { it.copy(wasteTrend = trend) }
                    }
                },
            )
            jobs.joinAll()
            _state.update { it.copy(isLoading = false) }
        }
    }
}
