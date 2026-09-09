package com.mr10.vello

import android.app.Application
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.serializer.KotlinXSerializer
import kotlinx.serialization.json.Json

class VelloApplication : Application() {

    companion object {
        lateinit var supabaseClient: SupabaseClient
            private set
        
        const val AGORA_APP_ID = "YOUR_AGORA_APP_ID_HERE" // Placeholder
    }

    override fun onCreate() {
        super.onCreate()

        supabaseClient = createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_KEY
        ) {
            install(Auth)
            install(Postgrest)
            install(Realtime)
            install(Storage)
            install(Functions)
            
            // Configure JSON to ignore nulls during serialization
            defaultSerializer = KotlinXSerializer(Json {
                ignoreUnknownKeys = true
                encodeDefaults = false // This prevents sending nulls for optional fields
            })
        }
    }
}
