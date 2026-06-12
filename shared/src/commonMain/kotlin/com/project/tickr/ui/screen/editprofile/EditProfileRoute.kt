package com.project.tickr.ui.screen.editprofile

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import com.project.tickr.presentation.editprofile.EditProfileEvent
import com.project.tickr.presentation.editprofile.EditProfileViewModel
import com.project.tickr.presentation.navigation.Destination
import com.project.tickr.presentation.navigation.Navigator
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun EditProfileRoute(
    navigator: Navigator,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val viewModel: EditProfileViewModel = koinViewModel()
    val state = viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                EditProfileEvent.NavigateBack -> navigator.back()
                EditProfileEvent.NavigateToChangePassword ->
                    navigator.navigate(Destination.ChangePassword)
                is EditProfileEvent.ShowSnackbar ->
                    snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    EditProfileScreen(
        state = state.value,
        onAction = viewModel::onAction,
        snackbarHostState = snackbarHostState,
    )
}
