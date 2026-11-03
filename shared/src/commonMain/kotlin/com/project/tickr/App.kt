package com.project.tickr

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.project.tickr.domain.model.ThemeMode
import com.project.tickr.domain.provider.AppInfoProvider
import com.project.tickr.domain.storage.ImageStorage
import com.project.tickr.domain.usecase.onboarding.GetOnboardingSeenUseCase
import com.project.tickr.domain.usecase.settings.ObserveSettingsUseCase
import com.project.tickr.presentation.common.AppMessenger
import com.project.tickr.presentation.navigation.Destination
import com.project.tickr.ui.common.AppMessageHost
import com.project.tickr.ui.common.LocalSnackbarInset
import com.project.tickr.ui.common.SnackbarInset
import com.project.tickr.ui.common.ApplyPlatformTheme
import com.project.tickr.ui.common.ProvideProductImageResolver
import com.project.tickr.ui.i18n.AppLocaleProvider
import com.project.tickr.ui.i18n.LocalAppInfo
import com.project.tickr.ui.navigation.TickrNavGraph
import com.project.tickr.ui.theme.TickrTheme
import kotlinx.coroutines.flow.first
import org.koin.compose.koinInject
import com.project.tickr.domain.usecase.reminder.SyncRemindersUseCase

@Composable
@Preview
fun App() {
    // StateFlow dengan nilai awal sinkron: tema & bahasa benar sejak frame pertama, tanpa berkedip.
    val observeSettings: ObserveSettingsUseCase = koinInject()
    val settings by observeSettings().collectAsState()
    val darkTheme = when (settings.themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    ApplyPlatformTheme(themeMode = settings.themeMode, darkTheme = darkTheme)

    // State yang HARUS bertahan saat bahasa diganti diletakkan di atas AppLocaleProvider,
    // karena provider membuat ulang seluruh isinya (key per bahasa).
    val navController = rememberNavController()
    val getOnboardingSeen: GetOnboardingSeenUseCase = koinInject()
    var startDestination by remember { mutableStateOf<Destination?>(null) }
    LaunchedEffect(Unit) {
        startDestination = if (getOnboardingSeen().first()) Destination.Home else Destination.Onboarding
    }

    // Selaraskan jadwal pengingat & catat yang jatuh tempo setiap kali app dibuka.
    val syncReminders: SyncRemindersUseCase = koinInject()
    LaunchedEffect(Unit) { runCatching { syncReminders() } }

    val imageStorage: ImageStorage = koinInject()
    val appInfoProvider: AppInfoProvider = koinInject()
    val appInfo = remember(appInfoProvider) { appInfoProvider.getAppInfo() }
    val messenger: AppMessenger = koinInject()
    val snackbarInset = remember { SnackbarInset() }

    AppLocaleProvider(language = settings.language) {
        TickrTheme(darkTheme = darkTheme) {
            Surface(modifier = Modifier.fillMaxSize()) {
                CompositionLocalProvider(
                    LocalAppInfo provides appInfo,
                    LocalSnackbarInset provides snackbarInset,
                ) {
                    Box(Modifier.fillMaxSize()) {
                        // null = sedang menentukan start destination; hindari NavHost sebelum siap
                        startDestination?.let { start ->
                            ProvideProductImageResolver(imageStorage) {
                                TickrNavGraph(
                                    navController = navController,
                                    startDestination = start,
                                )
                            }
                        }
                        // Satu host untuk semua layar; pesan bertahan walau layar pengirimnya sudah tertutup.
                        AppMessageHost(
                            messenger = messenger,
                            inset = snackbarInset,
                            modifier = Modifier.align(Alignment.BottomCenter),
                        )
                    }
                }
            }
        }
    }
}
