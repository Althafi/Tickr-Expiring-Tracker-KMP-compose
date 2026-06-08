package com.project.tickr.ui.screen.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import com.project.tickr.presentation.navigation.Navigator
import com.project.tickr.presentation.onboarding.OnboardingEvent
import com.project.tickr.presentation.onboarding.OnboardingViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun OnboardingRoute(
    navigator: Navigator,
) {
    val viewModel: OnboardingViewModel = koinViewModel()
    val state = viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is OnboardingEvent.NavigateToAuth -> navigator.navigate(com.project.tickr.presentation.navigation.Destination.Auth)
            }
        }
    }

    OnboardingScreen(
        state = state.value,
        onAction = viewModel::onAction,
    )
}
