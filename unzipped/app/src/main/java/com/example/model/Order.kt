package com.example.model

enum class OrderStatus(val titleAr: String, val stepIndex: Int) {
    ACCEPTED("تم قبول الطلب", 0),
    PREPARING("قيد التحضير", 1),
    ON_THE_WAY("في الطريق إليك", 2),
    DELIVERED("تم التوصيل", 3),
    CANCELLED("ملغي", -1)
}

data class Order(
    val id: String,
    val orderNumber: String,
    val storeName: String,
    val storeAddress: String,
    val deliveryAddress: String,
    val itemsSummary: List<Pair<String, Int>>,
    val totalPrice: Double,
    val dateText: String,
    val status: OrderStatus,
    val isStore: Boolean = false, // false = restaurant, true = store/grocery
    val driverName: String = "أحمد المسماري",
    val driverPhone: String = "+218-921234567",
    val driverVehicle: String = "دراجة نارية هوندا (12-4589)",
    val driverRating: Double = 4.9,
    val estimatedArrivalMinutes: Int = 18
)
