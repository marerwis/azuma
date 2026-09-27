package com.example.data

import com.example.model.*

object SampleData {
    val categories = listOf(
        StoreCategory("restaurants", "المطاعم", "🍔", "أشهى المأكولات والمطاعم السريعة"),
        StoreCategory("groceries", "البقالة", "🛍️", "منتجات طازجة ومواد غذائية يومية"),
        StoreCategory("pharmacies", "الصيدليات", "💊", "أدوية ومستحضرات تجميل ورعاية"),
        StoreCategory("coffee", "القهوة", "☕", "بن مختص، مشروبات مثلجة وحلويات"),
        StoreCategory("bakery", "المخبوزات", "🥐", "حلويات وكرواسون ومخبوزات طازجة"),
        StoreCategory("grill", "المشاوي", "🍢", "كباب وشاورما ولحوم مشوية على الفحم"),
        StoreCategory("pizza", "البيتزا", "🍕", "بيتزا إيطالية وباستا بجميع الأشكال"),
        StoreCategory("burger", "البرجر", "🍟", "برجر كلاسيكي ومدخن مع بطاطس مقرمشة"),
        StoreCategory("gifts", "الهدايا", "🎁", "هدايا مميزة لجميع المناسبات"),
        StoreCategory("flowers", "الزهور", "💐", "باقات ورد طبيعية منسقة بعناية"),
        StoreCategory("butchery", "اللحوم", "🥩", "لحوم ودواجن طازجة محلية مذبوحة بعناية"),
        StoreCategory("fruits", "الخضار والفواكه", "🥗", "خضروات وفواكه طازجة يومياً"),
        StoreCategory("ice_cream", "المثلجات", "🍦", "آيس كريم وجيلاتو بنكهات متعددة"),
        StoreCategory("seafood", "الأسماك", "🐟", "مأكولات بحرية طازجة من شواطئ بنغازي"),
        StoreCategory("pets", "الحيوانات الأليفة", "🐾", "طعام وإكسسوارات للحيوانات الأليفة"),
        StoreCategory("shops", "المتاجر", "🛒", "أجهزة وإلكترونيات ولوازم منزلية")
    )

