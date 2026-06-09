package com.project.tickr.ui.screen.auth

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import com.project.tickr.presentation.common.asString
import com.project.tickr.presentation.login.LoginEvent
import com.project.tickr.presentation.login.LoginViewModel
import com.project.tickr.presentation.navigation.Destination
import com.project.tickr.presentation.navigation.Navigator
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LoginRoute(
    navigator: Navigator,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val viewModel: LoginViewModel = koinViewModel()
    val state = viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                LoginEvent.NavigateToHome -> {
                    navigator.navigate(Destination.Home, popUpToInclusive = true)
                }
                LoginEvent.NavigateToRegister -> {
                    navigator.navigate(Destination.Register)
                }
                is LoginEvent.ShowError -> {
                    snackbarHostState.showSnackbar(event.message.asString())
                }
            }
        }
    }

    LoginScreen(
        state = state.value,
        onAction = viewModel::onAction,
        snackbarHostState = snackbarHostState,
    )
}
