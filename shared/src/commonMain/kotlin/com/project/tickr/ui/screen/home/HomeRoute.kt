package com.project.tickr.ui.screen.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import com.project.tickr.presentation.home.HomeEvent
import com.project.tickr.presentation.home.HomeViewModel
import com.project.tickr.presentation.navigation.Destination
import com.project.tickr.presentation.navigation.Navigator

@Composable
fun HomeRoute(
    navigator: Navigator,
    viewModel: HomeViewModel,
) {
    val state = viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is HomeEvent.NavigateToItemDetail -> navigator.navigate(event.destination)
                HomeEvent.NavigateToNotifications -> navigator.navigate(Destination.Notifications)
                HomeEvent.ShowAddItemSheet -> { /* handled by MainShell */ }
                HomeEvent.NavigateToConsumedItems -> navigator.navigate(Destination.ConsumedItems)
            }
        }
    }

    HomeScreen(
        state = state.value,
        onAction = viewModel::onAction,
    )
}
