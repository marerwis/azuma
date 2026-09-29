package com.example.data

import com.example.model.*

/**
 * Static seed data for LOCAL user-session state only.
 * (Addresses, Orders, Wallet Transactions, Notifications)
 *
 * ALL live app data (categories, stores, menu, banners) is now fetched
 * directly from Supabase and stored in UiState via AzoomaViewModel.
 * Do NOT add mutable live-data vars here.
 */
object SampleData {

    val initialAddresses = listOf(
        Address(
            id = "addr_1",
            name = "الكعب العالي",
            areaCode = "436G+585",
            cityCountry = "بنغازي، ليبيا",
            details = "بجانب مختبر الحياة، عمارة 4",
            isDefault = true,
            lat = 32.1194,
            lng = 20.0868
        ),
        Address(
            id = "addr_2",
            name = "رأس عبيدة",
            areaCode = "436G+585",
            cityCountry = "بنغازي، ليبيا",
            details = "شارع تسنيم لتنقية المياه",
            isDefault = false,
            lat = 32.1140,
            lng = 20.0750
        )
    )

    val initialOrders = listOf(
        Order(
            id = "ord_1",
            orderNumber = "26508466",
            storeName = "كافي ابو حجر - شارع جمال",
            storeAddress = "شارع جمال، بنغازي",
            deliveryAddress = "الكعب العالي",
            itemsSummary = listOf("موكا مثلجة" to 1, "قهوة اسبريسو" to 6),
            totalPrice = 40.0,
            dateText = "3 يوليو، 2026، 7:50 م",
            status = OrderStatus.DELIVERED,
            isStore = false
        ),
        Order(
            id = "ord_2",
            orderNumber = "24163250",
            storeName = "شنابو - طريق المطار",
            storeAddress = "طريق المطار، بنغازي",
            deliveryAddress = "الكعب العالي",
            itemsSummary = listOf("وجبة كباب مشوي" to 1),
            totalPrice = 30.0,
            dateText = "8 مايو، 2026، 3:56 م",
            status = OrderStatus.DELIVERED,
            isStore = false
        )
    )

    val initialWalletTransactions = listOf(
        WalletTransaction(
            id = "tx_1",
            title = "حجز قيمة 40 د.ل من محفظة الزبون للطلب رقم",
            referenceNumber = "#26508466",
            dateText = "3 يوليو، 2026، 5:50 م",
            amount = 40.0,
            isDeduction = true
        ),
        WalletTransaction(
            id = "tx_2",
            title = "شحن المحفظة |",
            referenceNumber = "#12205968",
            dateText = "3 يوليو، 2026، 5:50 م",
            amount = 40.0,
            isDeduction = false
        ),
        WalletTransaction(
            id = "tx_3",
            title = "حجز قيمة 30 د.ل من محفظة الزبون للطلب رقم",
            referenceNumber = "#24163250",
            dateText = "8 مايو، 2026، 1:56 م",
            amount = 30.0,
            isDeduction = true
        )
    )

    val initialNotifications = listOf(
        NotificationItem(
            id = "notif_1",
            title = "تم تسليم طلبك بنجاح! 🛵",
            message = "وصل طلبك من مطعم شنابو - نتمنى لك وجبة شهية.",
            timeAgo = "منذ ساعة",
            isRead = false,
            orderId = "ord_2"
        ),
        NotificationItem(
            id = "notif_2",
            title = "عرض حصري في منطقتك! 🏷️",
            message = "احصل على توصيل بـ 3 دينار فقط من بريوش وكافي روبوستا.",
            timeAgo = "اليوم",
            isRead = false
        )
    )
}
