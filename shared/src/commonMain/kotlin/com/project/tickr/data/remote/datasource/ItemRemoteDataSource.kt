package com.project.tickr.data.remote.datasource

import com.project.tickr.core.result.DataResult
import com.project.tickr.data.remote.dto.CreateItemDto
import com.project.tickr.data.remote.dto.ItemDto
import io.github.jan.supabase.SupabaseClient

class ItemRemoteDataSource(client: SupabaseClient) : BaseRemoteDataSource(client) {

    private val table = "items"

    suspend fun getAll(): DataResult<List<ItemDto>> = safeApiCall {
        db.from(table).select().decodeList<ItemDto>()
    }

    suspend fun getByUserId(userId: String): DataResult<List<ItemDto>> = safeApiCall {
        db.from(table).select { filter { eq("user_id", userId) } }.decodeList<ItemDto>()
    }

    suspend fun getById(id: Long): DataResult<ItemDto> = safeApiCall {
        db.from(table).select { filter { eq("id", id) } }.decodeSingle<ItemDto>()
    }

    suspend fun insert(dto: CreateItemDto): DataResult<ItemDto> = safeApiCall {
        db.from(table).insert(dto) { select() }.decodeSingle<ItemDto>()
    }

    suspend fun update(id: Long, dto: ItemDto): DataResult<ItemDto> = safeApiCall {
        db.from(table).update(dto) { filter { eq("id", id) }; select() }.decodeSingle<ItemDto>()
    }

    suspend fun delete(id: Long): DataResult<Unit> = safeApiCall {
        db.from(table).delete { filter { eq("id", id) } }
        Unit
    }
}
