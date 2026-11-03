package com.project.tickr.ui.screen.expiry.detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.tickr.domain.model.Urgency
import com.project.tickr.ui.theme.TickrCornerRadius
import com.project.tickr.ui.theme.TickrTheme
import tickrapp.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.pluralStringResource
import com.project.tickr.ui.i18n.countdownText
import com.project.tickr.ui.i18n.formatShortDate

@Composable
fun UrgencyBanner(
    urgency: Urgency,
    daysUntilExpiry: Int,
    expiryDate: String,
    modifier: Modifier = Modifier,
) {
    val colors = TickrTheme.colors
    val typography = TickrTheme.typography

    val dateText = formatShortDate(expiryDate)
    val (bg, textColor, icon, bannerText) = when (urgency) {
        Urgency.CRITICAL -> BannerVariant(
            bg = colors.criticalContainer,
            textColor = colors.critical,
            icon = Icons.Outlined.Error,
            text = if (daysUntilExpiry <= 0)
                stringResource(Res.string.banner_critical_today, dateText)
            else
                pluralStringResource(Res.plurals.banner_critical_days, daysUntilExpiry, daysUntilExpiry, dateText),
        )
        Urgency.WARNING -> BannerVariant(
            bg = colors.warningContainer,
            textColor = colors.warning,
            icon = Icons.Outlined.Warning,
            text = stringResource(Res.string.banner_warning, countdownText(daysUntilExpiry), dateText),
        )
        Urgency.SAFE -> BannerVariant(
            bg = colors.safeContainer,
            textColor = colors.safe,
            icon = Icons.Outlined.CheckCircle,
            text = stringResource(Res.string.banner_safe, countdownText(daysUntilExpiry), dateText),
        )
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(TickrCornerRadius.button))
            .background(bg)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .semantics { contentDescription = bannerText }, // announce status for a11y
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = textColor, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Text(text = bannerText, style = typography.body, color = textColor)
    }
}

private data class BannerVariant(
    val bg: androidx.compose.ui.graphics.Color,
    val textColor: androidx.compose.ui.graphics.Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val text: String,
)

@Preview(showBackground = true, backgroundColor = 0xFFFAFAFA)
@Composable
private fun UrgencyBannerCriticalPreview() {
    TickrTheme {
        UrgencyBanner(urgency = Urgency.CRITICAL, daysUntilExpiry = 0, expiryDate = "2026-06-11", modifier = Modifier.padding(16.dp))
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFAFAFA)
@Composable
private fun UrgencyBannerWarningPreview() {
    TickrTheme {
        UrgencyBanner(urgency = Urgency.WARNING, daysUntilExpiry = 5, expiryDate = "2026-06-16", modifier = Modifier.padding(16.dp))
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFAFAFA)
@Composable
private fun UrgencyBannerSafePreview() {
    TickrTheme {
        UrgencyBanner(urgency = Urgency.SAFE, daysUntilExpiry = 40, expiryDate = "2026-07-21", modifier = Modifier.padding(16.dp))
    }
}
