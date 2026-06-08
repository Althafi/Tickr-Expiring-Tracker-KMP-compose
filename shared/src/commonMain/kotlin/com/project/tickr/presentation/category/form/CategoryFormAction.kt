package com.project.tickr.presentation.category.form

import com.project.tickr.presentation.common.mvi.UiAction

sealed interface CategoryFormAction : UiAction {
    data class NameChanged(val value: String) : CategoryFormAction
    data class IconNameChanged(val value: String) : CategoryFormAction
    data class ColorHexChanged(val value: String) : CategoryFormAction
    data object Submit : CategoryFormAction
}
