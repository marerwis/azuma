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
    val isNewUser: Boolean? = null,
    val supabaseToken: String? = null,
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
    @Json(name = "commission_rate") val commissionRate: Double? = null,
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

// ── Banners ───────────────────────────────────────────────────────
@JsonClass(generateAdapter = true)
data class BannerDto(
    val id: String,
    @Json(name = "image_url") val imageUrl: String,
    @Json(name = "action_url") val actionUrl: String? = null,
    @Json(name = "store_id") val storeId: String? = null,
    @Json(name = "sort_order") val sortOrder: Int = 0
)

// ── Orders ────────────────────────────────────────────────────────
@JsonClass(generateAdapter = true)
data class OrderItemInput(
    @Json(name = "product_id") val productId: String,
    @Json(name = "variant_id") val variantId: String? = null,
    val quantity: Int,
    @Json(name = "unit_price") val unitPrice: Double,
    @Json(name = "addon_ids") val addonIds: List<String>? = null
)

@JsonClass(generateAdapter = true)
data class CreateOrderRequest(
    @Json(name = "store_id") val storeId: String,
    @Json(name = "coupon_id") val couponId: String? = null,
    @Json(name = "delivery_address") val deliveryAddress: String,
    @Json(name = "delivery_latitude") val deliveryLatitude: Double,
    @Json(name = "delivery_longitude") val deliveryLongitude: Double,
    @Json(name = "payment_method") val paymentMethod: String,
    @Json(name = "special_instructions") val specialInstructions: String? = null,
    val items: List<OrderItemInput>
)

@JsonClass(generateAdapter = true)
data class OrderDto(
    val id: String,
    val status: String,
    @Json(name = "subtotal") val subtotal: Double? = null,
    @Json(name = "delivery_fee") val deliveryFee: Double? = null,
    @Json(name = "total_amount") val totalAmount: Double? = null,
    @Json(name = "created_at") val createdAt: String? = null
)

// ── User Profile ───────────────────────────────────────────────────
@JsonClass(generateAdapter = true)
data class UpdateProfileRequest(
    @Json(name = "full_name") val fullName: String? = null,
    val phone: String? = null,
    val email: String? = null
)

// ── Addresses ─────────────────────────────────────────────────────
@JsonClass(generateAdapter = true)
data class AddressDto(
    val id: String,
    val title: String,
    @Json(name = "full_address") val fullAddress: String,
    @Json(name = "building_details") val buildingDetails: String? = null,
    @Json(name = "delivery_instructions") val deliveryInstructions: String? = null,
    @Json(name = "is_default") val isDefault: Boolean = false,
    val latitude: Double? = null,
    val longitude: Double? = null,
    @Json(name = "created_at") val createdAt: String? = null
)

@JsonClass(generateAdapter = true)
data class CreateAddressRequest(
    val title: String,
    @Json(name = "full_address") val fullAddress: String,
    @Json(name = "building_details") val buildingDetails: String? = null,
    @Json(name = "delivery_instructions") val deliveryInstructions: String? = null,
    @Json(name = "is_default") val isDefault: Boolean = false,
    val latitude: Double? = null,
    val longitude: Double? = null
)
