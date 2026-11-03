package com.project.tickr.ui.i18n

import androidx.compose.runtime.Composable
import com.project.tickr.domain.model.AppLanguage

/** Bahasa yang punya terjemahan lengkap. Indonesia = `values/` (default), English = `values-en/`. */
private val SUPPORTED_LANGUAGES = setOf("id", "en")

/** Kode bahasa Java lama `in` = Indonesia. Region/script dibuang: `en-US` → `en`. */
internal fun normalizeLanguageCode(code: String): String {
    val base = code.lowercase().substringBefore('-').substringBefore('_')
    return if (base == "in") "id" else base
}

/**
 * Menentukan bahasa efektif yang dipakai app.
 * - Pilihan eksplisit (Indonesia/English) selalu dihormati.
 * - "Sistem": ikuti bahasa perangkat; jika tidak didukung (mis. Prancis) jatuh ke English.
 */
internal fun resolveLanguageTag(language: AppLanguage, deviceLanguage: String): String {
    language.tag?.let { return it }
    val device = normalizeLanguageCode(deviceLanguage)
    return if (device in SUPPORTED_LANGUAGES) device else "en"
}

/** Bahasa perangkat yang asli (tidak terpengaruh override app). */
internal expect fun deviceLanguageCode(): String

/** Menerapkan locale proses ini di level platform. Dipakai juga oleh kode non-Compose (notifikasi). */
internal expect fun applyPlatformLocale(tag: String)

/** Menentukan bahasa efektif dari pilihan pengguna lalu menerapkannya; mengembalikan tag-nya. */
fun applyAppLanguage(language: AppLanguage): String =
    resolveLanguageTag(language, deviceLanguageCode()).also { applyPlatformLocale(it) }

/**
 * Memasang bahasa pilihan pengguna untuk seluruh [content] tanpa restart.
 *
 * Compose Resources membaca locale platform (bukan state yang bisa diamati), jadi bahasa diubah di level
 * platform lalu [content] dibuat ulang dengan `key(tag)`. Letakkan state yang harus bertahan
 * (NavController, tab terpilih) DI ATAS pemanggilan ini.
 */
@Composable
expect fun AppLocaleProvider(language: AppLanguage, content: @Composable () -> Unit)
