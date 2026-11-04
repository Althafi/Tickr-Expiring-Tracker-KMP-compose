package com.project.tickr.ui.screen.notifications

import com.project.tickr.ui.common.TickrIcons
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.project.tickr.domain.model.NotificationEntry
import com.project.tickr.presentation.notifications.NotificationsAction
import com.project.tickr.presentation.notifications.NotificationsUiState
import com.project.tickr.ui.common.rememberProductImageModel
import com.project.tickr.ui.i18n.formatShortDate
import com.project.tickr.ui.theme.TickrCornerRadius
import com.project.tickr.ui.theme.TickrTheme
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import tickrapp.shared.generated.resources.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    state: NotificationsUiState,
    onAction: (NotificationsAction) -> Unit,
    onOpenSettings: () -> Unit,
) {
    val colors = TickrTheme.colors
    val spacing = TickrTheme.spacing

    Scaffold(
        containerColor = colors.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(Res.string.notifications_title),
                        style = TickrTheme.typography.sectionTitle,
                        color = colors.textPrimary,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onAction(NotificationsAction.Back) }) {
                        Icon(
                            TickrIcons.ArrowBackMirrored,
                            contentDescription = stringResource(Res.string.cd_back),
                            tint = colors.textPrimary,
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = colors.background),
            )
        },
    ) { innerPadding ->
        Column(Modifier.fillMaxSize().padding(innerPadding)) {
            if (!state.permissionGranted) {
                PermissionBanner(
                    onOpenSettings = onOpenSettings,
                    modifier = Modifier.padding(horizontal = spacing.screen, vertical = spacing.sm),
                )
            }
            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colors.primaryBrand)
                }
                state.entries.isEmpty() -> EmptyState()
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = spacing.screen, vertical = spacing.sm,
                    ),
                    verticalArrangement = Arrangement.spacedBy(spacing.sm),
                ) {
                    items(state.entries, key = { it.id }) { entry ->
                        EntryRow(entry = entry, onClick = { onAction(NotificationsAction.OpenItem(entry.itemId)) })
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionBanner(onOpenSettings: () -> Unit, modifier: Modifier = Modifier) {
    val colors = TickrTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(TickrCornerRadius.button))
            .background(colors.warningContainer)
            .padding(TickrTheme.spacing.cardInner),
    ) {
        Text(
            text = stringResource(Res.string.notif_permission_off),
            style = TickrTheme.typography.body,
            color = colors.warning,
        )
        TextButton(onClick = onOpenSettings) {
            Text(
                text = stringResource(Res.string.notif_permission_action),
                style = TickrTheme.typography.body,
                color = colors.warning,
            )
        }
    }
}

@Composable
private fun EmptyState() {
    val colors = TickrTheme.colors
    Column(
        modifier = Modifier.fillMaxSize().padding(TickrTheme.spacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            TickrIcons.Notifications,
            contentDescription = null,
            tint = colors.textSecondary,
            modifier = Modifier.size(48.dp),
        )
        Spacer(Modifier.height(TickrTheme.spacing.md))
        Text(
            text = stringResource(Res.string.notifications_empty_title),
            style = TickrTheme.typography.sectionTitle,
            color = colors.textPrimary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(TickrTheme.spacing.xs))
        Text(
            text = stringResource(Res.string.notifications_empty_body),
            style = TickrTheme.typography.body,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun EntryRow(entry: NotificationEntry, onClick: () -> Unit) {
    val colors = TickrTheme.colors
    val typography = TickrTheme.typography
    val spacing = TickrTheme.spacing
    val critical = entry.daysBefore <= 3

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(TickrCornerRadius.card))
            .background(colors.surface)
            .border(1.dp, colors.textSecondary.copy(alpha = 0.10f), RoundedCornerShape(TickrCornerRadius.card))
            .clickable(onClick = onClick)
            .padding(spacing.cardInner),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(TickrCornerRadius.thumbnail))
                .background(if (critical) colors.criticalContainer else colors.warningContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                TickrIcons.Notifications,
                contentDescription = null,
                tint = if (critical) colors.critical else colors.warning,
                modifier = Modifier.size(22.dp),
            )
            AsyncImage(
                model = rememberProductImageModel(entry.imageUrl),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(TickrCornerRadius.thumbnail)),
            )
        }
        Column(Modifier.weight(1f)) {
            Text(
                text = entry.itemName,
                style = typography.productName,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = if (entry.daysBefore == 0) stringResource(Res.string.notif_entry_today)
                else pluralStringResource(Res.plurals.notif_entry_days, entry.daysBefore, entry.daysBefore),
                style = typography.caption,
                color = if (critical) colors.critical else colors.warning,
            )
        }
        Text(
            text = formatShortDate(entry.scheduledDate),
            style = typography.caption,
            color = colors.textSecondary,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NotificationsScreenPreview() {
    TickrTheme {
        NotificationsScreen(
            state = NotificationsUiState(
                isLoading = false,
                permissionGranted = false,
                entries = listOf(
                    NotificationEntry(1, 1, "Susu UHT", null, 0, "2026-06-11"),
                    NotificationEntry(2, 2, "Yogurt Strawberry", null, 3, "2026-06-12"),
                ),
            ),
            onAction = {},
            onOpenSettings = {},
        )
    }
}
