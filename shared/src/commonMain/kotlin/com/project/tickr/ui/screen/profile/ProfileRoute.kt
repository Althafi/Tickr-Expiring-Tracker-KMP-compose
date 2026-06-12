package com.project.tickr.ui.screen.profile

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import com.project.tickr.presentation.navigation.Destination
import com.project.tickr.presentation.navigation.Navigator
import com.project.tickr.presentation.profile.ProfileEvent
import com.project.tickr.presentation.profile.ProfileViewModel

@Composable
fun ProfileRoute(
    navigator: Navigator,
    viewModel: ProfileViewModel,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val state = viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                ProfileEvent.NavigateToAuth ->
                    navigator.navigate(Destination.Login, popUpToInclusive = true)
                ProfileEvent.NavigateToEditProfile ->
                    navigator.navigate(Destination.EditProfile)
                ProfileEvent.NavigateToHelp ->
                    navigator.navigate(Destination.Help)
                is ProfileEvent.ShowSnackbar ->
                    snackbarHostState.showSnackbar(event.message)
                ProfileEvent.Saved -> Unit
            }
        }
    }

    ProfileScreen(
        state = state.value,
        onAction = viewModel::onAction,
        snackbarHostState = snackbarHostState,
    )
}