    val stores = listOf(
        Store(
            id = "shnabo",
            name = "شنابو - طريق المطار",
            categoryId = "restaurants",
            rating = 4.3,
            ratingCount = 103633,
            deliveryTime = "وسط التوصيل",
            deliveryFee = 6.0,
            distanceKm = "5 كم",
            isOpen = true,
            isFeatured = true,
            hasOffer = true,
            offerTitle = "عرض خاص",
            address = "طريق المطار، بنغازي",
            prepTimeMin = 20,
            logoText = "شنابو",
            tags = listOf("شاورما", "مشاوي", "ساندوتشات", "برجر")
        ),
        Store(
            id = "albaron",
            name = "مطعم البارون - الفويهات",
            categoryId = "restaurants",
            rating = 4.2,
            ratingCount = 7223,
            deliveryTime = "30 دقيقة",
            deliveryFee = 5.0,
            distanceKm = "4 كم",
            isOpen = true,
            isFeatured = true,
            hasOffer = false,
            address = "الفويهات، بنغازي",
            prepTimeMin = 25,
            logoText = "البارون",
            tags = listOf("وجبات غربية", "مشاوي", "بيتزا")
        ),
        Store(
            id = "british_doner",
            name = "بريتش دونر كباب",
            categoryId = "restaurants",
            rating = 4.4,
            ratingCount = 4890,
            deliveryTime = "25 دقيقة",
            deliveryFee = 6.0,
            distanceKm = "3.5 كم",
            isOpen = true,
            isFeatured = true,
            hasOffer = false,
            address = "حي دبي، بنغازي",
            prepTimeMin = 15,
            logoText = "Doner",
            tags = listOf("دونر", "كباب", "تركي")
        ),
        Store(
            id = "kudo",
            name = "كودو - شارع جمال",
            categoryId = "restaurants",
            rating = 4.3,
            ratingCount = 6361,
            deliveryTime = "20 دقيقة",
            deliveryFee = 5.5,
            distanceKm = "2 كم",
            isOpen = true,
            isFeatured = true,
            hasOffer = false,
            address = "شارع جمال عبدالناصر، بنغازي",
            prepTimeMin = 20,
            logoText = "KUDO",
            tags = listOf("دجاج مقلي", "ساندوتشات", "سريع")
        ),
        Store(
            id = "smokey_bbq",
            name = "سموكي جريل - طبلينو",
            categoryId = "restaurants",
            rating = 4.5,
            ratingCount = 3120,
            deliveryTime = "35 دقيقة",
            deliveryFee = 7.0,
            distanceKm = "6 كم",
            isOpen = true,
            isFeatured = false,
            hasOffer = true,
            offerTitle = "خصم 15%",
            address = "طبلينو، بنغازي",
            prepTimeMin = 30,
            logoText = "Smokey",
            tags = listOf("برجر مدخن", "أضلاع", "ستيك")
        ),
        Store(
            id = "abu_hajar",
            name = "كافي ابو حجر - شارع جمال",
            categoryId = "coffee",
            rating = 4.4,
            ratingCount = 2890,
            deliveryTime = "15 دقيقة",
            deliveryFee = 4.0,
            distanceKm = "2.2 كم",
            isOpen = true,
            isFeatured = false,
            hasOffer = false,
            address = "شارع جمال، بنغازي",
            prepTimeMin = 10,
            logoText = "أبو حجر",
            tags = listOf("قهوة", "موهيتو", "حلويات")
        ),
        Store(
            id = "alqaissar",
            name = "مطعم القيصر - الماجوري",
            categoryId = "restaurants",
            rating = 4.5,
            ratingCount = 4789,
            deliveryTime = "25 دقيقة",
            deliveryFee = 6.0,
            distanceKm = "4.8 كم",
            isOpen = true,
            isFeatured = false,
            hasOffer = true,
            offerTitle = "توصيل 6 دينار",
            address = "الماجوري، بنغازي",
            prepTimeMin = 20,
            logoText = "القيصر",
            tags = listOf("وجبات دجاج", "أرز مبوخ", "شاورما")
        ),
        Store(
            id = "robusta",
            name = "كافي روبوستا 3 - السلماني",
            categoryId = "coffee",
            rating = 4.3,
            ratingCount = 3322,
            deliveryTime = "20 دقيقة",
            deliveryFee = 3.0,
            distanceKm = "3.1 كم",
            isOpen = true,
            isFeatured = false,
            hasOffer = true,
            offerTitle = "عرض توصيل 3 دينار",
            address = "السلماني الشرقي، بنغازي",
            prepTimeMin = 15,
            logoText = "Robusta",
            tags = listOf("قهوة مختصة", "كريب", "وافل")
        ),
        Store(
            id = "brioche",
            name = "بريوش - الدقادوستا",
            categoryId = "restaurants",
            rating = 4.4,
            ratingCount = 2845,
            deliveryTime = "20 دقيقة",
            deliveryFee = 3.0,
            distanceKm = "2.8 كم",
            isOpen = true,
            isFeatured = false,
            hasOffer = true,
            offerTitle = "عرض توصيل 3 دينار",
            address = "الدقادوستا، بنغازي",
            prepTimeMin = 15,
            logoText = "بريوش",
            tags = listOf("مخبوزات", "كرواسون", "فطور")
        ),
        Store(
            id = "alshahd",
            name = "مطعم الشهد - شارع جمال عبد الناصر",
            categoryId = "restaurants",
            rating = 4.3,
            ratingCount = 4081,
            deliveryTime = "30 دقيقة",
            deliveryFee = 5.0,
            distanceKm = "2.5 كم",
            isOpen = true,
            isFeatured = false,
            hasOffer = false,
            address = "شارع جمال، بنغازي",
            prepTimeMin = 25,
            logoText = "الشهد",
            tags = listOf("مشاوي", "طواجن", "أكل شرقي")
        ),
        Store(
            id = "alruban",
            name = "مطعم الربان - شارع جمال",
            categoryId = "restaurants",
            rating = 4.3,
            ratingCount = 3575,
            deliveryTime = "30 دقيقة",
            deliveryFee = 5.0,
            distanceKm = "3.0 كم",
            isOpen = true,
            isFeatured = false,
            hasOffer = false,
            address = "شارع جمال، بنغازي",
            prepTimeMin = 25,
            logoText = "الربان",
            tags = listOf("مأكولات بحرية", "أسماك", "طواجن")
        )
    )

