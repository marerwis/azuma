package com.example.model

data class Address(
    val id: String,
    val name: String, // e.g. "الكعب العالي", "رأس عبيدة"
    val areaCode: String = "436G+585",
    val cityCountry: String = "بنغازي، ليبيا",
    val details: String = "",
    val isDefault: Boolean = false,
    val lat: Double? = null,
    val lng: Double? = null
)

data class WalletTransaction(
    val id: String,
    val title: String,
    val referenceNumber: String,
    val dateText: String,
    val amount: Double,
    val isDeduction: Boolean // true: deduction (-), false: recharge (+)
)

enum class PaymentType(val id: String, val titleAr: String, val subText: String? = null) {
    CASH("cash", "نقداً", null),
    WALLET("wallet", "محفظة", "رصيد فوري"),
    LIBYANA("libyana", "ليبيانا", "الدفع عبر الرصيد"),
    SADAD("sadad", "سداد", "خدمة سداد للمدفوعات"),
    BANK_CARD("bank_card", "البطاقة المصرفية (اونلاين)", "فيزا / ماستركارد"),
    EDFA3LY("edfa3ly", "ادفعلي", "بوابة ادفعلي الإلكترونية"),
    MOBI_CASH("mobi_cash", "موبي كاش", "مصرف الوحدة"),
    MASRAFI_PAY("masrafi_pay", "مصرفي باي", "مصرف الجمهورية"),
    YUSR_ONLINE("yusr_online", "يسر اونلاين", "مصرف التجارة والتنمية")
}

data class NotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val timeAgo: String,
    val isRead: Boolean = false,
    val orderId: String? = null
)
