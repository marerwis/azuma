package com.example.model

data class CartItem(
    val menuItem: MenuItem,
    var quantity: Int,
    val note: String = ""
) {
    val totalPrice: Double
        get() = menuItem.price * quantity
}

data class CartState(
    val items: List<CartItem> = emptyList(),
    val storeId: String? = null,
    val storeName: String = "",
    val storeNote: String = "",
    val deliveryNote: String = "",
    val isDelivery: Boolean = true
) {
    val subtotal: Double
        get() = items.sumOf { it.totalPrice }

    val deliveryFee: Double
        get() = if (items.isEmpty() || !isDelivery) 0.0 else 6.0

    val serviceFee: Double
        get() = if (items.isEmpty()) 0.0 else 1.25

    val grandTotal: Double
        get() = if (items.isEmpty()) 0.0 else (subtotal + deliveryFee + serviceFee)

    val totalItemCount: Int
        get() = items.sumOf { it.quantity }
}