    val menuItems = listOf(
        MenuItem(
            id = "sh_1",
            storeId = "shnabo",
            name = "شاورما دجاج",
            description = "الدجاج مع الصوص، البطاطا المقلية والهريسة",
            price = 11.0,
            category = "شاورما",
            isPopular = true
        ),
        MenuItem(
            id = "sh_2",
            storeId = "shnabo",
            name = "شاورما دجاج اكسترا بالجبنة",
            description = "شرائح الدجاج مع الصوص، الجبنة، البطاطا المقلية والهريسة",
            price = 19.0,
            category = "شاورما",
            isPopular = true
        ),
        MenuItem(
            id = "sh_3",
            storeId = "shnabo",
            name = "شاورما لحم",
            description = "شرائح اللحم مع البقدونس، الهريسة والطحينة",
            price = 16.0,
            category = "شاورما",
            isPopular = false
        ),
        MenuItem(
            id = "sh_4",
            storeId = "shnabo",
            name = "شاورما لحم اكسترا",
            description = "شرائح اللحم مع البقدونس، الهريسة والطحينة",
            price = 25.0,
            category = "شاورما",
            isPopular = false
        ),
        MenuItem(
            id = "sh_5",
            storeId = "shnabo",
            name = "شاورما دجاج - خبز تنور",
            description = "الدجاج مع الطماطم، السلطة، البصل، صوص الريحان، صوص الثوم، صوص الساموراي، السلطة...",
            price = 15.0,
            category = "شاورما",
            isPopular = false
        ),
        MenuItem(
            id = "sh_6",
            storeId = "shnabo",
            name = "شاورما لحم - خبز تنور",
            description = "اللحم مع الطماطم، السلطة، البصل، صوص الريحان، صوص الثوم، صوص الساموراي، السلطة...",
            price = 19.0,
            category = "شاورما",
            isPopular = false
        ),
        MenuItem(
            id = "sh_7",
            storeId = "shnabo",
            name = "كباب دجاج - فطيرة",
            description = "الدجاج مع البطاطا المقلية، الهريسة، الصوص، البقدونس",
            price = 9.0,
            category = "ساندوتشات مشاوي",
            isPopular = true
        ),
        MenuItem(
            id = "sh_8",
            storeId = "shnabo",
            name = "ساندوتش كباب دجاج - دبل",
            description = "الدجاج مع البطاطا المقلية، الهريسة، الصوص، البقدونس",
            price = 15.0,
            category = "ساندوتشات مشاوي",
            isPopular = false
        ),
        MenuItem(
            id = "sh_9",
            storeId = "shnabo",
            name = "ساندوتش كباب لحم",
            description = "اللحم مع البطاطا المقلية، الهريسة، الصوص، البقدونس",
            price = 11.0,
            category = "ساندوتشات مشاوي",
            isPopular = false
        ),
        MenuItem(
            id = "sh_10",
            storeId = "shnabo",
            name = "ساندوتش كباب لحم - دبل",
            description = "اللحم مع البطاطا المقلية، الهريسة، الصوص، البقدونس",
            price = 18.0,
            category = "ساندوتشات مشاوي",
            isPopular = false
        ),
        MenuItem(
            id = "sh_11",
            storeId = "shnabo",
            name = "ساندوتش افخاذ دجاج مشوية",
            description = "افخاذ الدجاج مع الجرجير، الهريسة العربية، الطماطم، صوص الساموراي، الجبنة",
            price = 17.0,
            category = "ساندوتشات مشاوي",
            isPopular = true
        ),
        MenuItem(
            id = "sh_12",
            storeId = "shnabo",
            name = "تاكوس لافينو - تورتيلا",
            description = "لحم أو دجاج مع صوص الجبن والبطاطا بخبز التورتيلا المكرمل",
            price = 19.0,
            category = "ساندوتشات",
            isPopular = true
        ),
        MenuItem(
            id = "sh_13",
            storeId = "shnabo",
            name = "مشروب غازي",
            description = "علبة باردة من اختيارك (بيبسي، سفن اب، ميرندا)",
            price = 4.0,
            category = "ساندوتشات",
            isPopular = true
        ),
        MenuItem(
            id = "sh_14",
            storeId = "shnabo",
            name = "وجبة شاورما دجاج حجم صغير",
            description = "شرائح الشاورما مع الارز، السلطة و البطاطا المقلية",
            price = 35.0,
            category = "شاورما",
            isPopular = false
        ),
        MenuItem(
            id = "sh_15",
            storeId = "shnabo",
            name = "برجر دجاج مشوي",
            description = "صدر دجاج مشوي مع صوص الشيدر والخس والمايونيز",
            price = 14.0,
            category = "برجر",
            isPopular = true
        )
    )

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

