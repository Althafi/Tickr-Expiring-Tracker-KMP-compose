package com.project.tickr.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.project.tickr.presentation.navigation.Destination
import com.project.tickr.ui.screen.onboarding.OnboardingRoute

@Composable
fun TickrNavGraph(
    navController: NavHostController,
    startDestination: Destination = Destination.Onboarding,
) {
    val navigator = remember { ComposeNavigator(navController) }

    NavHost(
        navController = navController,
        startDestination = startDestination.route,
    ) {
        composable(route = Destination.Onboarding.route) {
            OnboardingRoute(navigator = navigator)
        }

        composable(route = Destination.Auth.route) {
            // AuthRoute(navigator = navigator) - TODO: implement when Auth screen is ready
        }

        composable(route = Destination.Home.route) {
            // HomeRoute(navigator = navigator) - TODO: implement when Home screen is ready
        }

        // Add other screens as needed
    }
}
