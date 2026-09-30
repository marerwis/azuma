package com.example.model

/**
 * Represents the authenticated user session after Firebase token verification.
 * Stored in ViewModel state after a successful /api/v1/auth/verify call.
 */
data class UserSession(
    val id: String,
    val email: String?,
    val fullName: String,
    val phone: String?,
    val role: String,        // "customer" | "vendor" | "driver" | "admin"
    val avatarUrl: String?,
    val supabaseToken: String?
)
