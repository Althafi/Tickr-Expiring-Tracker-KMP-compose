package com.project.tickr.data.remote.product

import kotlinx.serialization.Serializable

/** Respons `GET /api/v2/product/{barcode}` milik Open Food Facts (dan saudaranya Open Beauty/Products Facts). */
@Serializable
internal data class OpenFactsResponse(
    val status: Int = 0,
    val product: OpenFactsProduct? = null,
)

@Serializable
internal data class OpenFactsProduct(
    val product_name: String? = null,
    val product_name_id: String? = null,
    val brands: String? = null,
    val quantity: String? = null,
    val image_front_url: String? = null,
)
