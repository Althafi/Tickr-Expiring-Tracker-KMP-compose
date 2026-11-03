package com.project.tickr.ui.screen.settings

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.tickr.domain.model.AppLanguage
import com.project.tickr.domain.model.ThemeMode
import com.project.tickr.presentation.settings.SettingsAction
import com.project.tickr.presentation.settings.SettingsUiState
import com.project.tickr.ui.theme.TickrCornerRadius
import com.project.tickr.ui.theme.TickrTheme
import tickrapp.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource

private enum class OpenSheet { NONE, THEME, LANGUAGE }

@Composable
private fun ThemeMode.label(): String = stringResource(
    when (this) {
        ThemeMode.SYSTEM -> Res.string.theme_system
        ThemeMode.LIGHT -> Res.string.theme_light
        ThemeMode.DARK -> Res.string.theme_dark
    }
)

@Composable
private fun AppLanguage.label(): String = stringResource(
    when (this) {
        AppLanguage.SYSTEM -> Res.string.language_system
        AppLanguage.INDONESIAN -> Res.string.language_indonesian
        AppLanguage.ENGLISH -> Res.string.language_english
    }
)

@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onAction: (SettingsAction) -> Unit,
) {
    val colors = TickrTheme.colors
    val spacing = TickrTheme.spacing
    var openSheet by rememberSaveable { mutableStateOf(OpenSheet.NONE) }
    var showImportConfirm by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = spacing.screen, vertical = spacing.lg),
        verticalArrangement = Arrangement.spacedBy(spacing.sm),
    ) {
        Text(
            text = stringResource(Res.string.settings_title),
            style = TickrTheme.typography.greetingTitle,
            color = colors.textPrimary,
        )
        Spacer(Modifier.height(spacing.md))

        SectionLabel(stringResource(Res.string.settings_section_display))
        SettingsCard {
            SettingsRow(
                title = stringResource(Res.string.settings_theme),
                value = state.themeMode.label(),
                onClick = { openSheet = OpenSheet.THEME },
            )
            HorizontalDivider(color = colors.textSecondary.copy(alpha = 0.12f))
            SettingsRow(
                title = stringResource(Res.string.settings_language),
                value = state.language.label(),
                onClick = { openSheet = OpenSheet.LANGUAGE },
            )
        }

        Spacer(Modifier.height(spacing.md))
        SectionLabel(stringResource(Res.string.settings_section_data))
        SettingsCard {
            SettingsRow(
                title = stringResource(Res.string.settings_export),
                subtitle = stringResource(Res.string.settings_export_desc),
                enabled = !state.isBackupBusy,
                busy = state.isBackupBusy,
                onClick = { onAction(SettingsAction.ExportData) },
            )
            HorizontalDivider(color = colors.textSecondary.copy(alpha = 0.12f))
            SettingsRow(
                title = stringResource(Res.string.settings_import),
                subtitle = stringResource(Res.string.settings_import_desc),
                enabled = !state.isBackupBusy,
                onClick = { showImportConfirm = true },
            )
        }

        Spacer(Modifier.height(spacing.md))
        SectionLabel(stringResource(Res.string.settings_section_help))
        SettingsCard {
            SettingsRow(title = stringResource(Res.string.settings_help), onClick = { onAction(SettingsAction.OpenHelp) })
            HorizontalDivider(color = colors.textSecondary.copy(alpha = 0.12f))
            SettingsRow(title = stringResource(Res.string.settings_about), onClick = { onAction(SettingsAction.OpenAbout) })
        }

        Spacer(Modifier.height(spacing.xxl))
        Text(
            text = stringResource(Res.string.app_version_footer, state.appVersion),
            style = TickrTheme.typography.caption,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        // FAB tab bar menonjol di atas bilah dan menutupi footer bila tak ada ruang tambahan.
        Spacer(Modifier.height(40.dp))
    }

    if (showImportConfirm) {
        AlertDialog(
            onDismissRequest = { showImportConfirm = false },
            title = { Text(stringResource(Res.string.import_confirm_title)) },
            text = { Text(stringResource(Res.string.import_confirm_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showImportConfirm = false
                    onAction(SettingsAction.ImportData)
                }) { Text(stringResource(Res.string.import_confirm_action), color = colors.critical) }
            },
            dismissButton = {
                TextButton(onClick = { showImportConfirm = false }) { Text(stringResource(Res.string.action_cancel)) }
            },
        )
    }

    when (openSheet) {
        OpenSheet.THEME -> OptionSheet(
            title = stringResource(Res.string.settings_theme),
            options = ThemeMode.entries.map { it to it.label() },
            selected = state.themeMode,
            onSelect = { onAction(SettingsAction.SelectTheme(it)) },
            onDismiss = { openSheet = OpenSheet.NONE },
        )
        OpenSheet.LANGUAGE -> OptionSheet(
            title = stringResource(Res.string.settings_language),
            options = AppLanguage.entries.map { it to it.label() },
            selected = state.language,
            onSelect = { onAction(SettingsAction.SelectLanguage(it)) },
            onDismiss = { openSheet = OpenSheet.NONE },
        )
        OpenSheet.NONE -> Unit
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = TickrTheme.typography.caption,
        color = TickrTheme.colors.textSecondary,
        modifier = Modifier.padding(start = TickrTheme.spacing.xs),
    )
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    val colors = TickrTheme.colors
    Surface(
        shape = RoundedCornerShape(TickrCornerRadius.card),
        color = colors.surface,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, colors.textSecondary.copy(alpha = 0.15f), RoundedCornerShape(TickrCornerRadius.card)),
    ) {
        Column { content() }
    }
}

