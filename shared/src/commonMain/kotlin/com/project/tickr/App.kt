package com.project.tickr

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.project.tickr.domain.usecase.auth.ObserveSessionUseCase
import com.project.tickr.domain.usecase.onboarding.GetOnboardingSeenUseCase
import com.project.tickr.presentation.navigation.Destination
import com.project.tickr.ui.navigation.TickrNavGraph
import com.project.tickr.ui.theme.TickrTheme
import kotlinx.coroutines.flow.first
import org.koin.compose.koinInject

@Composable
@Preview
fun App() {
    TickrTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            val navController = rememberNavController()

            val getOnboardingSeen: GetOnboardingSeenUseCase = koinInject()
            val observeSession: ObserveSessionUseCase = koinInject()

            // null = sedang menentukan start destination; hindari NavHost sebelum siap
            var startDestination by remember { mutableStateOf<Destination?>(null) }

            LaunchedEffect(Unit) {
                val seen = getOnboardingSeen().first()
                val session = observeSession().first()
                startDestination = when {
                    !seen -> Destination.Onboarding
                    session != null -> Destination.Home
                    else -> Destination.Login
                }
            }

            startDestination?.let { start ->
                TickrNavGraph(
                    navController = navController,
                    startDestination = start,
                )
            }
        }
    }
}
