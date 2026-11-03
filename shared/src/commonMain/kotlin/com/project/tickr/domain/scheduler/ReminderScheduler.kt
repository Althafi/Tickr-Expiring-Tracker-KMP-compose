package com.project.tickr.domain.scheduler

import com.project.tickr.domain.model.DueReminder
import com.project.tickr.domain.model.Item

/** Notifikasi lokal terjadwal. Tanpa server, tanpa token. */
interface ReminderScheduler {
    suspend fun hasPermission(): Boolean

    /** Menyelaraskan jadwal platform dengan [items] (barang belum dikonsumsi). Idempoten. */
    suspend fun syncAll(items: List<Item>)

    /** Menampilkan pengingat yang jatuh tempo dan belum pernah ditampilkan. Sistem iOS sudah mengirimnya sendiri. */
    suspend fun deliver(due: List<DueReminder>)

    /** Membatalkan jadwal dan menghapus notifikasi yang masih tampil untuk satu barang. */
    suspend fun cancel(itemId: Long)

    suspend fun cancelAll()
}
