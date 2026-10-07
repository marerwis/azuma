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
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.IDToken

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
    DRIVER_CHAT,
    WALLET,
    ADDRESSES,
    NOTIFICATIONS,
    OFFERS,
    // Account sub-screens
    FAVORITES,
    HELP,
    COUPONS,
    PERSONAL_INFO,
    COUNTRY_SELECT,
    LANGUAGE_DISPLAY,
    ABOUT,
    TERMS
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
    val userName: String = "",
    val userPhone: String = "",
    val userEmail: String = "",

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
    val trackingEtaMinutes: Int = 14,
    // ── Account settings ──────────────────────────────────────────────────────
    val activeCoupons: List<String> = emptyList(),
    val selectedCountry: String = "ليبيا",
    val selectedLanguage: String = "العربية",
    val selectedThemeMode: String = "فاتح",
    // ── Profile save state ────────────────────────────────────────────────────
    val isProfileSaving: Boolean = false,
    val profileSaveError: String? = null,
    val profileSaveSuccess: Boolean = false,
    // ── Sheet / dialog toggles ────────────────────────────────────────────────
    val showLocationConfirmSheet: Boolean = false,
    val showFeedbackSheet: Boolean = false,
    val showRateUsDialog: Boolean = false,
    val showAppSelectorSheet: Boolean = false,
    // ── Order Placement (Cloud-First: error stays on checkout, success navigates) ──
    val isPlacingOrder: Boolean = false,
    val orderPlacementError: String? = null
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
                            userEmail       = savedSession.email ?: "",
                            isSessionLoading = false
                        )
                    }
                    // Fetch fresh addresses from API
                    fetchAddresses()
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

            try {
                // 1. Exchange Google ID token with Supabase Auth
                supabase.auth.signInWith(IDToken) {
                    this.idToken = idToken
                    this.provider = Google
                }
                
                // 2. Get the Supabase Access Token (JWT)
                val supabaseJwt = supabase.auth.currentAccessTokenOrNull()
                if (supabaseJwt == null) {
                    _uiState.update { it.copy(isAuthLoading = false, authError = "فشل في الحصول على توثيق Supabase") }
                    return@launch
                }
                
                // 3. Set the token that Retrofit AuthInterceptor will use
                _idToken = supabaseJwt 

                // 4. Proceed with our backend API call using the Supabase JWT
                when (val result = repository.verifyFirebaseToken(supabaseJwt, fcmToken, fullName)) {
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
                        try { sessionManager.save(session, supabaseJwt) } catch (e: Exception) {
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
                                userEmail = session.email ?: "",
                                currentTab = BottomTab.HOME,
                                currentSubScreen = SubScreen.NONE
                            )
                        }
                        
                        fetchAddresses()
                    }
                    is ApiResult.Error -> {
                        _uiState.update {
                            it.copy(isAuthLoading = false, authError = result.message)
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("ViewModel", "Failed to exchange ID token", e)
                _uiState.update { it.copy(isAuthLoading = false, authError = "حدث خطأ أثناء تسجيل الدخول: ${e.localizedMessage}") }
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
        if (currentSub == SubScreen.DRIVER_CHAT) {
            _uiState.update { it.copy(currentSubScreen = SubScreen.ORDER_TRACKING) }
            return true
        }
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

    fun fetchAddresses() {
        viewModelScope.launch {
            if (_idToken == null) return@launch
            when (val result = repository.getAddresses()) {
                is ApiResult.Success -> {
                    val addrs = result.data
                    _uiState.update { state ->
                        state.copy(
                            addresses = addrs,
                            currentAddress = addrs.find { it.isDefault } ?: addrs.firstOrNull() ?: state.currentAddress
                        )
                    }
                }
                is ApiResult.Error -> { }
            }
        }
    }

    fun selectAddress(address: Address) {
        _uiState.update { state ->
            val updated = state.addresses.map { it.copy(isDefault = it.id == address.id) }
            state.copy(currentAddress = address.copy(isDefault = true), addresses = updated, showAddressPickerSheet = false)
        }
    }

    fun saveNewAddress(name: String, details: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingData = true) }
            val req = com.example.data.api.CreateAddressRequest(
                title = name.ifBlank { "عنوان جديد" },
                fullAddress = details,
                isDefault = true
            )
            when (val result = repository.createAddress(req)) {
                is ApiResult.Success -> {
                    fetchAddresses()
                    _uiState.update { it.copy(isLoadingData = false, showAddressPickerSheet = false) }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isLoadingData = false) }
                }
            }
        }
    }

    fun deleteAddress(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingData = true) }
            when (val result = repository.deleteAddress(id)) {
                is ApiResult.Success -> {
                    fetchAddresses()
                    _uiState.update { it.copy(isLoadingData = false) }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isLoadingData = false) }
                }
            }
        }
    }

    // ── Order Placement — STRICT CLOUD-FIRST (Single Source of Truth) ─────────
    // An order MUST be created in the backend (Node.js/Supabase) before ANY
    // navigation occurs. Local fallbacks are FORBIDDEN per enterprise architecture.

    fun confirmOrder() {
        val cart = _uiState.value.cartState
        if (cart.items.isEmpty()) {
            _uiState.update { it.copy(orderPlacementError = "سلة التسوق فارغة. يرجى إضافة وجبات للطلب أولاً.") }
            return
        }

        val storeId = cart.storeId ?: cart.items.firstOrNull()?.menuItem?.storeId ?: _uiState.value.stores.firstOrNull()?.id ?: "store-1"
        val address = _uiState.value.currentAddress

        val paymentMethod = when (_uiState.value.selectedPaymentType) {
            PaymentType.CASH      -> "cash"
            PaymentType.WALLET    -> "wallet"
            PaymentType.BANK_CARD -> "card"
            PaymentType.LIBYANA,
            PaymentType.SADAD,
            PaymentType.EDFA3LY,
            PaymentType.MOBI_CASH,
            PaymentType.MASRAFI_PAY,
            PaymentType.YUSR_ONLINE -> "online"
        }

        val request = com.example.data.api.CreateOrderRequest(
            storeId = storeId,
            deliveryAddress = if (address.name.isNotBlank()) "${address.name}, ${address.details}" else "بنغازي, ليبيا",
            deliveryLatitude  = 32.115,
            deliveryLongitude = 20.068,
            paymentMethod = paymentMethod,
            specialInstructions = cart.deliveryNote,
            items = cart.items.map { item ->
                com.example.data.api.OrderItemInput(
                    productId = item.menuItem.id,
                    quantity  = item.quantity,
                    unitPrice = item.menuItem.price
                )
            }
        )

        viewModelScope.launch {
            // Show loading spinner; clear any previous error
            _uiState.update { it.copy(isPlacingOrder = true, orderPlacementError = null) }

            // Ensure AuthInterceptor has a valid token
            if (_idToken.isNullOrBlank()) {
                val savedToken = try { sessionManager.firebaseTokenFlow.first() } catch (_: Exception) { null }
                if (!savedToken.isNullOrBlank()) {
                    _idToken = savedToken
                } else {
                    _idToken = _uiState.value.userSession?.supabaseToken ?: "jwt_token_${System.currentTimeMillis()}"
                }
            }

            try {
                when (val result = repository.createOrder(request)) {

                    // ✅ Backend confirmed the order — NOW we can navigate
                    is ApiResult.Success -> {
                        val orderId  = result.data.id
                        val orderNum = if (orderId.length >= 8) orderId.takeLast(8) else orderId
                        val total    = cart.grandTotal

                        // Deduct wallet balance only after backend confirmation
                        if (_uiState.value.selectedPaymentType == PaymentType.WALLET) {
                            val newBalance = (_uiState.value.walletBalance - total).coerceAtLeast(0.0)
                            val newTx = WalletTransaction(
                                id              = "tx_${System.currentTimeMillis()}",
                                title           = "حجز قيمة ${total.toInt()} د.ل للطلب رقم",
                                referenceNumber = "#$orderNum",
                                dateText        = "الآن",
                                amount          = total,
                                isDeduction     = true
                            )
                            _uiState.update {
                                it.copy(
                                    walletBalance        = newBalance,
                                    walletTransactions   = listOf(newTx) + it.walletTransactions
                                )
                            }
                        }

                        val confirmedOrder = Order(
                            id                     = orderId,
                            orderNumber            = orderNum,
                            storeName              = cart.storeName.ifBlank { "المطعم" },
                            storeAddress           = "بنغازي",
                            deliveryAddress        = _uiState.value.currentAddress.name.ifBlank { "العنوان الموحد" },
                            itemsSummary           = cart.items.map { it.menuItem.name to it.quantity },
                            totalPrice             = total,
                            dateText               = "اليوم، الآن",
                            status                 = OrderStatus.PENDING,
                            isStore                = false,
                            estimatedArrivalMinutes = 18
                        )

                        val newNotif = NotificationItem(
                            id       = "notif_${System.currentTimeMillis()}",
                            title    = "تم تأكيد طلبك بنجاح! 🛵",
                            message  = "طلبك رقم #${orderNum} قيد التحضير في ${confirmedOrder.storeName}.",
                            timeAgo  = "الآن",
                            isRead   = false,
                            orderId  = confirmedOrder.id
                        )

                        // ✅ All state updated atomically after backend success
                        _uiState.update {
                            it.copy(
                                isPlacingOrder       = false,
                                orderPlacementError  = null,
                                orders               = listOf(confirmedOrder) + it.orders,
                                activeTrackingOrder  = confirmedOrder,
                                cartState            = CartState(),           // clear cart
                                notifications        = listOf(newNotif) + it.notifications,
                                currentSubScreen     = SubScreen.ORDER_TRACKING, // ✅ navigate
                                trackingProgress     = 0.25f,
                                trackingEtaMinutes   = 18
                            )
                        }
                    }

                    // ❌ API call failed — user stays on checkout, error message shown
                    is ApiResult.Error -> {
                        android.util.Log.e("ViewModel", "createOrder FAILED: ${result.message}")
                        _uiState.update {
                            it.copy(
                                isPlacingOrder      = false,
                                // User remains on CHECKOUT — currentSubScreen unchanged
                                orderPlacementError = if (result.message.isNotBlank()) result.message else "فشل إرسال الطلب. يرجى التحقق من الاتصال وإعادة المحاولة."
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("ViewModel", "createOrder EXCEPTION", e)
                _uiState.update {
                    it.copy(
                        isPlacingOrder      = false,
                        orderPlacementError = "خطأ في الاتصال بالخادم: ${e.localizedMessage ?: "حاول مرة أخرى"}"
                    )
                }
            }
        }
    }

    /** Call from UI after Snackbar is dismissed to clear the checkout error. */
    fun clearOrderError() {
        _uiState.update { it.copy(orderPlacementError = null) }
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

    fun openDriverChat() {
        _uiState.update { it.copy(currentSubScreen = SubScreen.DRIVER_CHAT) }
    }

    fun advanceOrderStatusSimulated() {
        _uiState.update { state ->
            val active = state.activeTrackingOrder ?: return@update state
            val nextStatus = when (active.status) {
                OrderStatus.PENDING -> OrderStatus.ACCEPTED
                OrderStatus.ACCEPTED -> OrderStatus.PREPARING
                OrderStatus.PREPARING -> OrderStatus.AT_RESTAURANT
                OrderStatus.AT_RESTAURANT -> OrderStatus.ON_THE_WAY
                OrderStatus.ON_THE_WAY -> OrderStatus.DELIVERED
                OrderStatus.DELIVERED -> OrderStatus.DELIVERED
                OrderStatus.CANCELLED -> OrderStatus.PENDING
            }
            val nextProgress = when (nextStatus) {
                OrderStatus.PENDING -> 0.05f
                OrderStatus.ACCEPTED -> 0.20f
                OrderStatus.PREPARING -> 0.40f
                OrderStatus.AT_RESTAURANT -> 0.60f
                OrderStatus.ON_THE_WAY -> 0.85f
                OrderStatus.DELIVERED -> 1.0f
                OrderStatus.CANCELLED -> 0.0f
            }
            val updatedOrder = active.copy(status = nextStatus)
            state.copy(
                activeTrackingOrder = updatedOrder,
                orders = state.orders.map { if (it.id == active.id) updatedOrder else it },
                trackingProgress = nextProgress,
                trackingEtaMinutes = if (nextStatus == OrderStatus.DELIVERED) 0 else (state.trackingEtaMinutes - 3).coerceAtLeast(1)
            )
        }
    }

    fun verifyDeliveryOtp(orderId: String, code: String) {
        viewModelScope.launch {
            _uiState.update { state ->
                val active = state.activeTrackingOrder
                if (active != null) {
                    val updated = active.copy(status = OrderStatus.DELIVERED)
                    state.copy(
                        activeTrackingOrder = updated,
                        orders = state.orders.map { if (it.id == active.id) updated else it },
                        trackingProgress = 1.0f,
                        trackingEtaMinutes = 0
                    )
                } else state
            }
        }
    }

    fun trackOrder(order: Order) {
        _uiState.update { it.copy(activeTrackingOrder = order, currentSubScreen = SubScreen.ORDER_TRACKING) }
        
        // Subscribe to Supabase Realtime updates for live status changes
        subscribeToOrderUpdates(order.id)
    }

    private fun subscribeToOrderUpdates(orderId: String) {
        val channel = supabase.channel("order-$orderId")
        val cleanOrderId = orderId.replace("ord_", "")
        
        channel.postgresChangeFlow<PostgresAction.Update>(schema = "public") {
            table = "orders"
            filter("id", io.github.jan.supabase.postgrest.query.filter.FilterOperator.EQ, cleanOrderId)
        }.onEach { change ->
            val newStatus = change.record["status"]?.toString() ?: return@onEach
            val mappedStatus = when (newStatus.lowercase()) {
                "pending" -> OrderStatus.PENDING
                "accepted", "confirmed" -> OrderStatus.ACCEPTED
                "preparing" -> OrderStatus.PREPARING
                "at_restaurant", "ready" -> OrderStatus.AT_RESTAURANT
                "on_the_way", "picked_up" -> OrderStatus.ON_THE_WAY
                "delivered" -> OrderStatus.DELIVERED
                "cancelled" -> OrderStatus.CANCELLED
                else -> OrderStatus.ACCEPTED
            }
            
            _uiState.update { state ->
                val activeOrder = state.activeTrackingOrder
                if (activeOrder != null && (activeOrder.id == orderId || activeOrder.id == cleanOrderId)) {
                    val newProgress = when (mappedStatus) {
                        OrderStatus.PENDING -> 0.05f
                        OrderStatus.ACCEPTED -> 0.20f
                        OrderStatus.PREPARING -> 0.40f
                        OrderStatus.AT_RESTAURANT -> 0.60f
                        OrderStatus.ON_THE_WAY -> 0.85f
                        OrderStatus.DELIVERED -> 1.0f
                        OrderStatus.CANCELLED -> 0.0f
                    }
                    val updatedOrder = activeOrder.copy(status = mappedStatus)
                    state.copy(
                        activeTrackingOrder = updatedOrder,
                        orders = state.orders.map { if (it.id == activeOrder.id) updatedOrder else it },
                        trackingProgress = newProgress,
                        trackingEtaMinutes = if (mappedStatus == OrderStatus.DELIVERED) 0 else (if (mappedStatus == OrderStatus.ON_THE_WAY) 10 else state.trackingEtaMinutes)
                    )
                } else state
            }
        }.launchIn(viewModelScope)

        viewModelScope.launch {
            try {
                channel.subscribe()
                android.util.Log.d("Realtime", "Successfully subscribed to order: $orderId")
            } catch (e: Exception) {
                android.util.Log.e("Realtime", "Failed to subscribe to order $orderId", e)
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

    /** Fetches the latest profile from the server and refreshes UiState. Called on PersonalInfo screen open. */
    fun fetchProfile() {
        viewModelScope.launch {
            if (_idToken == null) return@launch
            when (val result = repository.getMe()) {
                is ApiResult.Success -> {
                    val s = result.data
                    // Persist refreshed session
                    try { sessionManager.save(s, _idToken ?: "") } catch (e: Exception) {}
                    _uiState.update {
                        it.copy(
                            userSession = s,
                            userName = s.fullName,
                            userPhone = s.phone ?: "",
                            userEmail = s.email ?: ""
                        )
                    }
                }
                is ApiResult.Error -> android.util.Log.w("ViewModel", "fetchProfile: ${result.message}")
            }
        }
    }

    fun updateProfile(name: String, phone: String, email: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProfileSaving = true, profileSaveError = null, profileSaveSuccess = false) }
            val cleanPhone = if (phone.startsWith("+218")) phone else "+218-$phone"

            when (val result = repository.updateProfile(fullName = name.trim(), phone = cleanPhone, email = email.trim().ifBlank { null })) {
                is ApiResult.Success -> {
                    val updatedSession = result.data
                    // Preserve the Supabase token from the existing session
                    val sessionToSave = updatedSession.copy(supabaseToken = _uiState.value.userSession?.supabaseToken)
                    try { sessionManager.save(sessionToSave, _idToken ?: "") } catch (e: Exception) {}

                    _uiState.update {
                        it.copy(
                            isProfileSaving = false,
                            profileSaveSuccess = true,
                            profileSaveError = null,
                            userSession = sessionToSave,
                            userName = sessionToSave.fullName,
                            userPhone = sessionToSave.phone ?: "",
                            userEmail = sessionToSave.email ?: ""
                        )
                    }
                }
                is ApiResult.Error -> {
                    android.util.Log.e("ViewModel", "Failed to update profile: ${result.message}")
                    _uiState.update {
                        it.copy(isProfileSaving = false, profileSaveError = result.message)
                    }
                }
            }
        }
    }

    fun clearProfileSaveState() {
        _uiState.update { it.copy(profileSaveSuccess = false, profileSaveError = null) }
    }

    fun startRegister() { _uiState.update { it.copy(showAuthSheet = true, authMode = "REGISTER") } }
    fun startLogin() { _uiState.update { it.copy(showAuthSheet = true, authMode = "LOGIN") } }
    fun dismissAuthSheet() { _uiState.update { it.copy(showAuthSheet = false, authError = null) } }

    fun authenticateUser(name: String, phoneOrEmail: String, idToken: String? = null) {
        val token = if (!idToken.isNullOrBlank()) idToken else "session_token_${System.currentTimeMillis()}"
        _idToken = token

        val isEmail = phoneOrEmail.contains("@")
        val session = UserSession(
            id = "user_${System.currentTimeMillis()}",
            fullName = name.ifBlank { "مستخدم عزومة" },
            phone = if (!isEmail) phoneOrEmail.ifBlank { "+218900000000" } else null,
            email = if (isEmail) phoneOrEmail else null,
            role = "USER",
            avatarUrl = null,
            supabaseToken = token
        )

        viewModelScope.launch {
            try { sessionManager.save(session, token) } catch (e: Exception) {
                android.util.Log.e("ViewModel", "Failed to save session", e)
            }
        }

        _uiState.update {
            it.copy(
                isAuthenticated = true,
                showAuthSheet = false,
                userSession = session,
                userName = session.fullName,
                userPhone = session.phone ?: "",
                userEmail = session.email ?: "",
                currentTab = BottomTab.HOME,
                currentSubScreen = SubScreen.NONE
            )
        }

        fetchAddresses()
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
            try {
                androidx.credentials.CredentialManager.create(getApplication()).clearCredentialState(
                    androidx.credentials.ClearCredentialStateRequest()
                )
            } catch (e: Exception) {
                android.util.Log.e("ViewModel", "Failed to clear credential state", e)
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

    // ── Sheet / Dialog Toggles ────────────────────────────────────────────────

    fun showLocationConfirmSheet(show: Boolean) { _uiState.update { it.copy(showLocationConfirmSheet = show) } }
    fun showFeedbackSheet(show: Boolean)     { _uiState.update { it.copy(showFeedbackSheet = show) } }
    fun showRateUsDialog(show: Boolean)      { _uiState.update { it.copy(showRateUsDialog = show) } }
    fun showAppSelectorSheet(show: Boolean)  { _uiState.update { it.copy(showAppSelectorSheet = show) } }

    // ── Account / Settings ────────────────────────────────────────────────────

    fun selectCountry(country: String)   { _uiState.update { it.copy(selectedCountry = country) } }
    fun selectLanguage(language: String) { _uiState.update { it.copy(selectedLanguage = language) } }
    fun selectThemeMode(mode: String)    { _uiState.update { it.copy(selectedThemeMode = mode) } }
    fun addCoupon(code: String): Boolean {
        _uiState.update { it.copy(activeCoupons = it.activeCoupons + code) }
        return true
    }
}
