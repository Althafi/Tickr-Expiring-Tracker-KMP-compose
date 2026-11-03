package com.project.tickr.ui.screen.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.project.tickr.presentation.navigation.Navigator
import com.project.tickr.presentation.settings.SettingsEvent
import com.project.tickr.presentation.settings.SettingsViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsRoute(
    navigator: Navigator,
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is SettingsEvent.Navigate -> navigator.navigate(event.destination)
            }
        }
    }

    SettingsScreen(state = state, onAction = viewModel::onAction)
}
