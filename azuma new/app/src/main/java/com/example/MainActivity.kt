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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.layout.Column
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.model.OrderStatus
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
                    AzoomaApp()
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

    // If unauthenticated: Display Welcome Screen (Matching User's Uploaded Screenshot)
    if (!uiState.isAuthenticated) {
        WelcomeScreen(
            onRegisterClick = { viewModel.startRegister() },
            onLoginClick = { viewModel.startLogin() },
            onContinueAsGuestClick = { viewModel.continueAsGuest() }
        )

        // Auth Bottom Sheet (Phone + OTP + Google)
        if (uiState.showAuthSheet) {
            AuthSheet(
                isRegister = uiState.authMode == "REGISTER",
                onDismiss = { viewModel.dismissAuthSheet() },
                onSuccess = { name, phoneOrEmail, idToken ->
                    viewModel.authenticateUser(name, phoneOrEmail, idToken)
                    Toast.makeText(context, "مرحباً بك في عزومة! تم الدخول بنجاح", Toast.LENGTH_SHORT).show()
                }
            )
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
                Column {
                    uiState.activeTrackingOrder?.let { activeOrder ->
                        if (activeOrder.status != OrderStatus.DELIVERED && activeOrder.status != OrderStatus.CANCELLED) {
                            ActiveOrderFloatingBar(
                                order = activeOrder,
                                onClick = { viewModel.trackOrder(activeOrder) }
                            )
                        }
                    }
                    AzoomaBottomNav(
                        currentTab = uiState.currentTab,
                        cartItemCount = uiState.cartState.totalItemCount,
                        onTabSelected = { viewModel.selectTab(it) }
                    )
                }
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
                    uiState.selectedCategory?.let { category ->
                        CategoryDetailScreen(
                            category = category,
                            stores = uiState.stores,
                            onBackClick = { viewModel.navigateBack() },
                            onStoreClick = { viewModel.openStore(it) }
                        )
                    }
                }

                SubScreen.STORE_PROFILE -> {
                    uiState.selectedStore?.let { store ->
                        StoreProfileScreen(
                            store = store,
                            menuCategories = uiState.selectedStoreMenu,
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
                        isPlacingOrder = uiState.isPlacingOrder,
                        orderPlacementError = uiState.orderPlacementError,
                        onBackClick = { viewModel.navigateBack() },
                        onToggleDelivery = { viewModel.toggleDeliveryMode(it) },
                        onAddressClick = { viewModel.showAddressPicker(true) },
                        onSelectPaymentClick = { viewModel.showPaymentSheet(true) },
                        onDeliveryNoteChange = { viewModel.setDeliveryNote(it) },
                        onConfirmOrder = { viewModel.confirmOrder() },
                        onClearOrderError = { viewModel.clearOrderError() }
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
                            },
                            onChatDriver = { viewModel.openDriverChat() },
                            onAdvanceStatus = { viewModel.advanceOrderStatusSimulated() },
                            onVerifyDeliveryCode = { code -> viewModel.verifyDeliveryOtp(order.id, code) },
                            onReorderClick = { viewModel.reorder(order) }
                        )
                    }
                }

                SubScreen.DRIVER_CHAT -> {
                    val order = uiState.activeTrackingOrder ?: uiState.orders.firstOrNull()
                    DriverChatScreen(
                        driverName = order?.driverName ?: "أحمد المسماري",
                        onBackClick = { viewModel.navigateBack() }
                    )
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

                SubScreen.FAVORITES -> {
                    FavoritesScreen(
                        favoriteStoreIds = uiState.favoriteStoreIds,
                        stores = uiState.stores,
                        onBackClick = { viewModel.navigateBack() },
                        onStoreClick = { viewModel.openStore(it) },
                        onToggleFavorite = { viewModel.toggleFavorite(it) }
                    )
                }

                SubScreen.HELP -> {
                    HelpScreen(
                        onBackClick = { viewModel.navigateBack() }
                    )
                }

                SubScreen.COUPONS -> {
                    CouponsScreen(
                        activeCoupons = uiState.activeCoupons,
                        onAddCoupon = { viewModel.addCoupon(it) },
                        onBackClick = { viewModel.navigateBack() }
                    )
                }

                SubScreen.PERSONAL_INFO -> {
                    // Fetch fresh profile data from the server the moment this screen appears
                    LaunchedEffect(Unit) {
                        viewModel.fetchProfile()
                    }

                    // React to save success/error asynchronously
                    LaunchedEffect(uiState.profileSaveSuccess) {
                        if (uiState.profileSaveSuccess) {
                            Toast.makeText(context, "✅ تم حفظ البيانات بنجاح", Toast.LENGTH_SHORT).show()
                            viewModel.clearProfileSaveState()
                            viewModel.navigateBack()
                        }
                    }
                    LaunchedEffect(uiState.profileSaveError) {
                        uiState.profileSaveError?.let { err ->
                            Toast.makeText(context, "❌ فشل الحفظ: $err", Toast.LENGTH_LONG).show()
                            viewModel.clearProfileSaveState()
                        }
                    }

                    PersonalInfoScreen(
                        name = uiState.userName,
                        phone = uiState.userPhone,
                        email = uiState.userEmail,
                        onSaveProfile = { name, phone, email ->
                            viewModel.updateProfile(name, phone, email)
                        },
                        onDeleteAccount = {
                            viewModel.logout()
                            Toast.makeText(context, "تم حذف الحساب بنجاح", Toast.LENGTH_SHORT).show()
                        },
                        onBackClick = { viewModel.navigateBack() }
                    )
                }

                SubScreen.COUNTRY_SELECT -> {
                    CountrySelectScreen(
                        currentCountry = uiState.selectedCountry,
                        onSelectCountry = { country ->
                            viewModel.selectCountry(country)
                            Toast.makeText(context, "تم تغيير الدولة إلى $country", Toast.LENGTH_SHORT).show()
                            viewModel.navigateBack()
                        },
                        onBackClick = { viewModel.navigateBack() }
                    )
                }

                SubScreen.LANGUAGE_DISPLAY -> {
                    LanguageDisplayScreen(
                        currentLanguage = uiState.selectedLanguage,
                        currentThemeMode = uiState.selectedThemeMode,
                        onSelectLanguage = { viewModel.selectLanguage(it) },
                        onSelectThemeMode = { viewModel.selectThemeMode(it) },
                        onBackClick = { viewModel.navigateBack() }
                    )
                }

                SubScreen.ABOUT -> {
                    AboutScreen(
                        onTermsClick = { viewModel.openSubScreen(SubScreen.TERMS) },
                        onSocialClick = { platform ->
                            viewModel.showAppSelectorSheet(true)
                        },
                        onBackClick = { viewModel.navigateBack() }
                    )
                }

                SubScreen.TERMS -> {
                    TermsScreen(
                        onBackClick = { viewModel.navigateBack() }
                    )
                }

                SubScreen.NONE -> {
                    // Main 5 Bottom Tabs
                    when (uiState.currentTab) {
                        BottomTab.HOME -> {
                            HomeScreen(
                                currentAddressName = uiState.currentAddress.name,
                                stores = uiState.stores,
                                categories = uiState.appCategories,
                                onAddressClick = { viewModel.showLocationConfirmSheet(true) },
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
                                recommendations = uiState.selectedStoreMenu.flatMap { it.products },
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
                                selectedCountry = uiState.selectedCountry,
                                onWalletClick = { viewModel.openSubScreen(SubScreen.WALLET) },
                                onHelpClick = { viewModel.openSubScreen(SubScreen.HELP) },
                                onFavoritesClick = { viewModel.openSubScreen(SubScreen.FAVORITES) },
                                onAddressesClick = { viewModel.openSubScreen(SubScreen.ADDRESSES) },
                                onCouponsClick = { viewModel.openSubScreen(SubScreen.COUPONS) },
                                onPersonalInfoClick = { viewModel.openSubScreen(SubScreen.PERSONAL_INFO) },
                                onCountryClick = { viewModel.openSubScreen(SubScreen.COUNTRY_SELECT) },
                                onLanguageDisplayClick = { viewModel.openSubScreen(SubScreen.LANGUAGE_DISPLAY) },
                                onFeedbackClick = { viewModel.showFeedbackSheet(true) },
                                onTermsClick = { viewModel.openSubScreen(SubScreen.TERMS) },
                                onAboutClick = { viewModel.openSubScreen(SubScreen.ABOUT) },
                                onRateUsClick = { viewModel.showRateUsDialog(true) },
                                onNotificationClick = { viewModel.openSubScreen(SubScreen.NOTIFICATIONS) },
                                onLogoutClick = { viewModel.showLogoutDialog(true) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Payment Selection Bottom Sheet (Screenshot 7)
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

    // Wallet Recharge Sheet (Screenshots 21 & 22)
    if (uiState.showRechargeSheet) {
        WalletRechargeSheet(
            onDismiss = { viewModel.showRechargeSheet(false) },
            onRechargeSuccess = { amount, method ->
                viewModel.rechargeWallet(amount, method)
                Toast.makeText(context, "تم شحن المحفظة بنجاح بقيمة $amount د.ل عبر $method", Toast.LENGTH_LONG).show()
            }
        )
    }

    // Add / Edit Address Sheet (Screenshots 23 & 24)
    if (uiState.showAddressPickerSheet) {
        AddAddressSheet(
            onDismiss = { viewModel.showAddressPicker(false) },
            onSave = { name, details ->
                viewModel.saveNewAddress(name, details)
                Toast.makeText(context, "تم حفظ العنوان الجديد بنجاح", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Address Location Confirmation Sheet (Screenshot 1)
    if (uiState.showLocationConfirmSheet) {
        AddressConfirmSheet(
            addressName = uiState.currentAddress.name,
            onConfirmHere = {
                viewModel.showLocationConfirmSheet(false)
                Toast.makeText(context, "تم تأكيد موقع التوصيل: ${uiState.currentAddress.name}", Toast.LENGTH_SHORT).show()
            },
            onChangeAddress = {
                viewModel.showLocationConfirmSheet(false)
                viewModel.openSubScreen(SubScreen.ADDRESSES)
            },
            onDismiss = { viewModel.showLocationConfirmSheet(false) }
        )
    }

    // Feedback Sheet (Screenshots 17, 18, 19, 20)
    if (uiState.showFeedbackSheet) {
        FeedbackSheet(
            onDismiss = { viewModel.showFeedbackSheet(false) },
            onSubmitFeedback = { isProblem, text, allowContact ->
                viewModel.showFeedbackSheet(false)
                val msg = if (isProblem) "تم استلام ملاحظتك الفنية بنجاح، وسيتواصل معك الدعم قريباً" else "شكراً لمشاركتنا رأيك القيّم!"
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            }
        )
    }

    // Rate Us Dialog (Screenshot 16)
    if (uiState.showRateUsDialog) {
        RateUsDialog(
            onDismiss = { viewModel.showRateUsDialog(false) },
            onSubmitRating = { stars ->
                viewModel.showRateUsDialog(false)
                Toast.makeText(context, "شكراً لتقييمك بـ $stars نجوم! 🧡", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // App Selector Chooser Sheet (Screenshot 22)
    if (uiState.showAppSelectorSheet) {
        AppSelectorSheet(
            onDismiss = { viewModel.showAppSelectorSheet(false) },
            onAppSelected = { appName ->
                viewModel.showAppSelectorSheet(false)
                Toast.makeText(context, "جارٍ الفتح بواسطة $appName...", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Logout Confirmation Dialog (Screenshot 25)
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
