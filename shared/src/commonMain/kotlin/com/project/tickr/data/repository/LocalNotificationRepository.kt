package com.project.tickr.data.repository

import com.project.tickr.core.result.DataResult
import com.project.tickr.core.result.mapData
import com.project.tickr.data.local.datasource.NotificationLocalDataSource
import com.project.tickr.data.mapper.toDomain
import com.project.tickr.domain.model.NotificationLog
import com.project.tickr.domain.repository.NotificationRepository

internal class LocalNotificationRepository(
    private val local: NotificationLocalDataSource,
) : NotificationRepository {

    override suspend fun getNotifications(): DataResult<List<NotificationLog>> =
        local.getAll().mapData { rows -> rows.map { it.toDomain() } }

    override suspend fun getNotificationsByItem(itemId: Long): DataResult<List<NotificationLog>> =
        local.getByItem(itemId).mapData { rows -> rows.map { it.toDomain() } }

    override suspend fun getNotification(id: Long): DataResult<NotificationLog> =
        local.getById(id).mapData { it.toDomain() }

    override suspend fun createNotification(notification: NotificationLog): DataResult<NotificationLog> =
        local.insert(notification).mapData { it.toDomain() }

    override suspend fun recordIfAbsent(notification: NotificationLog): DataResult<NotificationLog?> =
        local.insertIfAbsent(notification).mapData { it?.toDomain() }

    override suspend fun deleteNotification(id: Long): DataResult<Unit> =
        local.delete(id)
}
