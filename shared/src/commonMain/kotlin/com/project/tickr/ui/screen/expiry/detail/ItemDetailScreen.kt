package com.project.tickr.ui.screen.expiry.detail

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.tickr.domain.model.Item
import com.project.tickr.domain.model.Urgency
import com.project.tickr.presentation.expiry.detail.ExpiryDetailAction
import com.project.tickr.presentation.expiry.detail.ExpiryDetailLoadState
import com.project.tickr.presentation.expiry.detail.ExpiryDetailUiState
import com.project.tickr.ui.screen.expiry.detail.components.DetailHero
import com.project.tickr.ui.screen.expiry.detail.components.DetailSkeletonContent
import com.project.tickr.ui.screen.expiry.detail.components.InfoBoxPair
import com.project.tickr.ui.screen.expiry.detail.components.StorageNoteBox
import com.project.tickr.ui.screen.expiry.detail.components.UrgencyBanner
import com.project.tickr.ui.theme.TickrCornerRadius
import com.project.tickr.ui.theme.TickrTheme

@Composable
fun ItemDetailScreen(
    state: ExpiryDetailUiState,
    onAction: (ExpiryDetailAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = TickrTheme.colors
    val spacing = TickrTheme.spacing
    val typography = TickrTheme.typography

    Box(modifier = modifier.fillMaxSize().background(colors.background)) {
        when (state.loadState) {

            ExpiryDetailLoadState.Loading -> {
                DetailSkeletonContent()
            }

            ExpiryDetailLoadState.Content -> {
                val item = state.item ?: return@Box

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                ) {
                    // ── Hero image ──────────────────────────────────────────────
                    DetailHero(
                        imageUrl = item.imageUrl,
                        productName = item.name,
                        onBack = { onAction(ExpiryDetailAction.Back) },
                        onDelete = { onAction(ExpiryDetailAction.Delete) },
                        modifier = Modifier.fillMaxWidth().height(280.dp),
                    )

                    // ── Content sheet (overlap hero, rounded-top) ────────────────
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = colors.surface,
                                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                            )
                            .padding(spacing.screen),
                    ) {
                        Spacer(Modifier.height(spacing.md))

                        // Urgency banner
                        UrgencyBanner(
                            urgency = state.urgency,
                            daysUntilExpiry = state.daysUntilExpiry,
                            expiryDate = item.expiryDate,
                        )

                        Spacer(Modifier.height(spacing.lg))

                        // Product name
                        Text(
                            text = item.name,
                            style = typography.greetingTitle,
                            color = colors.textPrimary,
                        )

                        Spacer(Modifier.height(spacing.lg))

                        // Info boxes: kategori + jumlah
                        InfoBoxPair(
                            categoryName = state.categoryName,
                            quantity = item.quantity,
                            unit = item.unit,
                        )

                        Spacer(Modifier.height(spacing.md))

                        // Storage note (hidden bila kosong)
                        StorageNoteBox(note = item.notes)

                        Spacer(Modifier.height(spacing.xxl))

                        // CTA
                        val ctaScale by animateFloatAsState(
                            targetValue = if (state.isProcessing) 0.97f else 1f,
                            label = "ctaScale",
                        )
                        Button(
                            onClick = { onAction(ExpiryDetailAction.MarkConsumed) },
                            enabled = !state.isProcessing,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .scale(ctaScale)
                                .navigationBarsPadding()
                                .imePadding(),
                            shape = RoundedCornerShape(TickrCornerRadius.button),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.primaryBrand,
                                disabledContainerColor = colors.primaryBrand.copy(alpha = 0.55f),
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                        ) {
                            if (state.isProcessing) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(20.dp),
                                )
                            } else {
                                Text(
                                    text = "Tandai Sudah Habis Dikonsumsi", // TODO(user): stringResource detail_cta_consume
                                    style = typography.productName,
                                    color = Color.White,
                                )
                            }
                        }

                        Spacer(Modifier.height(spacing.lg))
                    }
                }
            }

            is ExpiryDetailLoadState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(spacing.screen),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = state.loadState.message,
                        style = typography.body,
                        color = colors.critical,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(spacing.lg))
                    Button(
                        onClick = { onAction(ExpiryDetailAction.Back) },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primaryBrand),
                        shape = RoundedCornerShape(TickrCornerRadius.button),
                    ) {
                        Text("Kembali", color = Color.White)
                    }
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }

    // ── Delete confirmation dialog ─────────────────────────────────────────────
    if (state.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { onAction(ExpiryDetailAction.DismissDeleteDialog) },
            title = {
                Text(
                    text = "Hapus barang ini?", // TODO(user): stringResource detail_delete_confirm_title
                    style = TickrTheme.typography.sectionTitle,
                )
            },
            text = {
                Text(
                    text = "Tindakan ini tidak dapat dibatalkan.", // TODO(user): stringResource detail_delete_confirm_message
                    style = TickrTheme.typography.body,
                    color = TickrTheme.colors.textSecondary,
                )
            },
            confirmButton = {
                Button(
                    onClick = { onAction(ExpiryDetailAction.ConfirmDelete) },
                    colors = ButtonDefaults.buttonColors(containerColor = TickrTheme.colors.critical),
                ) {
                    Text("Hapus", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { onAction(ExpiryDetailAction.DismissDeleteDialog) }) {
                    Text("Batal", color = TickrTheme.colors.textSecondary)
                }
            },
            containerColor = TickrTheme.colors.surface,
        )
    }
}

