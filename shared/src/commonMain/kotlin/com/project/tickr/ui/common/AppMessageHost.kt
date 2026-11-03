package com.project.tickr.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.project.tickr.presentation.common.AppMessage
import com.project.tickr.presentation.common.AppMessenger
import com.project.tickr.presentation.common.MessageKind
import com.project.tickr.presentation.common.resolve
import com.project.tickr.ui.theme.TickrTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Jarak bawah yang harus disisakan snackbar. Layar dengan bilah bawah (MainShell) mengisinya
 * dengan tinggi bilahnya dan mengembalikannya ke 0 saat hilang.
 */
@Stable
class SnackbarInset {
    var bottom: Dp by mutableStateOf(0.dp)
}

val LocalSnackbarInset = compositionLocalOf { SnackbarInset() }

/**
 * Menampilkan semua [AppMessage] dari [AppMessenger] sebagai snackbar. Letakkan SEKALI di akar UI,
 * di atas NavHost, supaya pesan tetap terlihat walau layar pengirimnya sudah tertutup.
 */
@Composable
fun AppMessageHost(
    messenger: AppMessenger,
    inset: SnackbarInset,
    modifier: Modifier = Modifier,
) {
    val hostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var current by remember { mutableStateOf<AppMessage?>(null) }

    // collectLatest: pesan baru menggantikan yang sedang tampil, tidak mengantre 4 detik per pesan.
    LaunchedEffect(messenger, hostState) {
        messenger.messages.collectLatest { message ->
            val text = message.text.resolve()
            val label = message.actionLabel?.resolve()
            current = message
            val result = hostState.showSnackbar(
                message = text,
                actionLabel = label,
                withDismissAction = label == null,
                duration = if (label != null) SnackbarDuration.Long else SnackbarDuration.Short,
            )
            if (result == SnackbarResult.ActionPerformed) {
                // Dijalankan di scope terpisah agar tidak ikut batal bila pesan lain datang.
                message.onAction?.let { action -> scope.launch { action() } }
            }
        }
    }

    val bottomPadding = if (inset.bottom > 0.dp) Modifier.padding(bottom = inset.bottom) else Modifier.navigationBarsPadding()
    Box(modifier = modifier.fillMaxWidth().then(bottomPadding), contentAlignment = Alignment.BottomCenter) {
        SnackbarHost(hostState = hostState, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) { data ->
            val colors = TickrTheme.colors
            val isError = current?.kind == MessageKind.ERROR
            Snackbar(
                snackbarData = data,
                containerColor = if (isError) colors.critical else colors.textPrimary,
                contentColor = if (isError) colors.onCritical else colors.background,
                actionColor = if (isError) colors.onCritical else colors.background,
                dismissActionContentColor = if (isError) colors.onCritical else colors.background,
            )
        }
    }
}
