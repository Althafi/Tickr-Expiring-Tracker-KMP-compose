package com.project.tickr.domain.repository

import com.project.tickr.core.result.DataResult
import com.project.tickr.domain.model.NotificationLog

interface NotificationRepository {
    suspend fun getNotifications(): DataResult<List<NotificationLog>>
    suspend fun getNotificationsByItem(itemId: Long): DataResult<List<NotificationLog>>
    suspend fun getNotification(id: Long): DataResult<NotificationLog>
    suspend fun createNotification(notification: NotificationLog): DataResult<NotificationLog>
    /** Menyimpan hanya bila belum ada log untuk (item, tanggal) yang sama. Mengembalikan `null` bila sudah ada. */
    suspend fun recordIfAbsent(notification: NotificationLog): DataResult<NotificationLog?>
    suspend fun deleteNotification(id: Long): DataResult<Unit>
}
