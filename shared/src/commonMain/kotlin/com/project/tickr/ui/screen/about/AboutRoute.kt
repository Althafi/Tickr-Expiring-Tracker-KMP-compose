package com.project.tickr.ui.screen.about

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.project.tickr.presentation.about.AboutViewModel
import com.project.tickr.presentation.navigation.Navigator
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AboutRoute(
    navigator: Navigator,
    viewModel: AboutViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    AboutScreen(info = state.info, onBack = navigator::back)
}
