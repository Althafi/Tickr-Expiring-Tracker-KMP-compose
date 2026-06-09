package com.project.tickr.ui.screen.auth

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import com.project.tickr.presentation.common.asString
import com.project.tickr.presentation.navigation.Destination
import com.project.tickr.presentation.navigation.Navigator
import com.project.tickr.presentation.register.RegisterEvent
import com.project.tickr.presentation.register.RegisterViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun RegisterRoute(
    navigator: Navigator,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val viewModel: RegisterViewModel = koinViewModel()
    val state = viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                RegisterEvent.NavigateToSuccess -> {
                    navigator.navigate(Destination.AuthSuccess)
                }
                RegisterEvent.NavigateToLogin -> {
                    navigator.back()
                }
                is RegisterEvent.ShowError -> {
                    snackbarHostState.showSnackbar(event.message.asString())
                }
            }
        }
    }

    RegisterScreen(
        state = state.value,
        onAction = viewModel::onAction,
        snackbarHostState = snackbarHostState,
    )
}
