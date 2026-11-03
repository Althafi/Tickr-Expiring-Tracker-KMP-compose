package com.project.tickr.domain.model

/** Data produk hasil pencarian barcode. Semua field selain [barcode] boleh kosong: database publik tidak lengkap. */
data class ProductInfo(
    val barcode: String,
    val name: String?,
    val brand: String?,
    /** URL foto depan kemasan (di internet), belum disimpan ke perangkat. */
    val imageUrl: String?,
    val quantity: Int?,
    /** Salah satu kunci [ItemUnits], hanya terisi bila [quantity] terisi. */
    val unit: String?,
    /** Salah satu [CategoryKeys], atau null bila tidak bisa ditebak. */
    val categoryKey: String?,
) {
    /** "Merek Nama", tanpa mengulang merek bila sudah ada di dalam nama. */
    val displayName: String?
        get() {
            val n = name?.trim().orEmpty()
            val b = brand?.trim().orEmpty()
            return when {
                n.isEmpty() && b.isEmpty() -> null
                n.isEmpty() -> b
                b.isEmpty() || n.contains(b, ignoreCase = true) -> n
                else -> "$b $n"
            }
        }
}
