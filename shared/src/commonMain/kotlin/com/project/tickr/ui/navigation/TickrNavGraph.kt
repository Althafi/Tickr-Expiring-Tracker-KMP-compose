package com.project.tickr.ui.navigation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.project.tickr.presentation.navigation.Destination
import com.project.tickr.ui.screen.auth.LoginRoute
import com.project.tickr.ui.screen.auth.RegisterRoute
import com.project.tickr.ui.screen.auth.RegisterSuccessRoute
import com.project.tickr.ui.screen.home.HomePlaceholderScreen
import com.project.tickr.ui.screen.onboarding.OnboardingRoute

private const val TRANSITION_DURATION = 300

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
        // ─── Onboarding ───────────────────────────────────────────────────────
        composable(
            route = Destination.Onboarding.route,
            exitTransition = { fadeOut(tween(TRANSITION_DURATION)) },
        ) {
            OnboardingRoute(navigator = navigator)
        }

        // ─── Auth: Login ──────────────────────────────────────────────────────
        composable(
            route = Destination.Login.route,
            enterTransition = {
                slideInHorizontally(
                    animationSpec = tween(TRANSITION_DURATION, easing = FastOutSlowInEasing),
                ) { it } + fadeIn(tween(TRANSITION_DURATION))
            },
            exitTransition = {
                slideOutHorizontally(
                    animationSpec = tween(TRANSITION_DURATION, easing = FastOutSlowInEasing),
                ) { -it / 3 } + fadeOut(tween(TRANSITION_DURATION))
            },
            popEnterTransition = {
                slideInHorizontally(
                    animationSpec = tween(TRANSITION_DURATION, easing = FastOutSlowInEasing),
                ) { -it / 3 } + fadeIn(tween(TRANSITION_DURATION))
            },
            popExitTransition = {
                slideOutHorizontally(
                    animationSpec = tween(TRANSITION_DURATION, easing = FastOutSlowInEasing),
                ) { it } + fadeOut(tween(TRANSITION_DURATION))
            },
        ) {
            LoginRoute(navigator = navigator)
        }

        // ─── Auth: Register ───────────────────────────────────────────────────
        composable(
            route = Destination.Register.route,
            enterTransition = {
                slideInHorizontally(
                    animationSpec = tween(TRANSITION_DURATION, easing = FastOutSlowInEasing),
                ) { it } + fadeIn(tween(TRANSITION_DURATION))
            },
            exitTransition = {
                slideOutHorizontally(
                    animationSpec = tween(TRANSITION_DURATION, easing = FastOutSlowInEasing),
                ) { it } + fadeOut(tween(TRANSITION_DURATION))
            },
            popEnterTransition = {
                slideInHorizontally(
                    animationSpec = tween(TRANSITION_DURATION, easing = FastOutSlowInEasing),
                ) { it } + fadeIn(tween(TRANSITION_DURATION))
            },
            popExitTransition = {
                slideOutHorizontally(
                    animationSpec = tween(TRANSITION_DURATION, easing = FastOutSlowInEasing),
                ) { it } + fadeOut(tween(TRANSITION_DURATION))
            },
        ) {
            RegisterRoute(navigator = navigator)
        }

        // ─── Auth: RegisterSuccess (transient) ────────────────────────────────
        composable(
            route = Destination.AuthSuccess.route,
            enterTransition = {
                scaleIn(
                    animationSpec = tween(TRANSITION_DURATION, easing = FastOutSlowInEasing),
                    initialScale = 0.92f,
                ) + fadeIn(tween(TRANSITION_DURATION))
            },
            exitTransition = { fadeOut(tween(TRANSITION_DURATION)) },
        ) {
            RegisterSuccessRoute(navigator = navigator)
        }

        // ─── Home (placeholder — TODO: implementasi Phase berikutnya) ─────────
        composable(
            route = Destination.Home.route,
            enterTransition = {
                slideInVertically(
                    animationSpec = tween(TRANSITION_DURATION, easing = FastOutSlowInEasing),
                ) { it / 12 } + fadeIn(tween(TRANSITION_DURATION))
            },
        ) {
            HomePlaceholderScreen()
        }

        // Legacy Auth route (kept untuk backward compat dengan kode lama)
        composable(route = Destination.Auth.route) {
            LoginRoute(navigator = navigator)
        }
    }
}
