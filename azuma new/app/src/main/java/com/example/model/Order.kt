package com.example.model

enum class OrderStatus(val titleAr: String, val stepIndex: Int) {
    PENDING("قيد الانتظار", 0),
    ACCEPTED("تم قبول الطلب", 0),
    PREPARING("جارٍ التجهيز", 1),
    AT_RESTAURANT("في المطعم", 2),
    ON_THE_WAY("في الطريق إليك", 3),
    DELIVERED("وصلنا لك طلبك 😎", 4),
    CANCELLED("ملغي", -1)
}

enum class PickupStatus(val titleAr: String, val stepIndex: Int) {
    PENDING("قيد الانتظار", 0),
    PREPARING("جارٍ التجهيز", 1),
    READY("جاهز للاستلام 🛍️", 2),
    COMPLETED("تم الاستلام", 3)
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
    val driverRatingCount: Int = 142,
    val estimatedArrivalMinutes: Int = 18,
    val isPickup: Boolean = false,
    val pickupStatus: PickupStatus = PickupStatus.PREPARING,
    val deliveryVerificationCode: String = "4829",
    val pickupVerificationCode: String = "1294",
    val deliveryFee: Double = 3.5,
    val paidCash: Double = 0.0,
    val paidElectronic: Double = 0.0,
    val paymentMethodName: String = "نقداً عند الاستلام"
) {
    val timeText: String get() = dateText
    val subtotal: Double get() = (totalPrice - deliveryFee).coerceAtLeast(0.0)
    val pickupNumber: String get() = "#$orderNumber"
}
