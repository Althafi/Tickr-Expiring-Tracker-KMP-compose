package com.project.tickr.ui.common

import androidx.compose.runtime.Composable

/** Mengembalikan fungsi yang memunculkan dialog izin notifikasi; hasilnya dikirim ke [onResult]. */
@Composable
expect fun rememberNotificationPermissionRequester(onResult: (Boolean) -> Unit): () -> Unit

/** Mengembalikan fungsi yang membuka pengaturan notifikasi app di sistem. */
@Composable
expect fun rememberOpenNotificationSettings(): () -> Unit
