package com.project.tickr.data.local.db

import com.project.tickr.db.TickrDatabase

/** Mengisi 4 kategori default saat database pertama kali dibuat. Idempoten. */
internal object DatabaseSeeder {

    private data class DefaultCategory(
        val key: String,
        val name: String,
        val iconName: String,
        val colorHex: String,
    )

    // Nama di sini hanya fallback; nama tampil diambil dari resource berdasarkan `key` (Fase 6).
    private val defaults = listOf(
        DefaultCategory("food", "Makanan & Minuman", "ic_food", "#0D6759"),
        DefaultCategory("beauty", "Kecantikan", "ic_beauty", "#FA9A08"),
        DefaultCategory("medicine", "Obat & Vitamin", "ic_medicine", "#5EC9B7"),
        DefaultCategory("other", "Lainnya", "ic_other", "#6B7280"),
    )

    fun seedIfEmpty(db: TickrDatabase, createdAt: String) {
        db.transaction {
            if (db.categoryQueries.count().executeAsOne() > 0L) return@transaction
            defaults.forEach {
                db.categoryQueries.insert(it.key, it.name, it.iconName, it.colorHex, createdAt)
            }
        }
    }
}
