package com.example.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AppBanner(
    val id: String,
    @SerialName("store_id")
    val storeId: String? = null,
    @SerialName("image_url")
    val imageUrl: String,
    @SerialName("action_url")
    val actionUrl: String? = null,
    @SerialName("is_active")
    val isActive: Boolean = true,
    @SerialName("sort_order")
    val sortOrder: Int = 0
) {
    val absoluteImageUrl: String
        get() {
            if (imageUrl.isBlank()) return ""
            if (imageUrl.startsWith("http")) return imageUrl
            return com.example.data.BASE_IMAGE_URL + "/" + imageUrl.trimStart('/')
        }
}

// ─────────────────────────────────────────────────────────────────────────────
// AppCategory — maps strictly to the `app_categories` table.
// Platform-wide Home Screen tiles (Restaurants, Pharmacies, etc.)
// ─────────────────────────────────────────────────────────────────────────────
@Serializable
data class AppCategory(
    val id: String,
    val name: String,
    @SerialName("image_url")
    val imageUrl: String = "",
    @SerialName("is_active")
    val isActive: Boolean = true,
    @SerialName("sort_order")
    val sortOrder: Int = 0
) {
    val absoluteImageUrl: String
        get() {
            if (imageUrl.isBlank()) return ""
            if (imageUrl.startsWith("http")) return imageUrl
            return com.example.data.BASE_IMAGE_URL + "/" + imageUrl.trimStart('/')
        }
}

// ─────────────────────────────────────────────────────────────────────────────
// MenuCategory — maps to `menu_categories`.
// Store-specific menu sections (e.g. "شاورما", "ساندوتشات").
// store_id is NOT NULL — a MenuCategory always belongs to exactly one store.
// ─────────────────────────────────────────────────────────────────────────────
@Serializable
data class MenuCategory(
    val id: String,
    @SerialName("store_id")
    val storeId: String,
    val name: String,
    @SerialName("is_active")
    val isActive: Boolean = true,
    @SerialName("sort_order")
    val sortOrder: Int = 0
)

// ─────────────────────────────────────────────────────────────────────────────
// MenuCategoryWithProducts — nested model used for the hierarchical Supabase
// query: menu_categories(*, products(*))
// Gives us the exact structure: Category → [Products] for StoreProfileScreen.
// ─────────────────────────────────────────────────────────────────────────────
@Serializable
data class MenuCategoryWithProducts(
    val id: String,
    @SerialName("store_id")
    val storeId: String,
    val name: String,
    @SerialName("is_active")
    val isActive: Boolean = true,
    @SerialName("sort_order")
    val sortOrder: Int = 0,
    // Nested products belonging to this category
    val products: List<MenuItem> = emptyList()
)

// ─────────────────────────────────────────────────────────────────────────────
// StoreWithMenu — nested model used for the full store query:
// stores(*, menu_categories(*, products(*)))
// ─────────────────────────────────────────────────────────────────────────────
@Serializable
data class StoreWithMenu(
    val id: String,
    val name: String,
    @SerialName("image_url")
    val logoUrl: String = "",
    @SerialName("cover_url")
    val coverUrl: String = "",
    @SerialName("description")
    val description: String = "",
    @SerialName("address")
    val address: String = "بنغازي",
    @SerialName("is_open")
    val isOpen: Boolean = true,
    @SerialName("is_active")
    val isActive: Boolean = true,
    @SerialName("delivery_radius_km")
    val deliveryRadiusKm: Double = 5.0,
    @SerialName("commission_rate")
    val commissionRate: Double = 5.0,
    // Nested menu categories with their products
    @SerialName("menu_categories")
    val menuCategories: List<MenuCategoryWithProducts> = emptyList()
)

// ─────────────────────────────────────────────────────────────────────────────
// Store — flat model for the stores list (Home Screen, Search, etc.)
// ─────────────────────────────────────────────────────────────────────────────
@Serializable
data class Store(
    val id: String,
    val name: String,
    @SerialName("vendor_id")
    val categoryId: String = "restaurants",
    val rating: Double = 4.0,
    val ratingCount: Int = 100,
    @SerialName("closing_time")
    val deliveryTime: String = "30 دقيقة",
    @SerialName("commission_rate")
    val deliveryFee: Double = 5.0,
    @SerialName("address")
    val distanceKm: String = "5 كم",
    @SerialName("is_open")
    val isOpen: Boolean = true,
    @SerialName("is_active")
    val isFeatured: Boolean = false,
    val hasOffer: Boolean = false,
    val offerTitle: String? = null,
    @SerialName("description")
    val address: String = "بنغازي",
    val prepTimeMin: Int = 20,
    @SerialName("image_url")
    val logoText: String = "",
    val tags: List<String> = listOf("سريع"),
    val pickupAvailable: Boolean = true
) {
    val absoluteLogoUrl: String
        get() {
            if (logoText.isBlank()) return ""
            if (logoText.startsWith("http")) return logoText
            return com.example.data.BASE_IMAGE_URL + "/" + logoText.trimStart('/')
        }
}

// ─────────────────────────────────────────────────────────────────────────────
// MenuItem — maps to the `products` table.
// menuCategoryId references menu_categories.id (the correct FK).
// ─────────────────────────────────────────────────────────────────────────────
@Serializable
data class MenuItem(
    val id: String,
    @SerialName("store_id")
    val storeId: String,
    val name: String,
    val description: String = "",
    @SerialName("base_price")
    val price: Double,
    // The correct FK — references menu_categories.id
    @SerialName("menu_category_id")
    val menuCategoryId: String? = null,
    // Legacy fallback for rows not yet migrated
    @SerialName("category_id")
    val legacyCategoryId: String? = null,
    @SerialName("is_active")
    val isPopular: Boolean = false,
    val discountPrice: Double? = null,
    @SerialName("image_url")
    val imageUrl: String = ""
) {
    val absoluteImageUrl: String
        get() {
            if (imageUrl.isBlank()) return ""
            if (imageUrl.startsWith("http")) return imageUrl
            return com.example.data.BASE_IMAGE_URL + "/" + imageUrl.trimStart('/')
        }

    /** Returns the effective category ID regardless of which column is populated. */
    val effectiveCategoryId: String?
        get() = menuCategoryId ?: legacyCategoryId
}