@Composable
private fun SettingsRow(
    title: String,
    onClick: () -> Unit,
    value: String? = null,
    subtitle: String? = null,
    enabled: Boolean = true,
    busy: Boolean = false,
) {
    val colors = TickrTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = TickrTheme.spacing.cardInner, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = TickrTheme.typography.body,
                color = if (enabled) colors.textPrimary else colors.textSecondary,
            )
            if (subtitle != null) {
                Text(text = subtitle, style = TickrTheme.typography.caption, color = colors.textSecondary)
            }
        }
        if (value != null) {
            Text(text = value, style = TickrTheme.typography.body, color = colors.textSecondary)
        }
        if (busy) {
            CircularProgressIndicator(
                modifier = Modifier.padding(start = TickrTheme.spacing.xs).size(18.dp),
                strokeWidth = 2.dp,
                color = colors.primaryBrand,
            )
        } else {
            Icon(
                Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = colors.textSecondary,
                modifier = Modifier.padding(start = TickrTheme.spacing.xs).size(20.dp),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> OptionSheet(
    title: String,
    options: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = TickrTheme.colors
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.surface,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = TickrTheme.spacing.screen)
                .navigationBarsPadding(),
        ) {
            Text(text = title, style = TickrTheme.typography.sectionTitle, color = colors.textPrimary)
            Spacer(Modifier.height(TickrTheme.spacing.sm))
            options.forEach { (value, label) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(role = Role.RadioButton) {
                            onSelect(value)
                            onDismiss()
                        }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(
                        selected = value == selected,
                        onClick = null,
                        colors = RadioButtonDefaults.colors(
                            selectedColor = colors.primaryBrand,
                            unselectedColor = colors.textSecondary,
                        ),
                        modifier = Modifier.padding(end = TickrTheme.spacing.md, top = 12.dp, bottom = 12.dp),
                    )
                    Text(text = label, style = TickrTheme.typography.body, color = colors.textPrimary)
                }
            }
            Spacer(Modifier.height(TickrTheme.spacing.lg))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    TickrTheme {
        SettingsScreen(
            state = SettingsUiState(appName = "Tickr", appVersion = "1.0"),
            onAction = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenDarkPreview() {
    TickrTheme(darkTheme = true) {
        SettingsScreen(
            state = SettingsUiState(themeMode = ThemeMode.DARK, appName = "Tickr", appVersion = "1.0"),
            onAction = {},
        )
    }
}
