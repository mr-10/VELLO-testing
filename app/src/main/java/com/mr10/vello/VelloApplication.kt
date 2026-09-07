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
            supabaseUrl = "https://rrfpeekydobsejyvqycz.supabase.co",
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InJyZnBlZWt5ZG9ic2VqeXZxeWN6Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODcwMzAxMjcsImV4cCI6MjEwMjYwNjEyN30.jD1ApBusf01FC5zBnjkjn7es35n3Mp6XB1kWgfANcpQ"
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
