package com.project.tickr.presentation.common

import com.project.tickr.domain.model.DueReminder
import org.jetbrains.compose.resources.getPluralString
import org.jetbrains.compose.resources.getString
import tickrapp.shared.generated.resources.*

/**
 * Teks notifikasi. Memakai locale platform yang sedang aktif, jadi panggil `applyAppLanguage(...)`
 * lebih dulu bila dijalankan di luar UI (worker/penjadwalan).
 */
suspend fun DueReminder.notificationTitle(): String =
    if (daysBefore == 0) getString(Res.string.notif_title_today, itemName)
    else getPluralString(Res.plurals.notif_title_days, daysBefore, daysBefore, itemName)

suspend fun notificationBody(): String = getString(Res.string.notif_body)
