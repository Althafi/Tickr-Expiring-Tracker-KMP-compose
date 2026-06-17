package com.project.tickr.presentation.navigation

sealed interface Destination {
    val route: String

    data object Onboarding : Destination { override val route = "onboarding" }
    data object Auth : Destination { override val route = "auth" }
    // Phase 3.5: split Auth menjadi layar terpisah
    data object Login : Destination { override val route = "login" }
    data object Register : Destination { override val route = "register" }
    data object AuthSuccess : Destination { override val route = "auth_success" }
    data object AuthFailed : Destination { override val route = "auth_failed" }
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
    data object EditProfile : Destination { override val route = "edit_profile" }
    data object ChangePassword : Destination { override val route = "change_password" }
    data object Help : Destination { override val route = "help" }
    data object ConsumedItems : Destination { override val route = "consumed_items" }
}
