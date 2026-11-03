package com.project.tickr.presentation.navigation

sealed interface Destination {
    val route: String

    data object Onboarding : Destination { override val route = "onboarding" }
    data object Home : Destination { override val route = "home" }
    data class ItemDetail(val id: Long) : Destination {
        override val route = "items/$id"
        companion object {
            const val PATTERN = "items/{id}"
            const val ARG_ID = "id"
        }
    }
    data object Help : Destination { override val route = "help" }
    data object About : Destination { override val route = "about" }
    data object Notifications : Destination { override val route = "notifications" }
    data object ConsumedItems : Destination { override val route = "consumed_items" }
}
