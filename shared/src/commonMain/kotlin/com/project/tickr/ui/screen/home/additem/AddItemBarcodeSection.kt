package com.project.tickr.ui.screen.home.additem

import com.project.tickr.ui.common.TickrIcons
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.project.tickr.presentation.additem.AddItemAction
import com.project.tickr.presentation.additem.AddItemNotice
import com.project.tickr.presentation.additem.AddItemUiState
import com.project.tickr.presentation.additem.NoticeKind
import com.project.tickr.presentation.common.asString
import com.project.tickr.ui.theme.TickrCornerRadius
import com.project.tickr.ui.theme.TickrTheme
import org.jetbrains.compose.resources.stringResource
import tickrapp.shared.generated.resources.*

/** Pesan singkat di dalam sheet (warna mengikuti jenisnya). Ketuk untuk menutup. */
@Composable
fun AddItemNoticeBanner(
    notice: AddItemNotice,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = TickrTheme.colors
    val typography = TickrTheme.typography
    val (container, content) = when (notice.kind) {
        NoticeKind.SUCCESS -> colors.safeContainer to colors.safe
        NoticeKind.WARNING, NoticeKind.OFFLINE -> colors.warningContainer to colors.warning
        NoticeKind.INFO -> colors.background to colors.textSecondary
    }
    val icon = when (notice.kind) {
        NoticeKind.SUCCESS -> TickrIcons.CheckCircle
        NoticeKind.WARNING -> TickrIcons.Warning
        NoticeKind.OFFLINE -> TickrIcons.WifiOff
        NoticeKind.INFO -> TickrIcons.Info
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(TickrCornerRadius.button))
            .background(container)
            .border(1.dp, content.copy(alpha = 0.35f), RoundedCornerShape(TickrCornerRadius.button))
            .clickable(onClick = onDismiss)
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .semantics { liveRegion = LiveRegionMode.Polite },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(20.dp))
        Text(
            text = notice.text.asString(),
            style = typography.body,
            color = colors.textPrimary,
            modifier = Modifier.weight(1f),
        )
        Icon(
            TickrIcons.Close,
            contentDescription = stringResource(Res.string.cd_close),
            tint = content,
            modifier = Modifier.size(16.dp),
        )
    }
}

/** Tombol scan barcode + barcode yang sedang terpasang. Fitur ini butuh internet, jadi ditandai jelas. */
@Composable
fun BarcodeScanSection(
    state: AddItemUiState,
    onAction: (AddItemAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = TickrTheme.colors
    val typography = TickrTheme.typography

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(TickrCornerRadius.button))
                .background(colors.primaryBrand.copy(alpha = 0.08f))
                .border(1.dp, colors.primaryBrand.copy(alpha = 0.4f), RoundedCornerShape(TickrCornerRadius.button))
                .clickable(enabled = !state.isLookingUp) { onAction(AddItemAction.ScanClicked) }
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(colors.primaryBrand),
                contentAlignment = Alignment.Center,
            ) {
                if (state.isLookingUp) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = colors.onBrand,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Icon(TickrIcons.QrCodeScanner, contentDescription = null, tint = colors.onBrand, modifier = Modifier.size(22.dp))
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(if (state.isLookingUp) Res.string.additem_scan_looking else Res.string.additem_scan),
                    style = typography.body,
                    color = colors.textPrimary,
                )
                Spacer(Modifier.size(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(TickrIcons.Wifi, contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = stringResource(Res.string.additem_scan_needs_internet),
                        style = typography.caption,
                        color = colors.textSecondary,
                    )
                }
            }
        }

        state.barcode?.let { code ->
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(TickrCornerRadius.pill))
                    .background(colors.background)
                    .border(1.dp, colors.textSecondary.copy(alpha = 0.25f), RoundedCornerShape(TickrCornerRadius.pill))
                    .padding(start = 12.dp, top = 4.dp, bottom = 4.dp, end = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(Res.string.additem_barcode_chip, code),
                    style = typography.caption,
                    color = colors.textPrimary,
                )
                Box(
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .size(28.dp)
                        .clip(CircleShape)
                        .clickable { onAction(AddItemAction.ClearBarcode) },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        TickrIcons.Close,
                        contentDescription = stringResource(Res.string.cd_clear_barcode),
                        tint = colors.textSecondary,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
        }
    }
}
