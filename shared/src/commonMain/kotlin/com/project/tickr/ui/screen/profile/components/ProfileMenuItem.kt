package com.project.tickr.ui.screen.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.project.tickr.ui.theme.TickrCornerRadius
import com.project.tickr.ui.theme.TickrTheme

@Composable
fun ProfileMenuItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = TickrTheme.colors
    val spacing = TickrTheme.spacing

    val itemModifier = if (enabled) {
        modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    } else {
        modifier
            .fillMaxWidth()
            .alpha(0.45f)
            .semantics { disabled() }
    }

    Row(
        modifier = itemModifier.padding(vertical = spacing.sm, horizontal = spacing.lg),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Leading icon with soft circular background
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(colors.primaryBrand.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.primaryBrand,
                modifier = Modifier.size(20.dp),
            )
        }

        Spacer(Modifier.width(spacing.md))

        Text(
            text = label,
            style = TickrTheme.typography.body,
            color = colors.textPrimary,
            modifier = Modifier.weight(1f),
        )

        if (!enabled) {
            // "Segera" badge
            Surface(
                shape = RoundedCornerShape(TickrCornerRadius.pill),
                color = colors.primaryBrand.copy(alpha = 0.12f),
                modifier = Modifier.semantics {
                    // TODO(user): sesuaikan contentDescription cd_profile_coming_soon
                },
            ) {
                Text(
                    text = "Segera", // TODO(user): gunakan string resource profile_coming_soon_badge
                    style = TickrTheme.typography.countdown,
                    color = colors.primaryBrand,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }
        } else {
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = colors.textSecondary,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}
