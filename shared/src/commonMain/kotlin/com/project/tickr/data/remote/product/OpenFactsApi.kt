package com.project.tickr.data.remote.product

import com.project.tickr.core.result.AppError
import com.project.tickr.core.result.DataResult
import com.project.tickr.domain.model.CategoryKeys
import com.project.tickr.domain.model.ProductInfo
import com.project.tickr.domain.repository.ProductLookupRepository
import com.project.tickr.domain.usecase.product.ProductQuantityParser
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsBytes
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json

/**
 * Database produk publik gratis (Open Food Facts dan saudaranya). Dicoba berurutan sampai ketemu;
 * sumber yang menemukan produk juga menjadi petunjuk kategorinya.
 */
internal class OpenFactsApi(
    private val client: HttpClient = createHttpClient(),
) : ProductLookupRepository {

    private data class Source(val host: String, val categoryKey: String)

    private val sources = listOf(
        Source("world.openfoodfacts.org", CategoryKeys.FOOD),
        Source("world.openbeautyfacts.org", CategoryKeys.BEAUTY),
        Source("world.openproductsfacts.org", CategoryKeys.OTHER),
    )

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    override suspend fun lookup(barcode: String): DataResult<ProductInfo?> {
        if (barcode.isEmpty() || !barcode.all { it in '0'..'9' }) {
            return DataResult.Error(AppError.Validation("barcode", "must be digits"))
        }
        return try {
            for (source in sources) {
                val found = fetch(source, barcode)
                if (found != null) return DataResult.Success(found)
            }
            DataResult.Success(null)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            DataResult.Error(AppError.Network(e.message ?: e::class.simpleName ?: "unknown"))
        }
    }

    private suspend fun fetch(source: Source, barcode: String): ProductInfo? {
        val url = "https://${source.host}/api/v2/product/$barcode.json?fields=$FIELDS"
        val response = client.get(url) { header(HttpHeaders.UserAgent, USER_AGENT) }
        // Produk yang tidak ada dijawab 404 (dengan isi JSON status 0) oleh sebagian server.
        if (response.status == HttpStatusCode.NotFound) return null
        if (!response.status.isSuccess()) throw IllegalStateException("HTTP ${response.status.value}")

        val parsed = json.decodeFromString<OpenFactsResponse>(response.bodyAsText())
        val product = parsed.product
        if (parsed.status != 1 || product == null) return null

        val name = product.product_name_id?.takeIf { it.isNotBlank() } ?: product.product_name?.takeIf { it.isNotBlank() }
        val parsedQuantity = ProductQuantityParser.parse(product.quantity)
        val info = ProductInfo(
            barcode = barcode,
            name = name,
            brand = product.brands?.substringBefore(',')?.trim()?.takeIf { it.isNotEmpty() },
            imageUrl = product.image_front_url?.takeIf { it.startsWith("https://") },
            quantity = parsedQuantity?.amount,
            unit = parsedQuantity?.unit,
            categoryKey = source.categoryKey,
        )
        // Entri kosong (tanpa nama dan merek) tidak berguna untuk mengisi form.
        return info.takeIf { it.displayName != null }
    }

    override suspend fun downloadImage(url: String): DataResult<ByteArray> {
        if (!url.startsWith("https://")) return DataResult.Error(AppError.Validation("image_url", "must be https"))
        return try {
            val response: HttpResponse = client.get(url) { header(HttpHeaders.UserAgent, USER_AGENT) }
            if (!response.status.isSuccess()) throw IllegalStateException("HTTP ${response.status.value}")
            val bytes = response.bodyAsBytes()
            if (bytes.isEmpty() || bytes.size > MAX_IMAGE_BYTES) {
                DataResult.Error(AppError.Unknown("Ukuran gambar tidak valid"))
            } else DataResult.Success(bytes)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            DataResult.Error(AppError.Network(e.message ?: e::class.simpleName ?: "unknown"))
        }
    }

    private companion object {
        const val FIELDS = "product_name,product_name_id,brands,quantity,image_front_url"
        const val USER_AGENT = "Tickr/1.0 (Kotlin Multiplatform; pelacak kedaluwarsa)"
        const val MAX_IMAGE_BYTES = 5 * 1024 * 1024
    }
}

internal fun createHttpClient(): HttpClient = HttpClient {
    install(HttpTimeout) {
        connectTimeoutMillis = 8_000
        socketTimeoutMillis = 10_000
        requestTimeoutMillis = 15_000
    }
    expectSuccess = false
}
