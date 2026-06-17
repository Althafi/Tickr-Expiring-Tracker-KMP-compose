package com.project.tickr.ui.screen.consumed

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import com.project.tickr.presentation.consumed.ConsumedItemsEvent
import com.project.tickr.presentation.consumed.ConsumedItemsViewModel
import com.project.tickr.presentation.navigation.Navigator

@Composable
fun ConsumedItemsRoute(
    navigator: Navigator,
    viewModel: ConsumedItemsViewModel,
) {
    val state = viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                ConsumedItemsEvent.NavigateBack -> navigator.back()
            }
        }
    }

    ConsumedItemsScreen(
        state = state.value,
        onAction = viewModel::onAction,
    )
}