    val quickReorderStores = listOf(
        "شنابو" to "shnabo",
        "البارون" to "albaron",
        "بريتش دونر" to "british_doner",
        "كودو" to "kudo",
        "سموكي" to "smokey_bbq"
    )

    val quickReorderItems = listOf(
        ReorderStoreItem("kfc", "كنتاكي", "https://upload.wikimedia.org/wikipedia/en/thumb/b/bf/KFC_logo.svg/300px-KFC_logo.svg.png", "4.6"),
        ReorderStoreItem("mcdonalds", "ماكدونالدز", "https://upload.wikimedia.org/wikipedia/commons/thumb/3/36/McDonald%27s_Golden_Arches.svg/300px-McDonald%27s_Golden_Arches.svg.png", "4.5"),
        ReorderStoreItem("shnabo", "شنابو", "https://images.unsplash.com/photo-1599305445671-ac291c95aaa9?w=300&auto=format&fit=crop&q=80", "4.4"),
        ReorderStoreItem("pizzahut", "بيتزا هت", "https://upload.wikimedia.org/wikipedia/en/thumb/d/d2/Pizza_Hut_logo.svg/300px-Pizza_Hut_logo.svg.png", "4.3"),
        ReorderStoreItem("burgerking", "برجر كنج", "https://upload.wikimedia.org/wikipedia/commons/thumb/c/cc/Burger_King_2020.svg/300px-Burger_King_2020.svg.png", "4.4"),
        ReorderStoreItem("hardees", "هارديز", "https://upload.wikimedia.org/wikipedia/en/thumb/e/e0/Hardee%27s_logo.svg/300px-Hardee%27s_logo.svg.png", "4.3"),
        ReorderStoreItem("starbucks", "ستاربكس", "https://upload.wikimedia.org/wikipedia/en/thumb/d/d3/Starbucks_Corporation_Logo_2011.svg/300px-Starbucks_Corporation_Logo_2011.svg.png", "4.7"),
        ReorderStoreItem("dominos", "دومينوز", "https://upload.wikimedia.org/wikipedia/commons/thumb/7/74/Dominos_pizza_logo.svg/300px-Dominos_pizza_logo.svg.png", "4.5")
    )
}

data class ReorderStoreItem(
    val id: String,
    val name: String,
    val imageUrl: String,
    val rating: String
)

