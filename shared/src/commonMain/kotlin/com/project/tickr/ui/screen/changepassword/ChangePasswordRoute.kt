package com.project.tickr.ui.screen.changepassword

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import com.project.tickr.presentation.changepassword.ChangePasswordEvent
import com.project.tickr.presentation.changepassword.ChangePasswordViewModel
import com.project.tickr.presentation.navigation.Navigator
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ChangePasswordRoute(navigator: Navigator) {
    val viewModel: ChangePasswordViewModel = koinViewModel()
    val state = viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                ChangePasswordEvent.NavigateBack -> navigator.back()
                ChangePasswordEvent.NavigateBackToProfile -> {
                    // Pop ChangePassword → EditProfile → back at Profile tab in MainShell
                    navigator.back()
                    navigator.back()
                }
            }
        }
    }

    ChangePasswordScreen(
        state = state.value,
        onAction = viewModel::onAction,
    )
}
