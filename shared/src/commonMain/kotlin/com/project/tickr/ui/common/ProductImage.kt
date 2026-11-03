package com.project.tickr.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import com.project.tickr.domain.storage.ImageStorage

/**
 * Satu-satunya tempat path foto produk (relatif) diubah menjadi model yang bisa dimuat Coil.
 * Default-nya identitas supaya `@Preview` tetap jalan tanpa Koin.
 */
val LocalProductImageResolver: ProvidableCompositionLocal<(String) -> String> =
    staticCompositionLocalOf { { it } }

@Composable
fun ProvideProductImageResolver(storage: ImageStorage, content: @Composable () -> Unit) {
    val resolver = remember(storage) { { path: String -> storage.resolve(path).toImageModel() } }
    CompositionLocalProvider(LocalProductImageResolver provides resolver, content = content)
}

/** Model untuk `AsyncImage`: `null` bila barang tidak punya foto. */
@Composable
fun rememberProductImageModel(path: String?): String? {
    val resolver = LocalProductImageResolver.current
    return remember(path, resolver) { path?.takeIf { it.isNotBlank() }?.let(resolver) }
}

// Path absolut perangkat → URI file agar Coil memperlakukannya sebagai file, bukan string biasa.
private fun String.toImageModel(): String = if (startsWith("/")) "file://$this" else this
