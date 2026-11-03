package com.project.tickr.ui.screen.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.project.tickr.presentation.navigation.Navigator
import com.project.tickr.presentation.settings.SettingsAction
import com.project.tickr.presentation.settings.SettingsEvent
import com.project.tickr.presentation.settings.SettingsViewModel
import com.project.tickr.ui.common.BackupFileResult
import com.project.tickr.ui.common.rememberBackupFiles
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsRoute(
    navigator: Navigator,
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val backupFiles = rememberBackupFiles()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is SettingsEvent.Navigate -> navigator.navigate(event.destination)
                is SettingsEvent.SaveBackupFile -> backupFiles.save(event.fileName, event.content) { result ->
                    viewModel.onAction(
                        when (result) {
                            is BackupFileResult.Success -> SettingsAction.BackupSaved
                            BackupFileResult.Cancelled -> SettingsAction.BackupFileCancelled
                            BackupFileResult.Failed -> SettingsAction.BackupFileFailed(forExport = true)
                        }
                    )
                }
                SettingsEvent.PickBackupFile -> backupFiles.open { result ->
                    viewModel.onAction(
                        when (result) {
                            is BackupFileResult.Success ->
                                result.text?.let { SettingsAction.BackupFileRead(it) }
                                    ?: SettingsAction.BackupFileFailed(forExport = false)
                            BackupFileResult.Cancelled -> SettingsAction.BackupFileCancelled
                            BackupFileResult.Failed -> SettingsAction.BackupFileFailed(forExport = false)
                        }
                    )
                }
            }
        }
    }

    SettingsScreen(state = state, onAction = viewModel::onAction)
}
