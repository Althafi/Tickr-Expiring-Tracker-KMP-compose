package com.project.tickr.ui.navigation

import androidx.navigation.NavHostController
import com.project.tickr.presentation.navigation.Destination
import com.project.tickr.presentation.navigation.Navigator

class ComposeNavigator(
    private val navController: NavHostController,
) : Navigator {
    override fun navigate(destination: Destination, popUpToInclusive: Boolean) {
        navController.navigate(destination.route) {
            if (popUpToInclusive) {
                popUpTo(navController.graph.id) { inclusive = true }
            }
        }
    }

    override fun back() {
        navController.popBackStack()
    }
}
