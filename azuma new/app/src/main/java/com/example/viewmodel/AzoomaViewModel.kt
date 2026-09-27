package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.SampleData
import com.example.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class BottomTab {
    HOME,
    SEARCH,
    CART,
    ORDERS,
    ACCOUNT
}

enum class SubScreen {
    NONE,
    CATEGORY_DETAIL,
    STORE_PROFILE,
    CHECKOUT,
    ORDER_TRACKING,
    WALLET,
    ADDRESSES,
    NOTIFICATIONS,
    OFFERS
}

data class UiState(
    val isAuthenticated: Boolean = false,
    val showAuthSheet: Boolean = false,
    val authMode: String = "REGISTER", // "REGISTER" or "LOGIN"
    val userName: String = "مرعي زلاوي",
    val userPhone: String = "+218-914333564",
    val currentTab: BottomTab = BottomTab.HOME,
    val currentSubScreen: SubScreen = SubScreen.NONE,
    val selectedStore: Store? = null,
    val selectedCategory: StoreCategory? = null,
    val cartState: CartState = CartState(),
    val currentAddress: Address = SampleData.initialAddresses.first(),
    val addresses: List<Address> = SampleData.initialAddresses,
    val orders: List<Order> = SampleData.initialOrders,
    val activeTrackingOrder: Order? = null,
    val walletBalance: Double = 45.0,
    val walletTransactions: List<WalletTransaction> = SampleData.initialWalletTransactions,
    val selectedPaymentType: PaymentType = PaymentType.CASH,
    val searchQuery: String = "",
    val recentSearches: List<String> = listOf("كابتشينو", "شاورما دجاج", "ساندوتش كباب"),
    val favoriteStoreIds: Set<String> = setOf("shnabo", "robusta"),
    val notifications: List<NotificationItem> = SampleData.initialNotifications,
    val showLogoutDialog: Boolean = false,
    val showPaymentSheet: Boolean = false,
    val showRechargeSheet: Boolean = false,
    val showAddressPickerSheet: Boolean = false,
    val showOrderSuccessDialog: Boolean = false,
    val trackingProgress: Float = 0.65f, // driver progress along route 0.0 to 1.0
    val trackingEtaMinutes: Int = 14
)

class AzoomaViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        // Pre-populate 1 sample item in cart from Shnabo like user's screenshot
        val sampleItem = SampleData.menuItems.first { it.id == "sh_7" } // كباب دجاج - فطيرة (9 د.ل)
        _uiState.update {
            it.copy(
                cartState = CartState(
                    items = listOf(CartItem(menuItem = sampleItem, quantity = 1)),
                    storeId = "shnabo",
                    storeName = "شنابو - طريق المطار"
                ),
                selectedStore = SampleData.stores.first { s -> s.id == "shnabo" }
            )
        }

        // Live driver movement loop when tracking
        startDriverSimulation()
    }

    private fun startDriverSimulation() {
        viewModelScope.launch {
            while (true) {
                delay(3000)
                _uiState.update { state ->
                    if (state.activeTrackingOrder != null) {
                        val nextProgress = (state.trackingProgress + 0.04f).coerceAtMost(0.95f)
                        val nextEta = (state.trackingEtaMinutes - 1).coerceAtLeast(1)
                        state.copy(
                            trackingProgress = nextProgress,
                            trackingEtaMinutes = nextEta
                        )
                    } else {
                        state
                    }
                }
            }
        }
    }

    fun selectTab(tab: BottomTab) {
        _uiState.update {
            it.copy(
                currentTab = tab,
                currentSubScreen = SubScreen.NONE
            )
        }
    }

    fun openSubScreen(subScreen: SubScreen) {
        _uiState.update { it.copy(currentSubScreen = subScreen) }
    }

    fun navigateBack(): Boolean {
        val currentSub = _uiState.value.currentSubScreen
        if (currentSub != SubScreen.NONE) {
            _uiState.update { it.copy(currentSubScreen = SubScreen.NONE) }
            return true
        }
        if (_uiState.value.currentTab != BottomTab.HOME) {
            _uiState.update { it.copy(currentTab = BottomTab.HOME) }
            return true
        }
        return false
    }

    fun openStore(store: Store) {
        _uiState.update {
            it.copy(
                selectedStore = store,
                currentSubScreen = SubScreen.STORE_PROFILE
            )
        }
    }

    fun openStoreById(storeId: String) {
        val store = SampleData.stores.find { it.id == storeId } ?: SampleData.stores.first()
        openStore(store)
    }

    fun openCategory(category: StoreCategory) {
        _uiState.update {
            it.copy(
                selectedCategory = category,
                currentSubScreen = SubScreen.CATEGORY_DETAIL
            )
        }
    }

    // Cart Management
    fun addToCart(item: MenuItem, store: Store? = null) {
        val activeStore = store ?: _uiState.value.selectedStore ?: SampleData.stores.first()
        _uiState.update { state ->
            val currentCart = state.cartState
            val isNewStore = currentCart.storeId != null && currentCart.storeId != activeStore.id
            val currentItems = if (isNewStore) emptyList() else currentCart.items

            val existingIndex = currentItems.indexOfFirst { it.menuItem.id == item.id }
            val updatedItems = if (existingIndex >= 0) {
                currentItems.mapIndexed { idx, cItem ->
                    if (idx == existingIndex) cItem.copy(quantity = cItem.quantity + 1) else cItem
                }
            } else {
                currentItems + CartItem(menuItem = item, quantity = 1)
            }

            state.copy(
                cartState = currentCart.copy(
                    items = updatedItems,
                    storeId = activeStore.id,
                    storeName = activeStore.name
                )
            )
        }
    }

    fun removeFromCart(item: MenuItem) {
        _uiState.update { state ->
            val currentCart = state.cartState
            val existingIndex = currentCart.items.indexOfFirst { it.menuItem.id == item.id }
            if (existingIndex < 0) return@update state

            val existing = currentCart.items[existingIndex]
            val updatedItems = if (existing.quantity > 1) {
                currentCart.items.mapIndexed { idx, cItem ->
                    if (idx == existingIndex) cItem.copy(quantity = cItem.quantity - 1) else cItem
                }
            } else {
                currentCart.items.filterNot { it.menuItem.id == item.id }
            }

            val storeId = if (updatedItems.isEmpty()) null else currentCart.storeId
            val storeName = if (updatedItems.isEmpty()) "" else currentCart.storeName

            state.copy(
                cartState = currentCart.copy(
                    items = updatedItems,
                    storeId = storeId,
                    storeName = storeName
                )
            )
        }
    }

    fun deleteItemFromCart(item: MenuItem) {
        _uiState.update { state ->
            val currentCart = state.cartState
            val updatedItems = currentCart.items.filterNot { it.menuItem.id == item.id }
            state.copy(
                cartState = currentCart.copy(
                    items = updatedItems,
                    storeId = if (updatedItems.isEmpty()) null else currentCart.storeId,
                    storeName = if (updatedItems.isEmpty()) "" else currentCart.storeName
                )
            )
        }
    }

    fun clearCart() {
        _uiState.update { it.copy(cartState = CartState()) }
    }

    fun setStoreNote(note: String) {
        _uiState.update { it.copy(cartState = it.cartState.copy(storeNote = note)) }
    }

    fun setDeliveryNote(note: String) {
        _uiState.update { it.copy(cartState = it.cartState.copy(deliveryNote = note)) }
    }

    fun toggleDeliveryMode(isDelivery: Boolean) {
        _uiState.update { it.copy(cartState = it.cartState.copy(isDelivery = isDelivery)) }
    }

    fun setPaymentType(type: PaymentType) {
        _uiState.update { it.copy(selectedPaymentType = type, showPaymentSheet = false) }
    }

    fun showPaymentSheet(show: Boolean) {
        _uiState.update { it.copy(showPaymentSheet = show) }
    }

    fun showRechargeSheet(show: Boolean) {
        _uiState.update { it.copy(showRechargeSheet = show) }
    }

    fun showLogoutDialog(show: Boolean) {
        _uiState.update { it.copy(showLogoutDialog = show) }
    }

    fun showAddressPicker(show: Boolean) {
        _uiState.update { it.copy(showAddressPickerSheet = show) }
    }

    fun selectAddress(address: Address) {
        _uiState.update { state ->
            val updated = state.addresses.map { it.copy(isDefault = it.id == address.id) }
            state.copy(
                currentAddress = address.copy(isDefault = true),
                addresses = updated,
                showAddressPickerSheet = false
            )
        }
    }

    fun saveNewAddress(name: String, details: String) {
        val newAddr = Address(
            id = "addr_${System.currentTimeMillis()}",
            name = name.ifBlank { "عنوان جديد" },
            areaCode = "436G+585",
            cityCountry = "بنغازي، ليبيا",
            details = details,
            isDefault = true
        )
        _uiState.update { state ->
            val updated = state.addresses.map { it.copy(isDefault = false) } + newAddr
            state.copy(
                addresses = updated,
                currentAddress = newAddr,
                showAddressPickerSheet = false
            )
        }
    }

    fun deleteAddress(id: String) {
        _uiState.update { state ->
            val remaining = state.addresses.filterNot { it.id == id }
            val nextDefault = remaining.firstOrNull() ?: Address("addr_0", "الرئيسي", "436G+585", "بنغازي، ليبيا", isDefault = true)
            state.copy(
                addresses = remaining,
                currentAddress = if (state.currentAddress.id == id) nextDefault else state.currentAddress
            )
        }
    }

    // Confirm Order
    fun confirmOrder() {
        val cart = _uiState.value.cartState
        if (cart.items.isEmpty()) return

        val orderNum = (10000000..99999999).random().toString()
        val total = cart.grandTotal

        // Deduct from wallet if wallet payment
        if (_uiState.value.selectedPaymentType == PaymentType.WALLET) {
            val newBalance = (_uiState.value.walletBalance - total).coerceAtLeast(0.0)
            val newTx = WalletTransaction(
                id = "tx_${System.currentTimeMillis()}",
                title = "حجز قيمة ${total.toInt()} د.ل من محفظة الزبون للطلب رقم",
                referenceNumber = "#$orderNum",
                dateText = "الآن",
                amount = total,
                isDeduction = true
            )
            _uiState.update {
                it.copy(
                    walletBalance = newBalance,
                    walletTransactions = listOf(newTx) + it.walletTransactions
                )
            }
        }

        val newOrder = Order(
            id = "ord_${System.currentTimeMillis()}",
            orderNumber = orderNum,
            storeName = cart.storeName.ifBlank { "شنابو - طريق المطار" },
            storeAddress = "طريق المطار، بنغازي",
            deliveryAddress = _uiState.value.currentAddress.name,
            itemsSummary = cart.items.map { it.menuItem.name to it.quantity },
            totalPrice = total,
            dateText = "اليوم، الآن",
            status = OrderStatus.ON_THE_WAY,
            isStore = false,
            estimatedArrivalMinutes = 18
        )

        val newNotif = NotificationItem(
            id = "notif_${System.currentTimeMillis()}",
            title = "تم تأكيد طلبك بنجاح! 🛵",
            message = "طلبك رقم #${orderNum} قيد التحضير في ${newOrder.storeName}.",
            timeAgo = "الآن",
            isRead = false,
            orderId = newOrder.id
        )

        _uiState.update {
            it.copy(
                orders = listOf(newOrder) + it.orders,
                activeTrackingOrder = newOrder,
                cartState = CartState(),
                notifications = listOf(newNotif) + it.notifications,
                currentSubScreen = SubScreen.ORDER_TRACKING,
                trackingProgress = 0.25f,
                trackingEtaMinutes = 18
            )
        }
    }

    fun rechargeWallet(amount: Double, method: String) {
        val newTx = WalletTransaction(
            id = "tx_${System.currentTimeMillis()}",
            title = "شحن المحفظة عبر $method |",
            referenceNumber = "#${(10000000..99999999).random()}",
            dateText = "اليوم",
            amount = amount,
            isDeduction = false
        )
        _uiState.update {
            it.copy(
                walletBalance = it.walletBalance + amount,
                walletTransactions = listOf(newTx) + it.walletTransactions,
                showRechargeSheet = false
            )
        }
    }

    fun toggleFavorite(storeId: String) {
        _uiState.update { state ->
            val set = state.favoriteStoreIds.toMutableSet()
            if (set.contains(storeId)) set.remove(storeId) else set.add(storeId)
            state.copy(favoriteStoreIds = set)
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun addRecentSearch(query: String) {
        if (query.isBlank()) return
        _uiState.update { state ->
            val updated = (listOf(query) + state.recentSearches.filterNot { it == query }).take(8)
            state.copy(recentSearches = updated)
        }
    }

    fun removeRecentSearch(query: String) {
        _uiState.update { state ->
            state.copy(recentSearches = state.recentSearches.filterNot { it == query })
        }
    }

    fun trackOrder(order: Order) {
        _uiState.update {
            it.copy(
                activeTrackingOrder = order,
                currentSubScreen = SubScreen.ORDER_TRACKING
            )
        }
    }

    fun reorder(order: Order) {
        // Find store and add sample items
        val store = SampleData.stores.find { it.name.contains(order.storeName) } ?: SampleData.stores.first()
        val itemsToAdd = SampleData.menuItems.take(2)
        _uiState.update { state ->
            state.copy(
                cartState = CartState(
                    items = itemsToAdd.map { CartItem(it, 1) },
                    storeId = store.id,
                    storeName = store.name
                ),
                currentTab = BottomTab.CART,
                currentSubScreen = SubScreen.NONE
            )
        }
    }

    // Authentication methods
    fun startRegister() {
        _uiState.update { it.copy(showAuthSheet = true, authMode = "REGISTER") }
    }

    fun startLogin() {
        _uiState.update { it.copy(showAuthSheet = true, authMode = "LOGIN") }
    }

    fun dismissAuthSheet() {
        _uiState.update { it.copy(showAuthSheet = false) }
    }

    fun authenticateUser(name: String, phone: String) {
        _uiState.update {
            it.copy(
                isAuthenticated = true,
                showAuthSheet = false,
                userName = name.ifBlank { "مرعي زلاوي" },
                userPhone = phone.ifBlank { "+218-914333564" },
                currentTab = BottomTab.HOME,
                currentSubScreen = SubScreen.NONE
            )
        }
    }

    fun continueAsGuest() {
        _uiState.update {
            it.copy(
                isAuthenticated = true,
                showAuthSheet = false,
                userName = "زائر كريم",
                userPhone = "+218-9XXXXXXXX",
                currentTab = BottomTab.HOME,
                currentSubScreen = SubScreen.NONE
            )
        }
    }

    fun logout() {
        _uiState.update {
            it.copy(
                isAuthenticated = false,
                showLogoutDialog = false,
                currentTab = BottomTab.HOME,
                currentSubScreen = SubScreen.NONE
            )
        }
    }
}
