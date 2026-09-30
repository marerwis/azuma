package com.example.viewmodel

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.SampleData
import com.example.data.SessionManager
import com.example.data.api.RetrofitClient
import com.example.data.repository.ApiResult
import com.example.data.repository.AzoomaRepository
import com.example.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.data.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.realtime.realtime
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.PostgresAction
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.launchIn
import android.app.Application

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
    // ── Loading ──────────────────────────────────────────────────────────────
    val isLoadingData: Boolean = true,

    // ── Live API Data ─────────────────────────────────────────────────────────
    val appCategories: List<AppCategory> = emptyList(),
    val stores: List<Store> = emptyList(),
    val menuCategories: List<MenuCategory> = emptyList(),
    val menuItems: List<MenuItem> = emptyList(),
    val appBanners: List<AppBanner> = emptyList(),

    // ── Selected Store Nested Menu (fetched on demand) ────────────────────────
    val isLoadingStoreMenu: Boolean = false,
    val selectedStoreMenu: List<MenuCategoryWithProducts> = emptyList(),

    // ── Authentication ────────────────────────────────────────────────────────
    val isSessionLoading: Boolean = true,
    val isAuthenticated: Boolean = false,
    val showAuthSheet: Boolean = false,
    val authMode: String = "REGISTER",
    val isAuthLoading: Boolean = false,      // spinner during verify call
    val authError: String? = null,           // error message to show user
    val userSession: UserSession? = null,    // set after successful verify
    // Legacy display fields kept for UI compatibility
    val userName: String = "مرعي زلاوي",
    val userPhone: String = "+218-914333564",

    // ── Navigation ────────────────────────────────────────────────────────────
    val currentTab: BottomTab = BottomTab.HOME,
    val currentSubScreen: SubScreen = SubScreen.NONE,
    val selectedStore: Store? = null,
    val selectedCategory: AppCategory? = null,

    // ── Cart ──────────────────────────────────────────────────────────────────
    val cartState: CartState = CartState(),

    // ── User Session Data (local-only) ────────────────────────────────────────
    val currentAddress: Address = SampleData.initialAddresses.first(),
    val addresses: List<Address> = SampleData.initialAddresses,
    val orders: List<Order> = SampleData.initialOrders,
    val activeTrackingOrder: Order? = null,
    val walletBalance: Double = 45.0,
    val walletTransactions: List<WalletTransaction> = SampleData.initialWalletTransactions,
    val selectedPaymentType: PaymentType = PaymentType.CASH,
    val searchQuery: String = "",
    val recentSearches: List<String> = listOf("كابتشينو", "شاورما دجاج", "ساندوتش كباب"),
    val favoriteStoreIds: Set<String> = setOf(),
    val notifications: List<NotificationItem> = SampleData.initialNotifications,
    val showLogoutDialog: Boolean = false,
    val showPaymentSheet: Boolean = false,
    val showRechargeSheet: Boolean = false,
    val showAddressPickerSheet: Boolean = false,
    val showOrderSuccessDialog: Boolean = false,
    val trackingProgress: Float = 0.65f,
    val trackingEtaMinutes: Int = 14
)

class AzoomaViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val sessionManager = SessionManager(application.applicationContext)

    // Token provider — set from MainActivity after Firebase Auth gives us the token
    private var _idToken: String? = null
    private val repository = AzoomaRepository(
        RetrofitClient.create(tokenProvider = { _idToken })
    )

    /** Called from MainActivity once Firebase delivers the current user's ID token. */
    fun setFirebaseIdToken(token: String?) {
        _idToken = token
    }

    init {
        startDriverSimulation()
        restoreSession()       // ← restore persisted session BEFORE fetching data
        fetchInitialData()
    }

    /** Restores a previously saved session from DataStore so users stay logged in. */
    private fun restoreSession() {
        viewModelScope.launch {
            try {
                val savedSession = sessionManager.sessionFlow.first()
                val savedToken   = sessionManager.firebaseTokenFlow.first()
                if (savedSession != null && savedToken != null) {
                    _idToken = savedToken
                    // Re-inject Supabase JWT for Realtime if it was saved
                    savedSession.supabaseToken?.let { token ->
                        try { supabase.auth.importAuthToken(token) } catch (_: Exception) { }
                    }
                    _uiState.update {
                        it.copy(
                            isAuthenticated = true,
                            userSession     = savedSession,
                            userName        = savedSession.fullName.ifBlank { "مستخدم" },
                            userPhone       = savedSession.phone ?: "",
                            isSessionLoading = false
                        )
                    }
                } else {
                    _uiState.update { it.copy(isSessionLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSessionLoading = false) }
                android.util.Log.e("ViewModel", "Failed to restore session", e)
            }
        }
    }

    // ── Data Fetching ─────────────────────────────────────────────────────────

    private fun fetchInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingData = true) }

            // Run all three in parallel using separate coroutines, collect results
            val categoriesResult = repository.getCategories()
            val storesResult = repository.getStores()
            val bannersResult = repository.getBanners()

            _uiState.update { state ->
                state.copy(
                    isLoadingData = false,
                    appCategories = when (categoriesResult) {
                        is ApiResult.Success -> categoriesResult.data
                        is ApiResult.Error -> {
                            android.util.Log.e("ViewModel", "Categories error: ${categoriesResult.message}")
                            state.appCategories
                        }
                    },
                    stores = when (storesResult) {
                        is ApiResult.Success -> storesResult.data
                        is ApiResult.Error -> {
                            android.util.Log.e("ViewModel", "Stores error: ${storesResult.message}")
                            state.stores
                        }
                    },
                    appBanners = when (bannersResult) {
                        is ApiResult.Success -> bannersResult.data
                        is ApiResult.Error -> state.appBanners
                    }
                )
            }
        }
    }

    private fun fetchStoreMenu(storeId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingStoreMenu = true) }
            when (val result = repository.getStoreMenu(storeId)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(isLoadingStoreMenu = false, selectedStoreMenu = result.data)
                }
                is ApiResult.Error -> {
                    android.util.Log.e("ViewModel", "StoreMenu error: ${result.message}")
                    _uiState.update { it.copy(isLoadingStoreMenu = false) }
                }
            }
        }
    }

    // ── Auth ──────────────────────────────────────────────────────────────────

    /**
     * Called after Firebase Phone Auth completes and we have a valid ID token.
     * Sends the token to our Node.js API, syncs the user in Supabase, and
     * updates UI state with the returned user session.
     */
    fun verifyWithApi(idToken: String, fullName: String? = null, fcmToken: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAuthLoading = true, authError = null) }
            _idToken = idToken // store for future authenticated requests

            when (val result = repository.verifyFirebaseToken(idToken, fcmToken, fullName)) {
                is ApiResult.Success -> {
                    val session = result.data
                    
                    // Inject the custom backend JWT into Supabase so Realtime RLS works
                    session.supabaseToken?.let { token ->
                        try {
                            supabase.auth.importAuthToken(token)
                            supabase.realtime.connect()
                        } catch (e: Exception) {
                            android.util.Log.e("ViewModel", "Failed to authenticate Supabase", e)
                        }
                    }

                    // ── Persist session to DataStore so user stays logged in ──
                    try { sessionManager.save(session, idToken) } catch (e: Exception) {
                        android.util.Log.e("ViewModel", "Failed to persist session", e)
                    }

                    _uiState.update {
                        it.copy(
                            isAuthLoading = false,
                            isAuthenticated = true,
                            showAuthSheet = false,
                            userSession = session,
                            userName = session.fullName.ifBlank { fullName ?: "مستخدم" },
                            userPhone = session.phone ?: "",
                            currentTab = BottomTab.HOME,
                            currentSubScreen = SubScreen.NONE
                        )
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update {
                        it.copy(isAuthLoading = false, authError = result.message)
                    }
                }
            }
        }
    }

    // ── Driver Simulation ─────────────────────────────────────────────────────

    private fun startDriverSimulation() {
        viewModelScope.launch {
            while (true) {
                delay(3000)
                _uiState.update { state ->
                    if (state.activeTrackingOrder != null) {
                        val nextProgress = (state.trackingProgress + 0.04f).coerceAtMost(0.95f)
                        val nextEta = (state.trackingEtaMinutes - 1).coerceAtLeast(1)
                        state.copy(trackingProgress = nextProgress, trackingEtaMinutes = nextEta)
                    } else state
                }
            }
        }
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    fun selectTab(tab: BottomTab) {
        _uiState.update { it.copy(currentTab = tab, currentSubScreen = SubScreen.NONE) }
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
                selectedStoreMenu = emptyList(),
                currentSubScreen = SubScreen.STORE_PROFILE
            )
        }
        fetchStoreMenu(store.id)
    }

    fun openStoreById(storeId: String) {
        val store = _uiState.value.stores.find { it.id == storeId } ?: return
        openStore(store)
    }

    fun openCategory(category: AppCategory) {
        _uiState.update { it.copy(selectedCategory = category, currentSubScreen = SubScreen.CATEGORY_DETAIL) }
    }

    // ── Cart Management ───────────────────────────────────────────────────────

    fun addToCart(item: MenuItem, store: Store? = null) {
        val activeStore = store ?: _uiState.value.selectedStore ?: return
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
                    storeName = activeStore.name,
                    storeCommissionRate = activeStore.actualCommissionRate
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
            state.copy(
                cartState = currentCart.copy(
                    items = updatedItems,
                    storeId = if (updatedItems.isEmpty()) null else currentCart.storeId,
                    storeName = if (updatedItems.isEmpty()) "" else currentCart.storeName
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

    fun clearCart() { _uiState.update { it.copy(cartState = CartState()) } }
    fun setStoreNote(note: String) { _uiState.update { it.copy(cartState = it.cartState.copy(storeNote = note)) } }
    fun setDeliveryNote(note: String) { _uiState.update { it.copy(cartState = it.cartState.copy(deliveryNote = note)) } }
    fun toggleDeliveryMode(isDelivery: Boolean) { _uiState.update { it.copy(cartState = it.cartState.copy(isDelivery = isDelivery)) } }
    fun setPaymentType(type: PaymentType) { _uiState.update { it.copy(selectedPaymentType = type, showPaymentSheet = false) } }
    fun showPaymentSheet(show: Boolean) { _uiState.update { it.copy(showPaymentSheet = show) } }
    fun showRechargeSheet(show: Boolean) { _uiState.update { it.copy(showRechargeSheet = show) } }
    fun showLogoutDialog(show: Boolean) { _uiState.update { it.copy(showLogoutDialog = show) } }
    fun showAddressPicker(show: Boolean) { _uiState.update { it.copy(showAddressPickerSheet = show) } }

    fun selectAddress(address: Address) {
        _uiState.update { state ->
            val updated = state.addresses.map { it.copy(isDefault = it.id == address.id) }
            state.copy(currentAddress = address.copy(isDefault = true), addresses = updated, showAddressPickerSheet = false)
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
            state.copy(addresses = updated, currentAddress = newAddr, showAddressPickerSheet = false)
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

    // ── Order Placement (local for now) ──────────────────────────────────────

    fun confirmOrder() {
        val cart = _uiState.value.cartState
        if (cart.items.isEmpty()) return

        val orderNum = (10000000..99999999).random().toString()
        val total = cart.grandTotal

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
                it.copy(walletBalance = newBalance, walletTransactions = listOf(newTx) + it.walletTransactions)
            }
        }

        val newOrder = Order(
            id = "ord_${System.currentTimeMillis()}",
            orderNumber = orderNum,
            storeName = cart.storeName.ifBlank { "المطعم" },
            storeAddress = "بنغازي",
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

    // ── Wallet ────────────────────────────────────────────────────────────────

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
            it.copy(walletBalance = it.walletBalance + amount, walletTransactions = listOf(newTx) + it.walletTransactions, showRechargeSheet = false)
        }
    }

    // ── Misc UI ───────────────────────────────────────────────────────────────

    fun toggleFavorite(storeId: String) {
        _uiState.update { state ->
            val set = state.favoriteStoreIds.toMutableSet()
            if (set.contains(storeId)) set.remove(storeId) else set.add(storeId)
            state.copy(favoriteStoreIds = set)
        }
    }

    fun updateSearchQuery(query: String) { _uiState.update { it.copy(searchQuery = query) } }

    fun addRecentSearch(query: String) {
        if (query.isBlank()) return
        _uiState.update { state ->
            val updated = (listOf(query) + state.recentSearches.filterNot { it == query }).take(8)
            state.copy(recentSearches = updated)
        }
    }

    fun removeRecentSearch(query: String) {
        _uiState.update { state -> state.copy(recentSearches = state.recentSearches.filterNot { it == query }) }
    }

    fun trackOrder(order: Order) {
        _uiState.update { it.copy(activeTrackingOrder = order, currentSubScreen = SubScreen.ORDER_TRACKING) }
        
        // Only subscribe if we are authenticated with Supabase
        if (_uiState.value.userSession?.supabaseToken != null) {
            subscribeToOrderUpdates(order.id)
        }
    }

    private fun subscribeToOrderUpdates(orderId: String) {
        val channel = supabase.channel("order-$orderId")
        val cleanOrderId = orderId.replace("ord_", "") // If orderId matches the UUID in DB
        
        channel.postgresChangeFlow<PostgresAction.Update>(schema = "public") {
            table = "orders"
            filter("id", io.github.jan.supabase.postgrest.query.filter.FilterOperator.EQ, cleanOrderId)
        }.onEach { change ->
            val newStatus = change.record["status"]?.toString() ?: return@onEach
            val mappedStatus = when (newStatus) {
                "accepted" -> OrderStatus.ACCEPTED
                "preparing" -> OrderStatus.PREPARING
                "on_the_way" -> OrderStatus.ON_THE_WAY
                "delivered" -> OrderStatus.DELIVERED
                "cancelled" -> OrderStatus.CANCELLED
                else -> OrderStatus.ACCEPTED
            }
            
            _uiState.update { state ->
                val activeOrder = state.activeTrackingOrder
                if (activeOrder != null && activeOrder.id == orderId) {
                    val newProgress = when (mappedStatus) {
                        OrderStatus.ACCEPTED -> 0.1f
                        OrderStatus.PREPARING -> 0.3f
                        OrderStatus.ON_THE_WAY -> 0.8f
                        OrderStatus.DELIVERED -> 1.0f
                        OrderStatus.CANCELLED -> 0.0f
                    }
                    state.copy(
                        activeTrackingOrder = activeOrder.copy(status = mappedStatus),
                        trackingProgress = newProgress,
                        trackingEtaMinutes = if (mappedStatus == OrderStatus.ON_THE_WAY) 10 else state.trackingEtaMinutes
                    )
                } else state
            }
        }.launchIn(viewModelScope)

        viewModelScope.launch {
            try {
                channel.subscribe()
                android.util.Log.d("Realtime", "Successfully subscribed to order: $orderId")
            } catch (e: Exception) {
                android.util.Log.e("Realtime", "Failed to subscribe", e)
            }
        }
    }

    fun reorder(order: Order) {
        val store = _uiState.value.stores.find { it.name.contains(order.storeName) }
        if (store != null) {
            _uiState.update { state ->
                state.copy(selectedStore = store, currentSubScreen = SubScreen.STORE_PROFILE, currentTab = BottomTab.HOME)
            }
        }
    }

    // ── Auth sheet helpers ────────────────────────────────────────────────────

    fun startRegister() { _uiState.update { it.copy(showAuthSheet = true, authMode = "REGISTER") } }
    fun startLogin() { _uiState.update { it.copy(showAuthSheet = true, authMode = "LOGIN") } }
    fun dismissAuthSheet() { _uiState.update { it.copy(showAuthSheet = false, authError = null) } }

    /** Legacy path — used when Firebase Auth is not yet wired up in the UI */
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
        _idToken = null
        viewModelScope.launch {
            try { sessionManager.clear() } catch (e: Exception) {
                android.util.Log.e("ViewModel", "Failed to clear session", e)
            }
        }
        _uiState.update {
            it.copy(
                isAuthenticated = false,
                showLogoutDialog = false,
                userSession = null,
                currentTab = BottomTab.HOME,
                currentSubScreen = SubScreen.NONE,
                isSessionLoading = false
            )
        }
    }
}
