package com.project.tickr.ui.screen.expiry

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import com.project.tickr.presentation.expiry.ExpiryEvent
import com.project.tickr.presentation.expiry.ExpiryViewModel
import com.project.tickr.presentation.navigation.Destination
import com.project.tickr.presentation.navigation.Navigator

@Composable
fun ExpiryRoute(
    navigator: Navigator,
    viewModel: ExpiryViewModel,
    onAddItemRequest: () -> Unit = {},
) {
    val state = viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ExpiryEvent.NavigateToDetail ->
                    navigator.navigate(Destination.ItemDetail(event.id))
                ExpiryEvent.NavigateToAddItem -> onAddItemRequest()
                is ExpiryEvent.ShowError -> { /* TODO: show snackbar */ }
            }
        }
    }

    ExpiryScreen(
        state = state.value,
        onAction = viewModel::onAction,
    )
}
