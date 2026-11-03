package com.project.tickr.domain.usecase.reminder

import com.project.tickr.core.result.DataResult
import com.project.tickr.core.util.DateTimeUtil
import com.project.tickr.domain.model.DueReminder
import com.project.tickr.domain.model.NotificationEntry
import com.project.tickr.domain.model.NotificationLog
import com.project.tickr.domain.reminder.ReminderRules
import com.project.tickr.domain.repository.ItemRepository
import com.project.tickr.domain.repository.NotificationRepository
import com.project.tickr.domain.repository.ReminderPreferences
import com.project.tickr.domain.scheduler.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

internal fun Long.toLocalDateTime(): LocalDateTime =
    Instant.fromEpochMilliseconds(this).toLocalDateTime(TimeZone.currentSystemDefault())

/**
 * Mencatat pengingat yang sudah jatuh tempo ke riwayat (`notification_log`), sekali per (barang, tanggal).
 * Mengembalikan HANYA yang baru dicatat, sehingga pemanggil bisa menampilkannya tanpa duplikat.
 */
class ProcessDueRemindersUseCase(
    private val itemRepository: ItemRepository,
    private val notificationRepository: NotificationRepository,
    private val dateTime: DateTimeUtil,
) {
    suspend operator fun invoke(): DataResult<List<DueReminder>> {
        val items = when (val r = itemRepository.getItems()) {
            is DataResult.Success -> r.data
            is DataResult.Error -> return r
        }
        val now = dateTime.nowEpochMillis().toLocalDateTime()
        val recorded = mutableListOf<DueReminder>()

        for (item in items) {
            if (item.isConsumed) continue
            val expiry = runCatching { LocalDate.parse(item.expiryDate) }.getOrNull() ?: continue
            // createdAt tak terbaca → anggap baru dibuat, sehingga tidak ada pengingat lama yang "terkejar".
            val created = runCatching { Instant.parse(item.createdAt).toEpochMilliseconds().toLocalDateTime() }
                .getOrDefault(now)

            for (slot in ReminderRules.due(expiry, created, now)) {
                val date = slot.at.date.toString()
                val log = NotificationLog(0, item.id, date, isSent = true, createdAt = "")
                val result = notificationRepository.recordIfAbsent(log)
                if (result is DataResult.Success && result.data != null) {
                    recorded += DueReminder(item.id, item.name, slot.daysBefore, date)
                }
            }
        }
        return DataResult.Success(recorded)
    }
}

/**
 * Menyelaraskan notifikasi platform dengan data saat ini, lalu menampilkan yang jatuh tempo.
 * Dipanggil saat app dibuka, setelah barang berubah/dihapus/dikonsumsi, setelah bahasa berganti,
 * dan oleh worker harian (Android).
 *
 * Harus berupa singleton: [mutex] menserialkan pemanggilan bersamaan, dan [requestSync] berjalan di scope
 * sendiri sehingga tidak ikut batal ketika ViewModel pemanggilnya dibersihkan (mis. layar Detail ditutup).
 */
class SyncRemindersUseCase(
    private val itemRepository: ItemRepository,
    private val scheduler: ReminderScheduler,
    private val processDue: ProcessDueRemindersUseCase,
) {
    private val mutex = Mutex()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    suspend operator fun invoke(cancelItemId: Long? = null) = mutex.withLock {
        cancelItemId?.let { scheduler.cancel(it) }
        val active = (itemRepository.getItems() as? DataResult.Success)?.data.orEmpty().filter { !it.isConsumed }
        scheduler.syncAll(active)
        (processDue() as? DataResult.Success)?.data?.takeIf { it.isNotEmpty() }?.let { scheduler.deliver(it) }
    }

    /** Fire-and-forget; kegagalan tidak boleh mengganggu alur utama. */
    fun requestSync(cancelItemId: Long? = null) {
        scope.launch { runCatching { invoke(cancelItemId) } }
    }
}

/** Riwayat notifikasi (terbaru dulu) untuk layar Notifikasi. Barang yang sudah dihapus ikut hilang (cascade). */
class GetNotificationHistoryUseCase(
    private val notificationRepository: NotificationRepository,
    private val itemRepository: ItemRepository,
) {
    suspend operator fun invoke(): DataResult<List<NotificationEntry>> {
        val logs = when (val r = notificationRepository.getNotifications()) {
            is DataResult.Success -> r.data
            is DataResult.Error -> return r
        }
        val items = when (val r = itemRepository.getItems()) {
            is DataResult.Success -> r.data.associateBy { it.id }
            is DataResult.Error -> return r
        }
        val entries = logs.filter { it.isSent }.mapNotNull { log ->
            val item = items[log.itemId] ?: return@mapNotNull null
            val expiry = runCatching { LocalDate.parse(item.expiryDate) }.getOrNull() ?: return@mapNotNull null
            val scheduled = runCatching { LocalDate.parse(log.scheduledDate) }.getOrNull() ?: return@mapNotNull null
            NotificationEntry(
                id = log.id,
                itemId = item.id,
                itemName = item.name,
                imageUrl = item.imageUrl,
                daysBefore = (expiry.toEpochDays() - scheduled.toEpochDays()).toInt().coerceAtLeast(0),
                scheduledDate = log.scheduledDate,
            )
        }
        return DataResult.Success(entries.sortedWith(compareByDescending<NotificationEntry> { it.scheduledDate }.thenByDescending { it.id }))
    }
}

/** Menentukan apakah izin notifikasi perlu diminta sekarang (belum diizinkan dan belum pernah ditanyakan). */
class ShouldRequestNotificationPermissionUseCase(
    private val scheduler: ReminderScheduler,
    private val preferences: ReminderPreferences,
) {
    suspend operator fun invoke(): Boolean = !preferences.hasAskedPermission() && !scheduler.hasPermission()
}

class MarkNotificationPermissionAskedUseCase(private val preferences: ReminderPreferences) {
    operator fun invoke() = preferences.markPermissionAsked()
}

class HasNotificationPermissionUseCase(private val scheduler: ReminderScheduler) {
    suspend operator fun invoke(): Boolean = scheduler.hasPermission()
}
