package com.project.tickr.presentation.common

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.PluralStringResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getPluralString
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/**
 * Teks yang siap ditampilkan tapi belum diterjemahkan. ViewModel mengirim [UiText], bukan kalimat jadi,
 * supaya bahasa dipilih saat ditampilkan (dan ikut berganti seketika).
 */
sealed interface UiText {
    data class Raw(val value: String) : UiText
    data class Res(val res: StringResource, val args: List<Any> = emptyList()) : UiText
    data class Plural(val res: PluralStringResource, val quantity: Int, val args: List<Any> = emptyList()) : UiText
}

fun String.toUiText(): UiText = UiText.Raw(this)

fun StringResource.asUiText(vararg args: Any): UiText = UiText.Res(this, args.toList())

@Composable
fun UiText.asString(): String = when (this) {
    is UiText.Raw -> value
    is UiText.Res -> stringResource(res, *args.toTypedArray())
    is UiText.Plural -> pluralStringResource(res, quantity, *args.toTypedArray())
}

/** Untuk kode non-Compose (mis. notifikasi). Memakai locale platform yang sedang aktif. */
suspend fun UiText.resolve(): String = when (this) {
    is UiText.Raw -> value
    is UiText.Res -> getString(res, *args.toTypedArray())
    is UiText.Plural -> getPluralString(res, quantity, *args.toTypedArray())
}
