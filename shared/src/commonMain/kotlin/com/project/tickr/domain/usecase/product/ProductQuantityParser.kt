package com.project.tickr.domain.usecase.product

import com.project.tickr.domain.model.ItemUnits

/** Hasil membaca ukuran kemasan, mis. "250 g" → 250 GRAM. */
data class ParsedQuantity(val amount: Int, val unit: String)

/**
 * Membaca teks ukuran kemasan dari database produk ("250 g", "1,5 L", "500ml").
 * Hanya mengenali bentuk sederhana "angka + satuan"; bentuk lain ("6 x 100 g") dilewati supaya
 * pengguna mengisi sendiri daripada menerima tebakan yang salah.
 */
object ProductQuantityParser {
    private val pattern = Regex("""^\s*(\d+(?:[.,]\d+)?)\s*([a-zA-Z]+)\s*$""")

    fun parse(text: String?): ParsedQuantity? {
        if (text.isNullOrBlank()) return null
        val match = pattern.matchEntire(text) ?: return null
        val number = match.groupValues[1].replace(',', '.').toDoubleOrNull() ?: return null
        val unit = when (match.groupValues[2].lowercase()) {
            "g", "gr", "gram" -> ItemUnits.GRAM
            "kg" -> ItemUnits.KILOGRAM
            "l", "lt", "ltr", "liter" -> ItemUnits.LITER
            "ml" -> ItemUnits.MILLILITER
            else -> return null
        }
        val amount = number.toInt()
        // 0,5 L tidak bisa jadi bilangan bulat di satuan yang sama: ubah ke satuan yang lebih kecil.
        if (amount < 1 || number != amount.toDouble()) {
            return when (unit) {
                ItemUnits.LITER -> ParsedQuantity((number * 1000).toInt(), ItemUnits.MILLILITER)
                ItemUnits.KILOGRAM -> ParsedQuantity((number * 1000).toInt(), ItemUnits.GRAM)
                else -> null
            }.takeIf { it != null && it.amount >= 1 }
        }
        return ParsedQuantity(amount, unit)
    }
}
