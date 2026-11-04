package com.project.tickr.ui.screen.home.components

import com.project.tickr.ui.common.TickrIcons
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.tickr.ui.theme.TickrTheme
import tickrapp.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import com.project.tickr.core.util.DateParts
import com.project.tickr.ui.i18n.formatLongDate

@Composable
fun TickrTopBar(
    nowEpochMillis: Long,
    date: DateParts?,
    displayTime: String,
    onNotificationClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = TickrTheme.colors
    val typography = TickrTheme.typography
    val spacing = TickrTheme.spacing
    val notificationsDescription = stringResource(Res.string.cd_home_notification)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.screen, vertical = spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Kiri: wordmark + tanggal + jam
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.sm),
            ) {
                Text(
                    text = stringResource(Res.string.app_name), // TODO(user): ganti dengan Image logo jika tersedia
                    style = typography.greetingTitle,
                    color = colors.primaryBrand,
                )
                Column {
                    Text(
                        text = date?.let { formatLongDate(it) } ?: "",
                        style = typography.caption,
                        color = colors.textSecondary,
                    )
                    Text(
                        text = displayTime,
                        style = typography.displayClock,
                        color = colors.primaryBrand,
                    )
                }
            }
        }

        // Kanan: bell notifikasi
        IconButton(
            onClick = onNotificationClick,
            modifier = Modifier
                .size(44.dp)
                .semantics { contentDescription = notificationsDescription },
        ) {
            Icon(
                TickrIcons.Notifications,
                contentDescription = null,
                tint = colors.textPrimary,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFAFAFA)
@Composable
private fun TickrTopBarPreview() {
    TickrTheme {
        TickrTopBar(
            nowEpochMillis = 1749470828000L,
            date = DateParts(3, 10, 6, 2026),
            displayTime = "18:27:08",
            onNotificationClick = {},
        )
    }
}
