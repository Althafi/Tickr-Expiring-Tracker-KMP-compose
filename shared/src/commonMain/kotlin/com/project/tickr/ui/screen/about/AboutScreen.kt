package com.project.tickr.ui.screen.about

import com.project.tickr.ui.common.TickrIcons
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.tickr.domain.model.AppInfo
import com.project.tickr.ui.theme.TickrCornerRadius
import com.project.tickr.ui.theme.TickrTheme
import tickrapp.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    info: AppInfo,
    onBack: () -> Unit,
) {
    val colors = TickrTheme.colors
    val spacing = TickrTheme.spacing

    Scaffold(
        containerColor = colors.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(Res.string.about_title),
                        style = TickrTheme.typography.productName,
                        color = colors.primaryBrand,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            TickrIcons.ArrowBackMirrored,
                            contentDescription = stringResource(Res.string.cd_back),
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(spacing.md),
        ) {
            Spacer(Modifier.height(spacing.lg))
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(colors.primaryBrand.copy(alpha = 0.10f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(TickrIcons.Info, contentDescription = null, tint = colors.primaryBrand, modifier = Modifier.size(36.dp))
            }
            Text(text = info.name, style = TickrTheme.typography.greetingTitle, color = colors.textPrimary)
            Text(
                text = stringResource(Res.string.about_version, info.versionName, info.buildNumber),
                style = TickrTheme.typography.caption,
                color = colors.textSecondary,
            )
            Spacer(Modifier.height(spacing.sm))
            Text(
                text = stringResource(Res.string.about_description),
                style = TickrTheme.typography.body,
                color = colors.textPrimary,
                textAlign = TextAlign.Center,
            )
            Surface(
                shape = RoundedCornerShape(TickrCornerRadius.card),
                color = colors.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.textSecondary.copy(alpha = 0.15f), RoundedCornerShape(TickrCornerRadius.card)),
            ) {
                Text(
                    text = stringResource(Res.string.about_privacy),
                    style = TickrTheme.typography.body,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(spacing.cardInner),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AboutScreenPreview() {
    TickrTheme { AboutScreen(info = AppInfo("Tickr", "1.0", "1"), onBack = {}) }
}
