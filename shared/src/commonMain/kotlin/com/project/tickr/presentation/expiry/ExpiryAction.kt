package com.project.tickr.presentation.expiry

import com.project.tickr.domain.model.TimeRange
import com.project.tickr.domain.model.Urgency

sealed interface ExpiryAction {
    object Load : ExpiryAction
    object Refresh : ExpiryAction
    data class QueryChanged(val query: String) : ExpiryAction
    data class CategorySelected(val categoryName: String?) : ExpiryAction
    object OpenFilterSheet : ExpiryAction
    object DismissFilterSheet : ExpiryAction
    data class ToggleUrgency(val urgency: Urgency) : ExpiryAction
    data class SelectTimeRange(val timeRange: TimeRange?) : ExpiryAction
    object ApplyFilter : ExpiryAction
    object ResetFilter : ExpiryAction
    data class ItemClicked(val id: Long) : ExpiryAction
    object AddClicked : ExpiryAction
}
