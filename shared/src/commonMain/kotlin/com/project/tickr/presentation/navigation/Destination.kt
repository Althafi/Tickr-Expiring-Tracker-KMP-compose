package com.project.tickr.presentation.navigation

sealed interface Destination {
    val route: String

    data object Onboarding : Destination { override val route = "onboarding" }
    data object Auth : Destination { override val route = "auth" }
    data object Home : Destination { override val route = "home" }
    data object ItemList : Destination { override val route = "items" }
    data class ItemDetail(val id: Long) : Destination {
        override val route = "items/$id"
        companion object {
            const val PATTERN = "items/{id}"
            const val ARG_ID = "id"
        }
    }
    data object ItemCreate : Destination { override val route = "items/new" }
    data class ItemEdit(val id: Long) : Destination {
        override val route = "items/$id/edit"
        companion object {
            const val PATTERN = "items/{id}/edit"
            const val ARG_ID = "id"
        }
    }
    data object CategoryList : Destination { override val route = "categories" }
    data object Profile : Destination { override val route = "profile" }
}
