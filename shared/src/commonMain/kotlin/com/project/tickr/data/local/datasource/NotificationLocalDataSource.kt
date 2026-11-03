package com.project.tickr.data.local.datasource

import com.project.tickr.core.result.DataResult
import com.project.tickr.db.Notification_log as NotificationRow
import com.project.tickr.db.TickrDatabase
import com.project.tickr.domain.model.NotificationLog

internal class NotificationLocalDataSource(
    private val db: TickrDatabase,
    private val now: () -> String = ::nowIso,
) {
    private val queries get() = db.notificationLogQueries

    suspend fun getAll(): DataResult<List<NotificationRow>> =
        safeDbCall { queries.selectAll().executeAsList() }

    suspend fun getByItem(itemId: Long): DataResult<List<NotificationRow>> =
        safeDbCall { queries.selectByItem(itemId).executeAsList() }

    suspend fun getById(id: Long): DataResult<NotificationRow> =
        safeDbCall { queries.selectById(id).executeAsOneOrNull() ?: throw EntityNotFoundException("notification") }

    suspend fun insert(notification: NotificationLog): DataResult<NotificationRow> = safeDbCall {
        db.transactionWithResult {
            queries.insert(
                item_id = notification.itemId,
                scheduled_date = notification.scheduledDate,
                is_sent = if (notification.isSent) 1L else 0L,
                created_at = now(),
            )
            queries.selectById(queries.lastInsertRowId().executeAsOne()).executeAsOne()
        }
    }

    suspend fun insertIfAbsent(notification: NotificationLog): DataResult<NotificationRow?> = safeDbCall {
        db.transactionWithResult {
            val exists = queries.selectByItem(notification.itemId).executeAsList()
                .any { it.scheduled_date == notification.scheduledDate }
            if (exists) null
            else {
                queries.insert(
                    item_id = notification.itemId,
                    scheduled_date = notification.scheduledDate,
                    is_sent = if (notification.isSent) 1L else 0L,
                    created_at = now(),
                )
                queries.selectById(queries.lastInsertRowId().executeAsOne()).executeAsOne()
            }
        }
    }

    suspend fun delete(id: Long): DataResult<Unit> =
        safeDbCall { queries.delete(id) }
}
