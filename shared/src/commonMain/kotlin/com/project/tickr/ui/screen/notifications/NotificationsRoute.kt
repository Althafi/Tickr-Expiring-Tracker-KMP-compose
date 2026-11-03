package com.project.tickr.ui.screen.notifications

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.project.tickr.presentation.navigation.Navigator
import com.project.tickr.presentation.notifications.NotificationsAction
import com.project.tickr.presentation.notifications.NotificationsEvent
import com.project.tickr.presentation.notifications.NotificationsViewModel
import com.project.tickr.ui.common.rememberOpenNotificationSettings
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun NotificationsRoute(
    navigator: Navigator,
    viewModel: NotificationsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val openSettings = rememberOpenNotificationSettings()

    // Pengguna mungkin baru mengaktifkan notifikasi lewat pengaturan sistem.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.onAction(NotificationsAction.Refresh) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is NotificationsEvent.Navigate -> navigator.navigate(event.destination)
                NotificationsEvent.NavigateBack -> navigator.back()
            }
        }
    }

    NotificationsScreen(state = state, onAction = viewModel::onAction, onOpenSettings = openSettings)
}
