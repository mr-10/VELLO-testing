package com.mr10.vello.di

import com.mr10.vello.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.realtime
import io.github.jan.supabase.serializer.KotlinXSerializer
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SupabaseModule {

    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient {
        return createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_KEY
        ) {
            install(Auth)
            install(Postgrest)
            install(Realtime)
            install(Storage)
            install(Functions)

            defaultSerializer = KotlinXSerializer(Json {
                ignoreUnknownKeys = true
                encodeDefaults = false
            })
        }
    }

    @Provides
    @Singleton
    fun provideSupabaseAuth(client: SupabaseClient) = client.auth

    @Provides
    @Singleton
    fun provideSupabasePostgrest(client: SupabaseClient) = client.postgrest

    @Provides
    @Singleton
    fun provideSupabaseRealtime(client: SupabaseClient) = client.realtime

    @Provides
    @Singleton
    fun provideSupabaseStorage(client: SupabaseClient) = client.storage

    @Provides
    @Singleton
    fun provideSupabaseFunctions(client: SupabaseClient) = client.functions
}
