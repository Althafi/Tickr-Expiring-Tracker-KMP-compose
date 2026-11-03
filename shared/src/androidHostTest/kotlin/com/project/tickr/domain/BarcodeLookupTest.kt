package com.project.tickr.domain

import com.project.tickr.core.result.AppError
import com.project.tickr.core.result.DataResult
import com.project.tickr.domain.model.CategoryKeys
import com.project.tickr.domain.model.ItemUnits
import com.project.tickr.domain.model.ProductInfo
import com.project.tickr.domain.provider.ConnectivityProvider
import com.project.tickr.domain.repository.ProductLookupRepository
import com.project.tickr.domain.usecase.product.LookupBarcodeUseCase
import com.project.tickr.domain.usecase.product.ParsedQuantity
import com.project.tickr.domain.usecase.product.ProductQuantityParser
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BarcodeLookupTest {

    private class FakeConnectivity(var online: Boolean) : ConnectivityProvider {
        override fun isOnline() = online
    }

    private class FakeRepo(var result: DataResult<ProductInfo?> = DataResult.Success(null)) : ProductLookupRepository {
        var calls = 0
        override suspend fun lookup(barcode: String): DataResult<ProductInfo?> { calls++; return result }
        override suspend fun downloadImage(url: String): DataResult<ByteArray> = DataResult.Success(ByteArray(0))
    }

    private val product = ProductInfo("8992761111014", "Susu UHT", "Ultra", null, 1, ItemUnits.LITER, CategoryKeys.FOOD)

    @Test
    fun offline_never_touches_the_network() = runBlocking<Unit> {
        val repo = FakeRepo(DataResult.Success(product))
        val result = LookupBarcodeUseCase(FakeConnectivity(false), repo)("8992761111014")
        assertEquals(DataResult.Error(AppError.Offline), result)
        assertEquals(0, repo.calls)
    }

    @Test
    fun online_returns_the_product() = runBlocking<Unit> {
        val repo = FakeRepo(DataResult.Success(product))
        val result = LookupBarcodeUseCase(FakeConnectivity(true), repo)(" 8992761111014 ")
        assertEquals(DataResult.Success(product), result)
        assertEquals(1, repo.calls)
    }

    @Test
    fun invalid_barcodes_are_rejected_before_any_network_call() = runBlocking<Unit> {
        val repo = FakeRepo()
        val useCase = LookupBarcodeUseCase(FakeConnectivity(true), repo)
        listOf("", "123", "ABCDEFGH1234", "1234567890123456", "89927611110 4").forEach { code ->
            val result = useCase(code)
            assertIs<DataResult.Error>(result, "kode '$code' seharusnya ditolak")
            assertIs<AppError.Validation>(result.error)
        }
        assertEquals(0, repo.calls)
    }

    @Test
    fun network_failure_is_passed_through() = runBlocking<Unit> {
        val repo = FakeRepo(DataResult.Error(AppError.Network("timeout")))
        val result = LookupBarcodeUseCase(FakeConnectivity(true), repo)("8992761111014")
        assertIs<AppError.Network>((result as DataResult.Error).error)
    }

    @Test
    fun barcode_format_check() {
        assertTrue(LookupBarcodeUseCase.isValidBarcode("12345678"))     // EAN-8
        assertTrue(LookupBarcodeUseCase.isValidBarcode("036000291452")) // UPC-A
        assertTrue(LookupBarcodeUseCase.isValidBarcode("8992761111014"))// EAN-13
        assertTrue(!LookupBarcodeUseCase.isValidBarcode("1234567"))
    }

    @Test
    fun display_name_does_not_repeat_the_brand() {
        assertEquals("Indomilk Susu Kental", ProductInfo("1", "Susu Kental", "Indomilk", null, null, null, null).displayName)
        assertEquals("Indomilk Susu", ProductInfo("1", "Indomilk Susu", "Indomilk", null, null, null, null).displayName)
        assertEquals("Indomilk", ProductInfo("1", null, "Indomilk", null, null, null, null).displayName)
        assertNull(ProductInfo("1", " ", null, null, null, null, null).displayName)
    }

    @Test
    fun quantity_parser_reads_simple_sizes() {
        assertEquals(ParsedQuantity(250, ItemUnits.GRAM), ProductQuantityParser.parse("250 g"))
        assertEquals(ParsedQuantity(500, ItemUnits.MILLILITER), ProductQuantityParser.parse("500ml"))
        assertEquals(ParsedQuantity(1, ItemUnits.LITER), ProductQuantityParser.parse("1 L"))
        assertEquals(ParsedQuantity(2, ItemUnits.KILOGRAM), ProductQuantityParser.parse(" 2 kg "))
    }

    @Test
    fun quantity_parser_converts_fractions_to_a_smaller_unit() {
        assertEquals(ParsedQuantity(1500, ItemUnits.MILLILITER), ProductQuantityParser.parse("1,5 L"))
        assertEquals(ParsedQuantity(500, ItemUnits.GRAM), ProductQuantityParser.parse("0.5 kg"))
    }

    @Test
    fun quantity_parser_skips_what_it_cannot_read() {
        listOf(null, "", "6 x 100 g", "12 pcs", "250", "g 250", "1.5 g").forEach {
            assertNull(ProductQuantityParser.parse(it), "'$it' seharusnya dilewati")
        }
    }
}
