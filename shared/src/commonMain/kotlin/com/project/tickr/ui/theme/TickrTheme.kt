package com.project.tickr.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

val LocalTickrColors = staticCompositionLocalOf<TickrColors> { TickrLightColors }
val LocalTickrSpacing = staticCompositionLocalOf<TickrSpacing> { TickrSpacing() }
val LocalTickrTypography = staticCompositionLocalOf<TickrTypography> { tickrTypography() }

@Composable
fun TickrTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) TickrDarkColors else TickrLightColors

    CompositionLocalProvider(
        LocalTickrColors provides colors,
        LocalTickrSpacing provides TickrSpacing(),
        LocalTickrTypography provides tickrTypography(),
    ) {
        MaterialTheme(
            colorScheme = colors.toMaterial3ColorScheme(darkTheme),
            shapes = TickrShapes,
            typography = Material3Typography,
            content = content,
        )
    }
}

object TickrTheme {
    val colors: TickrColors
        @Composable
        get() = LocalTickrColors.current

    val spacing: TickrSpacing
        @Composable
        get() = LocalTickrSpacing.current

    val typography: TickrTypography
        @Composable
        get() = LocalTickrTypography.current
}
