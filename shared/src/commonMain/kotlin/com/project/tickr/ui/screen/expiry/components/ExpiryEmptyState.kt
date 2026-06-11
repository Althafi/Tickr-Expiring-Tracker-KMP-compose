package com.project.tickr.ui.screen.expiry.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ShoppingBasket
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.tickr.ui.theme.TickrCornerRadius
import com.project.tickr.ui.theme.TickrTheme

@Composable
fun ExpiryEmptyState(
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = TickrTheme.colors
    val typography = TickrTheme.typography
    val spacing = TickrTheme.spacing

    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = spacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(96.dp),
            contentAlignment = Alignment.Center,
        ) {
            // Placeholder ilustrasi — TODO(user): ganti dengan ill_empty_expiry
            Icon(
                Icons.Outlined.ShoppingBasket,
                contentDescription = null,
                tint = colors.primaryBrand.copy(alpha = 0.35f),
                modifier = Modifier.size(64.dp),
            )
        }
        Spacer(Modifier.height(spacing.lg))
        Text(
            text = "Belum ada barang", // TODO(user): stringResource expiry_empty_title
            style = typography.sectionTitle,
            color = colors.textPrimary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(spacing.sm))
        Text(
            text = "Tambahkan barang pertamamu untuk mulai memantau tanggal kedaluwarsa.", // TODO(user): stringResource expiry_empty_subtitle
            style = typography.body,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
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
                text = "Tambah Barang", // TODO(user): stringResource expiry_empty_cta
                style = typography.productName,
                color = androidx.compose.ui.graphics.Color.White,
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFAFAFA)
@Composable
private fun ExpiryEmptyStatePreview() {
    TickrTheme {
        ExpiryEmptyState(onAddClick = {}, modifier = Modifier.padding(20.dp))
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF101713)
@Composable
private fun ExpiryEmptyStateDarkPreview() {
    TickrTheme(darkTheme = true) {
        ExpiryEmptyState(onAddClick = {}, modifier = Modifier.padding(20.dp))
    }
}
