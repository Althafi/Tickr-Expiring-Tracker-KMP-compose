package com.project.tickr.ui.screen.help

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Help
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.tickr.presentation.help.FaqItem
import com.project.tickr.presentation.help.HelpAction
import com.project.tickr.presentation.help.HelpUiState
import com.project.tickr.ui.theme.TickrCornerRadius
import com.project.tickr.ui.theme.TickrTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(
    state: HelpUiState,
    onAction: (HelpAction) -> Unit,
) {
    val colors = TickrTheme.colors
    val spacing = TickrTheme.spacing

    Scaffold(
        containerColor = colors.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Bantuan", // TODO(user): string resource help_title
                        style = TickrTheme.typography.productName,
                        color = colors.primaryBrand,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onAction(HelpAction.Back) }) {
                        Icon(
                            Icons.Outlined.ArrowBack,
                            contentDescription = "Kembali", // TODO(user): cd_back
                            tint = colors.textPrimary,
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = colors.surface,
                    scrolledContainerColor = colors.surface,
                ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(spacing.screen),
        ) {
            // Hero
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = spacing.lg),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(colors.primaryBrand.copy(alpha = 0.10f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Outlined.Help,
                        contentDescription = null,
                        tint = colors.primaryBrand,
                        modifier = Modifier.size(22.dp),
                    )
                }
                Spacer(Modifier.size(spacing.md))
                Text(
                    text = "Temukan jawaban cepat atau hubungi tim kami bila butuh bantuan lebih lanjut.", // TODO(user): string resource help_subtitle
                    style = TickrTheme.typography.body,
                    color = colors.textSecondary,
                )
            }

            // FAQ section
            Text(
                text = "Pertanyaan Umum", // TODO(user): string resource help_section_faq
                style = TickrTheme.typography.sectionTitle,
                color = colors.textPrimary,
                modifier = Modifier.padding(bottom = spacing.sm),
            )

            Surface(
                shape = RoundedCornerShape(TickrCornerRadius.card),
                color = colors.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        colors.textSecondary.copy(alpha = 0.15f),
                        RoundedCornerShape(TickrCornerRadius.card),
                    ),
            ) {
                Column {
                    state.faqs.forEachIndexed { index, faq ->
                        FaqExpandableItem(
                            faq = faq,
                            isExpanded = state.expandedId == faq.id,
                            onToggle = { onAction(HelpAction.ToggleFaq(faq.id)) },
                        )
                        if (index < state.faqs.lastIndex) {
                            Spacer(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .padding(horizontal = 16.dp)
                                    .background(colors.textSecondary.copy(alpha = 0.10f)),
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(spacing.xxl))

            // Contact section
            Text(
                text = "Hubungi Kami", // TODO(user): string resource help_section_contact
                style = TickrTheme.typography.sectionTitle,
                color = colors.textPrimary,
                modifier = Modifier.padding(bottom = spacing.sm),
            )
            HelpContactRow(
                icon = Icons.Outlined.Email,
                label = "Email Dukungan", // TODO(user): string resource help_contact_email
                onClick = { onAction(HelpAction.ContactEmail) },
            )
            Spacer(Modifier.height(spacing.sm))
            HelpContactRow(
                icon = Icons.Outlined.Send,
                label = "WhatsApp", // TODO(user): string resource help_contact_whatsapp
                onClick = { onAction(HelpAction.ContactWhatsApp) },
            )

            Spacer(Modifier.height(spacing.xxl))

            Text(
                text = "Versi 1.0.4 • © 2024 Tickr Team", // TODO(user): gunakan string resource app_version_copyright
                style = TickrTheme.typography.caption,
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(spacing.xl))
        }
    }
}

@Composable
private fun FaqExpandableItem(
    faq: FaqItem,
    isExpanded: Boolean,
    onToggle: () -> Unit,
) {
    val colors = TickrTheme.colors
    val spacing = TickrTheme.spacing
    val chevronRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(200),
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(spacing.cardInner)
            .animateContentSize()
            .semantics { contentDescription = "Buka atau tutup jawaban" }, // TODO(user): cd_help_faq_toggle
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = faq.question,
                style = TickrTheme.typography.body,
                color = colors.textPrimary,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.size(spacing.sm))
            Icon(
                Icons.Outlined.ExpandMore,
                contentDescription = null,
                tint = colors.textSecondary,
                modifier = Modifier.size(20.dp).rotate(chevronRotation),
            )
        }
        if (isExpanded) {
            Spacer(Modifier.height(spacing.sm))
            Text(
                text = faq.answer,
                style = TickrTheme.typography.body,
                color = colors.textSecondary,
            )
        }
    }
}

@Composable
private fun HelpContactRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    val colors = TickrTheme.colors
    val spacing = TickrTheme.spacing

    Surface(
        shape = RoundedCornerShape(TickrCornerRadius.card),
        color = colors.surface,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, colors.textSecondary.copy(alpha = 0.15f), RoundedCornerShape(TickrCornerRadius.card))
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(spacing.cardInner),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(colors.primaryBrand.copy(alpha = 0.08f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = colors.primaryBrand, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.size(spacing.md))
            Text(label, style = TickrTheme.typography.body, color = colors.textPrimary)
        }
    }
}

@Preview(name = "Help — Light", showBackground = true)
@Composable
private fun HelpScreenPreviewLight() {
    TickrTheme(darkTheme = false) {
        HelpScreen(
            state = HelpUiState(
                faqs = listOf(
                    FaqItem("1", "Bagaimana cara menambahkan barang baru?", "Tekan tombol tambah (+) di bilah bawah..."),
                    FaqItem("2", "Kapan saya menerima pengingat?", "Tickr mengirim notifikasi bertahap..."),
                ),
                expandedId = "1",
            ),
            onAction = {},
        )
    }
}

@Preview(name = "Help — Dark", showBackground = true, backgroundColor = 0xFF101713)
@Composable
private fun HelpScreenPreviewDark() {
    TickrTheme(darkTheme = true) {
        HelpScreen(
            state = HelpUiState(
                faqs = listOf(FaqItem("1", "Bagaimana cara menambahkan barang baru?", "Tekan tombol tambah...")),
            ),
            onAction = {},
        )
    }
}
