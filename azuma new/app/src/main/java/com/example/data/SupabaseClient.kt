package com.example.data

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

import io.github.jan.supabase.serializer.KotlinXSerializer
import kotlinx.serialization.json.Json

const val BASE_IMAGE_URL = "https://arivoyaepcxaoupzvvbw.supabase.co/storage/v1/object/public/store_images"

val supabase: SupabaseClient by lazy {
    createSupabaseClient(
        supabaseUrl = "https://arivoyaepcxaoupzvvbw.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImFyaXZveWFlcGN4YW91cHp2dmJ3Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3OTA1MzAwODQsImV4cCI6MjEwNjEwNjA4NH0.ktk8TTu6QZB5PZuYnQl-Qy9Edx3RN8ZPmaR2AGigwok"
    ) {
        install(Postgrest) {
            serializer = KotlinXSerializer(
                Json {
                    ignoreUnknownKeys = true
                    coerceInputValues = true
                }
            )
        }
        install(Auth)
        install(Storage)
    }
}
