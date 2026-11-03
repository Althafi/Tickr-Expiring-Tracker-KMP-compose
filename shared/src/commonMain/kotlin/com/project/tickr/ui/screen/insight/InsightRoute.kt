package com.project.tickr.ui.screen.insight

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.project.tickr.presentation.insight.InsightEvent
import com.project.tickr.presentation.insight.InsightViewModel
import com.project.tickr.presentation.navigation.Destination
import com.project.tickr.presentation.navigation.Navigator

@Composable
fun InsightRoute(
    navigator: Navigator,
    viewModel: InsightViewModel,
    onAddItemRequest: () -> Unit = {},
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is InsightEvent.NavigateToDetail -> navigator.navigate(Destination.ItemDetail(event.id))
                InsightEvent.NavigateToAddItem -> onAddItemRequest()
            }
        }
    }

    InsightScreen(state = state, onAction = viewModel::onAction)
}
