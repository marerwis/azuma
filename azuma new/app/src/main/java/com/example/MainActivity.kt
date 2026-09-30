package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AzoomaViewModel
import com.example.viewmodel.BottomTab
import com.example.viewmodel.SubScreen
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                // STRICT GLOBAL RTL ENFORCEMENT FOR THE ENTIRE APPLICATION
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    androidx.compose.material3.Surface(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
                        AzoomaApp()
                    }
                }
            }
        }
    }
}

@Composable
fun AzoomaApp(viewModel: AzoomaViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // If session is still loading, block the UI to prevent login screen flash
    if (uiState.isSessionLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // If unauthenticated: Display Welcome Screen
    if (!uiState.isAuthenticated) {
        WelcomeScreen(
            onRegisterClick = { viewModel.startRegister() },
            onLoginClick = { viewModel.startLogin() },
            onContinueAsGuestClick = { viewModel.continueAsGuest() }
        )

        // Auth Bottom Sheet (Firebase Phone Auth → API verify)
        if (uiState.showAuthSheet) {
            AuthSheet(
                isRegister = uiState.authMode == "REGISTER",
                onDismiss = { viewModel.dismissAuthSheet() },
                onSuccess = { idToken, name ->
                    // Token verified by Firebase → send to our Node.js API to sync user
                    viewModel.verifyWithApi(idToken = idToken, fullName = name.ifBlank { null })
                }
            )
        }

        // Show API-level auth error as a toast (e.g. server unreachable)
        val authError = uiState.authError
        LaunchedEffect(authError) {
            if (authError != null) {
                Toast.makeText(context, "خطأ: $authError", Toast.LENGTH_LONG).show()
            }
        }
        return
    }

    // Hardware / System Back handling when inside authenticated app
    BackHandler(enabled = uiState.currentSubScreen != SubScreen.NONE || uiState.currentTab != BottomTab.HOME) {
        viewModel.navigateBack()
    }

    val showBottomBar = uiState.currentSubScreen == SubScreen.NONE

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                AzoomaBottomNav(
                    currentTab = uiState.currentTab,
                    cartItemCount = uiState.cartState.totalItemCount,
                    onTabSelected = { viewModel.selectTab(it) }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (showBottomBar) innerPadding.calculateBottomPadding() else androidx.compose.ui.unit.Dp(0f))
        ) {
            when (uiState.currentSubScreen) {
                SubScreen.CATEGORY_DETAIL -> {
                    val category = uiState.selectedCategory
                    if (category != null) {
                        CategoryDetailScreen(
                            category = category,
                            stores = uiState.stores,
                            onBackClick = { viewModel.navigateBack() },
                            onStoreClick = { viewModel.openStore(it) }
                        )
                    }
                }

                SubScreen.STORE_PROFILE -> {
                    val store = uiState.selectedStore
                    if (store != null) {
                        StoreProfileScreen(
                            store = store,
                            storeMenu = uiState.selectedStoreMenu,
                            isLoadingMenu = uiState.isLoadingStoreMenu,
                            cartState = uiState.cartState,
                            isFavorite = uiState.favoriteStoreIds.contains(store.id),
                            onToggleFavorite = { viewModel.toggleFavorite(store.id) },
                            onBackClick = { viewModel.navigateBack() },
                            onAddToCart = { viewModel.addToCart(it, store) },
                            onRemoveFromCart = { viewModel.removeFromCart(it) },
                            onViewCartClick = { viewModel.selectTab(BottomTab.CART) }
                        )
                    }
                }

                SubScreen.CHECKOUT -> {
                    CheckoutScreen(
                        cartState = uiState.cartState,
                        currentAddress = uiState.currentAddress,
                        selectedPaymentType = uiState.selectedPaymentType,
                        walletBalance = uiState.walletBalance,
                        onBackClick = { viewModel.navigateBack() },
                        onToggleDelivery = { viewModel.toggleDeliveryMode(it) },
                        onAddressClick = { viewModel.showAddressPicker(true) },
                        onSelectPaymentClick = { viewModel.showPaymentSheet(true) },
                        onDeliveryNoteChange = { viewModel.setDeliveryNote(it) },
                        onConfirmOrder = { viewModel.confirmOrder() }
                    )
                }

                SubScreen.ORDER_TRACKING -> {
                    val order = uiState.activeTrackingOrder ?: uiState.orders.firstOrNull()
                    if (order != null) {
                        OrderTrackingScreen(
                            order = order,
                            driverProgress = uiState.trackingProgress,
                            etaMinutes = uiState.trackingEtaMinutes,
                            onBackClick = { viewModel.navigateBack() },
                            onCallDriver = {
                                Toast.makeText(context, "جارٍ الاتصال بالسائق ${order.driverName}...", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }

                SubScreen.WALLET -> {
                    WalletScreen(
                        balance = uiState.walletBalance,
                        transactions = uiState.walletTransactions,
                        onBackClick = { viewModel.navigateBack() },
                        onRechargeClick = { viewModel.showRechargeSheet(true) }
                    )
                }

                SubScreen.ADDRESSES -> {
                    AddressesScreen(
                        addresses = uiState.addresses,
                        currentAddress = uiState.currentAddress,
                        onBackClick = { viewModel.navigateBack() },
                        onSelectAddress = { viewModel.selectAddress(it) },
                        onDeleteAddress = { viewModel.deleteAddress(it) },
                        onAddNewAddressClick = { viewModel.showAddressPicker(true) }
                    )
                }

                SubScreen.NOTIFICATIONS -> {
                    NotificationsScreen(
                        notifications = uiState.notifications,
                        onBackClick = { viewModel.navigateBack() },
                        onNotificationClick = { notif ->
                            if (notif.orderId != null) {
                                val order = uiState.orders.find { it.id == notif.orderId }
                                if (order != null) {
                                    viewModel.trackOrder(order)
                                }
                            }
                        }
                    )
                }

                SubScreen.OFFERS -> {
                    OffersScreen(
                        stores = uiState.stores,
                        onBackClick = { viewModel.navigateBack() },
                        onStoreClick = { viewModel.openStore(it) },
                        favoriteStoreIds = uiState.favoriteStoreIds,
                        onToggleFavorite = { viewModel.toggleFavorite(it) }
                    )
                }

                SubScreen.NONE -> {
                    // Show global loading indicator while Supabase data is being fetched
                    if (uiState.isLoadingData && uiState.stores.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else {
                        // Main 5 Bottom Tabs
                        when (uiState.currentTab) {
                            BottomTab.HOME -> {
                                HomeScreen(
                                    currentAddressName = uiState.currentAddress.name,
                                    categories = uiState.appCategories,
                                    stores = uiState.stores,
                                    banners = uiState.appBanners,
                                    onAddressClick = { viewModel.showAddressPicker(true) },
                                    onSearchClick = { viewModel.selectTab(BottomTab.SEARCH) },
                                    onNotificationClick = { viewModel.openSubScreen(SubScreen.NOTIFICATIONS) },
                                    onCategoryClick = { viewModel.openCategory(it) },
                                    onStoreClick = { viewModel.openStore(it) },
                                    onViewAllOffersClick = { viewModel.openSubScreen(SubScreen.OFFERS) }
                                )
                            }

                            BottomTab.SEARCH -> {
                                SearchScreen(
                                    searchQuery = uiState.searchQuery,
                                    stores = uiState.stores,
                                    recentSearches = uiState.recentSearches,
                                    onQueryChange = { viewModel.updateSearchQuery(it) },
                                    onSearchSubmit = { viewModel.addRecentSearch(it) },
                                    onRemoveRecentSearch = { viewModel.removeRecentSearch(it) },
                                    onStoreClick = { viewModel.openStore(it) }
                                )
                            }

                            BottomTab.CART -> {
                                CartScreen(
                                    cartState = uiState.cartState,
                                    recommendedItems = uiState.menuItems,
                                    onBackClick = { viewModel.navigateBack() },
                                    onAddToCart = { viewModel.addToCart(it) },
                                    onRemoveFromCart = { viewModel.removeFromCart(it) },
                                    onDeleteItem = { viewModel.deleteItemFromCart(it) },
                                    onAddMoreItemsClick = {
                                        if (uiState.selectedStore != null) {
                                            viewModel.openSubScreen(SubScreen.STORE_PROFILE)
                                        } else {
                                            viewModel.selectTab(BottomTab.HOME)
                                        }
                                    },
                                    onStoreNoteChange = { viewModel.setStoreNote(it) },
                                    onContinueClick = { viewModel.openSubScreen(SubScreen.CHECKOUT) }
                                )
                            }

                            BottomTab.ORDERS -> {
                                OrdersScreen(
                                    orders = uiState.orders,
                                    activeTrackingOrder = uiState.activeTrackingOrder,
                                    onTrackOrder = { viewModel.trackOrder(it) },
                                    onReorder = { viewModel.reorder(it) }
                                )
                            }

                            BottomTab.ACCOUNT -> {
                                AccountScreen(
                                    userName = uiState.userName,
                                    userPhone = uiState.userPhone,
                                    onFavoritesClick = {
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("لديك ${uiState.favoriteStoreIds.size} متاجر في المفضلة")
                                        }
                                    },
                                    onAddressesClick = { viewModel.openSubScreen(SubScreen.ADDRESSES) },
                                    onWalletClick = { viewModel.openSubScreen(SubScreen.WALLET) },
                                    onNotificationClick = { viewModel.openSubScreen(SubScreen.NOTIFICATIONS) },
                                    onLogoutClick = { viewModel.showLogoutDialog(true) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Payment Selection Bottom Sheet
    if (uiState.showPaymentSheet) {
        PaymentMethodSheet(
            selectedType = uiState.selectedPaymentType,
            walletBalance = uiState.walletBalance,
            totalAmount = uiState.cartState.grandTotal,
            onSelect = { viewModel.setPaymentType(it) },
            onDismiss = { viewModel.showPaymentSheet(false) },
            onRechargeClick = {
                viewModel.showPaymentSheet(false)
                viewModel.showRechargeSheet(true)
            }
        )
    }

    // Wallet Recharge Sheet
    if (uiState.showRechargeSheet) {
        WalletRechargeSheet(
            onDismiss = { viewModel.showRechargeSheet(false) },
            onRechargeSuccess = { amount, method ->
                viewModel.rechargeWallet(amount, method)
                Toast.makeText(context, "تم شحن المحفظة بنجاح بقيمة $amount د.ل عبر $method", Toast.LENGTH_LONG).show()
            }
        )
    }

    // Add / Edit Address Sheet
    if (uiState.showAddressPickerSheet) {
        AddAddressSheet(
            onDismiss = { viewModel.showAddressPicker(false) },
            onSave = { name, details ->
                viewModel.saveNewAddress(name, details)
                Toast.makeText(context, "تم حفظ العنوان الجديد بنجاح", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Logout Confirmation Dialog
    if (uiState.showLogoutDialog) {
        LogoutConfirmationDialog(
            onDismiss = { viewModel.showLogoutDialog(false) },
            onConfirm = {
                viewModel.logout()
                Toast.makeText(context, "تم تسجيل الخروج بنجاح", Toast.LENGTH_SHORT).show()
            }
        )
    }
}
