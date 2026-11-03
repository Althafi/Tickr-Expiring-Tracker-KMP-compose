package com.project.tickr.data.local.reminder

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.project.tickr.domain.usecase.reminder.SyncRemindersUseCase
import org.koin.core.context.GlobalContext

/** Berjalan harian ~08:00: mencatat dan menampilkan pengingat yang jatuh tempo. */
class ReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        // Koin dimulai di Application.onCreate yang selalu berjalan sebelum worker.
        val koin = GlobalContext.getOrNull() ?: return Result.retry()
        return try {
            koin.get<SyncRemindersUseCase>().invoke()
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }
}
