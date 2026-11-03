package com.project.tickr.ui.screen.home.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.project.tickr.ui.theme.TickrTheme
import tickrapp.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun GreetingHeader(
    modifier: Modifier = Modifier,
) {
    val colors = TickrTheme.colors
    val typography = TickrTheme.typography

    Column(modifier = modifier) {
        Text(
            text = stringResource(Res.string.home_greeting),
            style = typography.greetingTitle,
            color = colors.textPrimary,
        )
        Spacer(Modifier.height(TickrTheme.spacing.xs))
        Text(
            text = stringResource(Res.string.home_subtitle),
            style = typography.body,
            color = colors.textSecondary,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFAFAFA)
@Composable
private fun GreetingHeaderPreview() {
    TickrTheme {
        GreetingHeader()
    }
}
