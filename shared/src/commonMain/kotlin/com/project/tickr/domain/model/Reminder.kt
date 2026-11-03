package com.project.tickr.domain.model

/** Pengingat yang jatuh tempo dan baru dicatat ke riwayat. [scheduledDate]: tanggal ISO pengingat. */
data class DueReminder(
    val itemId: Long,
    val itemName: String,
    val daysBefore: Int,
    val scheduledDate: String,
)

/** Satu baris riwayat notifikasi untuk layar Notifikasi. */
data class NotificationEntry(
    val id: Long,
    val itemId: Long,
    val itemName: String,
    val imageUrl: String?,
    val daysBefore: Int,
    val scheduledDate: String,
)
