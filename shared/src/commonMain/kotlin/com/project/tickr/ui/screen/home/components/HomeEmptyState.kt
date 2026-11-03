package com.project.tickr.ui.screen.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.tickr.ui.theme.TickrCornerRadius
import com.project.tickr.ui.theme.TickrTheme
import tickrapp.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun HomeEmptyState(
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = TickrTheme.colors
    val typography = TickrTheme.typography
    val spacing = TickrTheme.spacing

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Placeholder ilustrasi — TODO(user): ganti dengan ill_empty_items drawable
        Box(
            modifier = Modifier
                .size(180.dp)
                .background(
                    color = colors.primaryBrand.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(TickrCornerRadius.card),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "📦", // TODO(user): ganti aset final ill_empty_items
                style = typography.sectionTitle,
            )
        }

        Spacer(Modifier.height(spacing.lg))

        Text(
            text = stringResource(Res.string.home_empty_title),
            style = typography.sectionTitle,
            color = colors.textPrimary,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(spacing.sm))

        Text(
            text = stringResource(Res.string.home_empty_body),
            style = typography.body,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = spacing.xl),
        )

        Spacer(Modifier.height(spacing.xl))

        Button(
            onClick = onAddClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(TickrCornerRadius.button),
            colors = ButtonDefaults.buttonColors(containerColor = colors.primaryBrand),
        ) {
            Text(
                text = stringResource(Res.string.home_empty_cta),
                style = typography.body,
                color = TickrTheme.colors.onBrand,
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFAFAFA)
@Composable
private fun HomeEmptyStatePreview() {
    TickrTheme {
        HomeEmptyState(onAddClick = {}, modifier = Modifier.padding(16.dp))
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF101713)
@Composable
private fun HomeEmptyStateDarkPreview() {
    TickrTheme(darkTheme = true) {
        HomeEmptyState(onAddClick = {}, modifier = Modifier.padding(16.dp))
    }
}
