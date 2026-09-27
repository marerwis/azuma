package com.example.model

data class StoreCategory(
    val id: String,
    val name: String,
    val iconEmoji: String,
    val description: String = ""
)

data class Store(
    val id: String,
    val name: String,
    val categoryId: String,
    val rating: Double,
    val ratingCount: Int,
    val deliveryTime: String,
    val deliveryFee: Double,
    val distanceKm: String,
    val isOpen: Boolean = true,
    val isFeatured: Boolean = false,
    val hasOffer: Boolean = false,
    val offerTitle: String? = null,
    val address: String = "طريق المطار، بنغازي",
    val prepTimeMin: Int = 20,
    val logoText: String = "S",
    val tags: List<String> = listOf("سريع", "وجبات", "مشويات"),
    val pickupAvailable: Boolean = true
)

data class MenuItem(
    val id: String,
    val storeId: String,
    val name: String,
    val description: String,
    val price: Double,
    val category: String,
    val isPopular: Boolean = false,
    val discountPrice: Double? = null
)
