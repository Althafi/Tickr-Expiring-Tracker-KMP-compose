package com.project.tickr.presentation.common

sealed interface UiText {
    data class Raw(val value: String) : UiText
}

fun String.toUiText(): UiText = UiText.Raw(this)
