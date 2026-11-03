package com.project.tickr.domain.model

/** Kunci satuan yang disimpan di database; teks tampil diambil dari resource berdasarkan kunci ini. */
object ItemUnits {
    const val PCS = "PCS"
    const val GRAM = "GRAM"
    const val KILOGRAM = "KILOGRAM"
    const val LITER = "LITER"
    const val MILLILITER = "MILLILITER"
    const val BOTTLE = "BOTTLE"
    const val PACK = "PACK"
    const val DOZEN = "DOZEN"

    val ALL = listOf(PCS, GRAM, KILOGRAM, LITER, MILLILITER, BOTTLE, PACK, DOZEN)
    const val DEFAULT = PCS
}
