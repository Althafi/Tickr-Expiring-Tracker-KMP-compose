package com.project.tickr.ui.screen.auth.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import com.project.tickr.ui.theme.TickrCornerRadius
import com.project.tickr.ui.theme.TickrTheme

@Composable
fun GoogleButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(TickrCornerRadius.button),
        border = BorderStroke(
            width = 1.dp,
            color = TickrTheme.colors.textSecondary.copy(alpha = 0.35f),
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = TickrTheme.colors.surface,
            contentColor = TickrTheme.colors.textPrimary,
        ),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // TODO(user): ganti Box ini dengan ic_google drawable final
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(TickrTheme.colors.primaryBrand.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "G",
                    style = TickrTheme.typography.countdown,
                    color = TickrTheme.colors.primaryBrand,
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                style = TickrTheme.typography.productName,
                color = TickrTheme.colors.textPrimary,
            )
        }
    }
}
