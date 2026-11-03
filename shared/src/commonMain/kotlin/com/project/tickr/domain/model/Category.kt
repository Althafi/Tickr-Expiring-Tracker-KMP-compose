package com.project.tickr.domain.model

data class Category(
    val id: Long,
    val userId: String?,
    val name: String,
    val iconName: String,
    val colorHex: String,
    val createdAt: String,
    /** Kunci stabil kategori default (`food`, `beauty`, `medicine`, `other`); null untuk kategori buatan pengguna. */
    val key: String? = null
)

/** Kunci stabil kategori default; nama tampil diambil dari resource berdasarkan kunci ini. */
object CategoryKeys {
    const val FOOD = "food"
    const val BEAUTY = "beauty"
    const val MEDICINE = "medicine"
    const val OTHER = "other"
    val DEFAULTS = listOf(FOOD, BEAUTY, MEDICINE, OTHER)

    /** Nama cadangan bila kategori sebuah barang sudah tidak ada. */
    const val FALLBACK_NAME = "Lainnya"
}
