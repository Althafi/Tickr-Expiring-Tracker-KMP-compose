package com.project.tickr.presentation.insight

import com.project.tickr.domain.model.InsightData
import com.project.tickr.presentation.common.mvi.UiAction

data class InsightUiState(
    val isLoading: Boolean = true,
    val data: InsightData = InsightData.EMPTY,
)

sealed interface InsightAction : UiAction {
    data class ItemClicked(val id: Long) : InsightAction
    data object AddItemClicked : InsightAction
}

sealed interface InsightEvent {
    data class NavigateToDetail(val id: Long) : InsightEvent
    data object NavigateToAddItem : InsightEvent
}
