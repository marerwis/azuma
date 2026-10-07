package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Address
import com.example.model.CartState
import com.example.model.PaymentType
import com.example.ui.components.InteractiveMapCanvas
import com.example.ui.components.PaymentBrandIcon
import com.example.ui.theme.*

@Composable
fun CheckoutScreen(
    cartState: CartState,
    currentAddress: Address,
    selectedPaymentType: PaymentType,
    walletBalance: Double,
    isPlacingOrder: Boolean = false,
    orderPlacementError: String? = null,
    onBackClick: () -> Unit,
    onToggleDelivery: (Boolean) -> Unit,
    onAddressClick: () -> Unit,
    onSelectPaymentClick: () -> Unit,
    onDeliveryNoteChange: (String) -> Unit,
    onConfirmOrder: () -> Unit,
    onClearOrderError: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showItemsSummary by remember { mutableStateOf(false) }
    var showNotesInput by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Show Snackbar when API returns an error (user stays on checkout)
    LaunchedEffect(orderPlacementError) {
        if (orderPlacementError != null) {
            snackbarHostState.showSnackbar(
                message = orderPlacementError,
                actionLabel = "إغلاق",
                duration = SnackbarDuration.Long
            )
            onClearOrderError()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            // Bottom CTA Button: "تأكيد الطلب" — Cloud-First: disabled while API call is in flight
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                shadowElevation = 8.dp
            ) {
                Button(
                    onClick = { if (!isPlacingOrder) onConfirmOrder() },
                    enabled = !isPlacingOrder,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AzoomaOrange,
                        disabledContainerColor = AzoomaOrange.copy(alpha = 0.7f),
                        disabledContentColor = Color.White
                    )
                ) {
                    if (isPlacingOrder) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.5.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "جاري إرسال الطلب...",
                                style = AppTypography.titleMedium.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "%.2f د.ل".format(cartState.grandTotal),
                                style = AppTypography.titleMedium.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "تأكيد الطلب",
                                style = AppTypography.titleMedium.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }
        },
        containerColor = AzoomaBackground
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Header (Screenshot 6: "تفاصيل الطلب")
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع",
                            tint = AzoomaTextPrimary
                        )
                    }

                    Text(
                        text = "تفاصيل الطلب",
                        style = AppTypography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = AzoomaTextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.size(40.dp))
                }
            }

            // Delivery vs Pickup Switcher (Screenshot 6)
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(14.dp)),
                    color = Color(0xFFF1F3F5)
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onToggleDelivery(true) },
                            color = if (cartState.isDelivery) Color.White else Color.Transparent,
                            shadowElevation = if (cartState.isDelivery) 2.dp else 0.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "توصيل",
                                    style = AppTypography.titleSmall.copy(
                                        color = if (cartState.isDelivery) AzoomaOrange else AzoomaTextSecondary,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.TwoWheeler,
                                    contentDescription = null,
                                    tint = if (cartState.isDelivery) AzoomaOrange else AzoomaTextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onToggleDelivery(false) },
                            color = if (!cartState.isDelivery) Color.White else Color.Transparent,
                            shadowElevation = if (!cartState.isDelivery) 2.dp else 0.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "استلام",
                                    style = AppTypography.titleSmall.copy(
                                        color = if (!cartState.isDelivery) AzoomaOrange else AzoomaTextSecondary,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.ShoppingBag,
                                    contentDescription = null,
                                    tint = if (!cartState.isDelivery) AzoomaOrange else AzoomaTextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Map and Delivery Address (Screenshot 6)
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    color = Color.White,
                    shadowElevation = 1.dp
                ) {
                    Column {
                        // Map view
                        InteractiveMapCanvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            destinationLabel = currentAddress.name
                        )

                        // Location address bar below map
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onAddressClick() }
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = AzoomaTextSecondary
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = currentAddress.name,
                                    style = AppTypography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = AzoomaTextPrimary
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = AzoomaOrange,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Notes Section (Screenshot 6: "ملاحظات -> ملاحظة لبريستومان")
            item {
                Text(
                    text = "ملاحظات",
                    style = AppTypography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = AzoomaTextPrimary
                    ),
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    color = Color.White,
                    shadowElevation = 1.dp
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showNotesInput = !showNotesInput },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (showNotesInput) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = AzoomaTextSecondary
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (cartState.deliveryNote.isBlank()) "ملاحظة لبريستومان" else cartState.deliveryNote,
                                    style = AppTypography.bodyMedium.copy(
                                        color = if (cartState.deliveryNote.isBlank()) AzoomaTextSecondary else AzoomaTextPrimary,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = AzoomaOrange,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        if (showNotesInput) {
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = cartState.deliveryNote,
                                onValueChange = onDeliveryNoteChange,
                                placeholder = { Text("مثلاً: يررجى الاتصال عند الوصول أمام العمارة") },
                                modifier = Modifier
                                    .fillMaxWidth()
,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AzoomaOrange,
                                    unfocusedBorderColor = AzoomaCardBorder
                                )
                            )
                        }
                    }
                }
            }

            // Order Summary Card (Screenshot 6: "ملخص الطلب")
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "ملخص الطلب",
                    style = AppTypography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = AzoomaTextPrimary
                    ),
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    color = Color.White,
                    shadowElevation = 1.dp
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showItemsSummary = !showItemsSummary },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (showItemsSummary) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = AzoomaTextSecondary
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = cartState.storeName.ifBlank { "شنابو - طريق المطار" },
                                        style = AppTypography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = AzoomaTextPrimary
                                        )
                                    )
                                    Text(
                                        text = "${cartState.totalItemCount} عناصر",
                                        style = AppTypography.bodySmall.copy(color = AzoomaTextSecondary)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.ReceiptLong,
                                    contentDescription = null,
                                    tint = AzoomaOrange,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        if (showItemsSummary) {
                            Divider(modifier = Modifier.padding(vertical = 10.dp), color = AzoomaCardBorder)
                            cartState.items.forEach { cItem ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "%.0f د.ل".format(cItem.totalPrice),
                                        style = AppTypography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = AzoomaTextPrimary
                                        )
                                    )
                                    Text(
                                        text = "${cItem.menuItem.name} (x${cItem.quantity})",
                                        style = AppTypography.bodyMedium.copy(color = AzoomaTextSecondary)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Payment Method Selector Row (Screenshot 6: "طريقة الدفع")
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "طريقة الدفع",
                    style = AppTypography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = AzoomaTextPrimary
                    ),
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onSelectPaymentClick() }
,
                    color = Color.White,
                    shadowElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = AzoomaTextSecondary
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = selectedPaymentType.titleAr,
                                style = AppTypography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = AzoomaTextPrimary
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            PaymentBrandIcon(selectedPaymentType)
                        }
                    }
                }
            }

            // Invoice Breakdown Card (Screenshot 6: "الفاتورة")
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "الفاتورة",
                    style = AppTypography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = AzoomaTextPrimary
                    ),
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    color = Color.White,
                    shadowElevation = 1.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        InvoiceRow(label = "مجموع الأصناف", value = "%.2f د.ل".format(cartState.subtotal))
                        InvoiceRow(label = "رسوم التوصيل", value = "%.2f د.ل".format(cartState.deliveryFee))
                        InvoiceRow(label = "رسوم الخدمة", value = "%.2f د.ل".format(cartState.serviceFee))

                        Divider(modifier = Modifier.padding(vertical = 10.dp), color = AzoomaCardBorder)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "%.2f د.ل".format(cartState.grandTotal),
                                style = AppTypography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = AzoomaOrange
                                )
                            )
                            Text(
                                text = "المبلغ الإجمالي",
                                style = AppTypography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = AzoomaTextPrimary
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InvoiceRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = value,
            style = AppTypography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                color = AzoomaTextPrimary
            )
        )
        Text(
            text = label,
            style = AppTypography.bodyMedium.copy(color = AzoomaTextSecondary)
        )
    }
}
