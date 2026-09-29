package com.example.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// ── Generic API wrapper ─────────────────────────────────────────────────────
@JsonClass(generateAdapter = true)
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: String? = null,
    val count: Int? = null
)

// ── Auth ────────────────────────────────────────────────────────────────────
@JsonClass(generateAdapter = true)
data class VerifyRequest(
    @Json(name = "idToken") val idToken: String,
    @Json(name = "fcmToken") val fcmToken: String? = null,
    @Json(name = "fullName") val fullName: String? = null
)

@JsonClass(generateAdapter = true)
data class UserDto(
    val id: String,
    val email: String?,
    @Json(name = "full_name") val fullName: String?,
    val phone: String?,
    val role: String,
    @Json(name = "avatar_url") val avatarUrl: String?,
    @Json(name = "is_active") val isActive: Boolean?,
    @Json(name = "created_at") val createdAt: String?
)

@JsonClass(generateAdapter = true)
data class VerifyResponse(
    val success: Boolean,
    val user: UserDto? = null,
    val error: String? = null
)

// ── App Categories ──────────────────────────────────────────────────────────
@JsonClass(generateAdapter = true)
data class AppCategoryDto(
    val id: String,
    val name: String,
    @Json(name = "image_url") val imageUrl: String? = null,
    @Json(name = "is_active") val isActive: Boolean = true,
    @Json(name = "sort_order") val sortOrder: Int = 0
)

// ── Stores ──────────────────────────────────────────────────────────────────
@JsonClass(generateAdapter = true)
data class AppCategoryRefDto(
    val id: String,
    val name: String,
    @Json(name = "image_url") val imageUrl: String? = null
)

@JsonClass(generateAdapter = true)
data class StoreCountDto(
    @Json(name = "menu_categories") val menuCategories: Int = 0,
    val products: Int = 0
)

@JsonClass(generateAdapter = true)
data class StoreDto(
    val id: String,
    val name: String,
    val description: String? = null,
    @Json(name = "image_url") val imageUrl: String? = null,
    @Json(name = "cover_url") val coverUrl: String? = null,
    val address: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    @Json(name = "delivery_radius_km") val deliveryRadiusKm: Double? = null,
    @Json(name = "is_active") val isActive: Boolean = true,
    @Json(name = "is_open") val isOpen: Boolean = false,
    @Json(name = "opening_time") val openingTime: String? = null,
    @Json(name = "closing_time") val closingTime: String? = null,
    @Json(name = "created_at") val createdAt: String? = null,
    @Json(name = "app_categories") val appCategory: AppCategoryRefDto? = null,
    @Json(name = "_count") val count: StoreCountDto? = null
)

// ── Menu Categories ─────────────────────────────────────────────────────────
@JsonClass(generateAdapter = true)
data class MenuCategoryDto(
    val id: String,
    val name: String,
    @Json(name = "store_id") val storeId: String? = null,
    @Json(name = "sort_order") val sortOrder: Int = 0,
    @Json(name = "is_active") val isActive: Boolean = true
)

// ── Products ────────────────────────────────────────────────────────────────
@JsonClass(generateAdapter = true)
data class ProductDto(
    val id: String,
    val name: String,
    val description: String? = null,
    @Json(name = "base_price") val basePrice: Double,
    @Json(name = "image_url") val imageUrl: String? = null,
    @Json(name = "is_veg") val isVeg: Boolean = false,
    @Json(name = "is_active") val isActive: Boolean = true,
    @Json(name = "stock_quantity") val stockQuantity: Int? = null,
    @Json(name = "menu_category_id") val menuCategoryId: String? = null
)
