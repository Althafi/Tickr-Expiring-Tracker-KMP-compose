package com.project.tickr.ui.screen.expiry.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import com.project.tickr.presentation.expiry.detail.ExpiryDetailAction
import com.project.tickr.presentation.expiry.detail.ExpiryDetailEvent
import com.project.tickr.presentation.expiry.detail.ExpiryDetailViewModel
import com.project.tickr.presentation.navigation.Navigator

@Composable
fun ItemDetailRoute(
    itemId: Long,
    navigator: Navigator,
    viewModel: ExpiryDetailViewModel,
) {
    val state = viewModel.state.collectAsState()

    LaunchedEffect(itemId) {
        viewModel.onAction(ExpiryDetailAction.Load(itemId))
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                ExpiryDetailEvent.NavigateBack -> navigator.back()
                ExpiryDetailEvent.ItemRemoved -> navigator.back()
                is ExpiryDetailEvent.ShowError -> { /* TODO: show snackbar */ }
            }
        }
    }

    ItemDetailScreen(
        state = state.value,
        onAction = viewModel::onAction,
    )
}
