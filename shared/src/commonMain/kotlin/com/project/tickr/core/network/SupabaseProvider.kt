@file:OptIn(io.github.jan.supabase.annotations.SupabaseInternal::class)

package com.project.tickr.core.network

import com.project.tickr.BuildKonfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging

object SupabaseProvider {
    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = BuildKonfig.SUPABASE_URL,
            supabaseKey = BuildKonfig.SUPABASE_KEY
        ) {
            httpEngine = provideHttpEngine()
            install(Postgrest)
            install(Auth)
            install(Storage)
            httpConfig {
                install(Logging) {
                    level = LogLevel.ALL
                }
            }
        }
    }
}
