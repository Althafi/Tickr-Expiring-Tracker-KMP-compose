package com.project.tickr.ui.screen.help

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import com.project.tickr.presentation.help.HelpEvent
import com.project.tickr.presentation.help.HelpViewModel
import com.project.tickr.presentation.navigation.Navigator
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HelpRoute(navigator: Navigator) {
    val viewModel: HelpViewModel = koinViewModel()
    val state = viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                HelpEvent.NavigateBack -> navigator.back()
                is HelpEvent.OpenExternal -> {
                    // TODO(user): implementasi expect/actual openUrl(event.url)
                }
            }
        }
    }

    HelpScreen(
        state = state.value,
        onAction = viewModel::onAction,
    )
}
