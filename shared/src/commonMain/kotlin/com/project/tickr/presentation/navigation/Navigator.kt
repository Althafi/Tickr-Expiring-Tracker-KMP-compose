package com.project.tickr.presentation.navigation

interface Navigator {
    fun navigate(destination: Destination, popUpToInclusive: Boolean = false)
    fun back()
}
