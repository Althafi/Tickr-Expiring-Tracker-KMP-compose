package com.project.tickr.data.remote.datasource

import com.project.tickr.core.result.DataResult
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.storage.storage
import kotlin.random.Random

class StorageRemoteDataSource(client: SupabaseClient) : BaseRemoteDataSource(client) {

    suspend fun uploadProductImage(userId: String, bytes: ByteArray): DataResult<String> = safeApiCall {
        val fileName = "$userId/${Random.nextLong(0, Long.MAX_VALUE)}.jpg"
        client.storage.from(BUCKET).upload(fileName, bytes)
        client.storage.from(BUCKET).publicUrl(fileName)
    }

    companion object {
        private const val BUCKET = "tickr-product-images"
    }
}