// ─── Previews ─────────────────────────────────────────────────────────────────

private val sampleItem = Item(
    id = 1, userId = "u1", categoryId = 1,
    name = "Susu UHT Full Cream", barcode = null,
    expiryDate = "2026-06-11", imageUrl = null,
    notes = "Simpan di kulkas bagian bawah. Konsumsi sebelum tanggal tertera.",
    isConsumed = false, quantity = 2, unit = "Karton",
    createdAt = "", updatedAt = "",
)

@Preview(name = "Detail · Critical (Light)", showBackground = true, backgroundColor = 0xFFFAFAFA)
@Composable
private fun DetailCriticalPreview() {
    TickrTheme {
        ItemDetailScreen(
            state = ExpiryDetailUiState(
                loadState = ExpiryDetailLoadState.Content,
                item = sampleItem,
                categoryName = "Makanan & Minuman",
                daysUntilExpiry = 0,
                urgency = Urgency.CRITICAL,
            ),
            onAction = {},
        )
    }
}

@Preview(name = "Detail · Warning (Light)", showBackground = true, backgroundColor = 0xFFFAFAFA)
@Composable
private fun DetailWarningPreview() {
    TickrTheme {
        ItemDetailScreen(
            state = ExpiryDetailUiState(
                loadState = ExpiryDetailLoadState.Content,
                item = sampleItem.copy(expiryDate = "2026-06-16"),
                categoryName = "Makanan & Minuman",
                daysUntilExpiry = 5,
                urgency = Urgency.WARNING,
            ),
            onAction = {},
        )
    }
}

@Preview(name = "Detail · Safe (Light)", showBackground = true, backgroundColor = 0xFFFAFAFA)
@Composable
private fun DetailSafePreview() {
    TickrTheme {
        ItemDetailScreen(
            state = ExpiryDetailUiState(
                loadState = ExpiryDetailLoadState.Content,
                item = sampleItem.copy(expiryDate = "2026-07-20"),
                categoryName = "Makanan & Minuman",
                daysUntilExpiry = 40,
                urgency = Urgency.SAFE,
            ),
            onAction = {},
        )
    }
}

@Preview(name = "Detail · Critical (Dark)", showBackground = true, backgroundColor = 0xFF101713)
@Composable
private fun DetailCriticalDarkPreview() {
    TickrTheme(darkTheme = true) {
        ItemDetailScreen(
            state = ExpiryDetailUiState(
                loadState = ExpiryDetailLoadState.Content,
                item = sampleItem,
                categoryName = "Makanan & Minuman",
                daysUntilExpiry = 0,
                urgency = Urgency.CRITICAL,
            ),
            onAction = {},
        )
    }
}

@Preview(name = "Detail · Skeleton (Light)", showBackground = true, backgroundColor = 0xFFFAFAFA)
@Composable
private fun DetailSkeletonPreview() {
    TickrTheme {
        ItemDetailScreen(
            state = ExpiryDetailUiState(loadState = ExpiryDetailLoadState.Loading),
            onAction = {},
        )
    }
}
