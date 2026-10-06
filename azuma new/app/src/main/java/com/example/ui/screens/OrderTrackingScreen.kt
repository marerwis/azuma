package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Order
import com.example.model.OrderStatus
import com.example.model.PickupStatus
import com.example.ui.components.InteractiveMapCanvas
import com.example.ui.theme.*

@Composable
fun OrderTrackingScreen(
    order: Order,
    driverProgress: Float,
    etaMinutes: Int,
    pickupStatus: PickupStatus = order.pickupStatus,
    activeNotification: String? = null,
    onBackClick: () -> Unit,
    onCallDriver: () -> Unit = {},
    onChatDriver: () -> Unit = {},
    onHelpClick: () -> Unit = {},
    onAdvanceStatus: () -> Unit = {},
    onVerifyDeliveryCode: (String) -> Unit = {},
    onDismissNotification: () -> Unit = {},
    onReorderClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (order.isPickup) {
        PickupOrderTrackingView(
            order = order,
            currentStatus = pickupStatus,
            activeNotification = activeNotification,
            onBackClick = onBackClick,
            onHelpClick = onHelpClick,
            onAdvancePickupStatus = onAdvanceStatus,
            onDismissNotification = onDismissNotification,
            modifier = modifier
        )
    } else {
        DeliveryOrderLifecycleView(
            order = order,
            driverProgress = driverProgress,
            activeNotification = activeNotification,
            onBackClick = onBackClick,
            onCallDriver = onCallDriver,
            onChatDriver = onChatDriver,
            onHelpClick = onHelpClick,
            onAdvanceStatus = onAdvanceStatus,
            onVerifyDeliveryCode = onVerifyDeliveryCode,
            onDismissNotification = onDismissNotification,
            onReorderClick = onReorderClick,
            modifier = modifier
        )
    }
}

/**
 * Full Delivery Order Lifecycle Tracking View (دورة طلب العميل لزر التوصيل)
 * Matches the user's video:
 * 1. 5-Step Horizontal Stepper: [قيد الإنتظار] -> [جار التجهيز] -> [في المطعم] -> [في الطريق] -> [وصلنا لك طلبك 😎]
 * 2. Driver Card with Call & In-App Chat buttons
 * 3. Customer Delivery Verification Code linked to Room Database
 * 4. Driver Code Input Verification Handover Dialog
 * 5. Delivered Final Completion Screen ("وصلنا لك طلبك 😎") with ratings, invoice, and email receipt
 */
@Composable
private fun DeliveryOrderLifecycleView(
    order: Order,
    driverProgress: Float,
    activeNotification: String?,
    onBackClick: () -> Unit,
    onCallDriver: () -> Unit,
    onChatDriver: () -> Unit,
    onHelpClick: () -> Unit,
    onAdvanceStatus: () -> Unit,
    onVerifyDeliveryCode: (String) -> Unit,
    onDismissNotification: () -> Unit,
    onReorderClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showDriverCodeDialog by remember { mutableStateOf(false) }
    var userRating by remember { mutableIntStateOf(5) }
    var emailInput by remember { mutableStateOf("marerwis@gmail.com") }
    var emailSent by remember { mutableStateOf(false) }
    var isStoreExpanded by remember { mutableStateOf(true) }

    // If order has reached DELIVERED status, render the Delivered Completion Screen (Video 02:51 - 03:00)
    if (order.status == OrderStatus.DELIVERED) {
        DeliveredCompletionScreen(
            order = order,
            userRating = userRating,
            onRatingChange = { userRating = it },
            emailInput = emailInput,
            onEmailChange = { emailInput = it },
            emailSent = emailSent,
            onSendEmail = {
                emailSent = true
                Toast.makeText(context, "تم إرسال الفاتورة إلى $emailInput بنجاح!", Toast.LENGTH_LONG).show()
            },
            onBackClick = onBackClick,
            onReorderClick = onReorderClick,
            modifier = modifier
        )
        return
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFE9EDF0))
    ) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 1. Live Interactive City Map Canvas (Upper section, 40% height)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.40f)
        ) {
            InteractiveMapCanvas(
                modifier = Modifier.fillMaxSize(),
                driverProgress = driverProgress,
                showDriver = order.status >= OrderStatus.AT_RESTAURANT,
                destinationLabel = order.deliveryAddress
            )

            // 2. Top Floating Navigation Bar (Back + Help Capsule)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .clickable { onBackClick() }
                        .testTag("delivery_back_btn"),
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 3.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "رجوع",
                            tint = AzoomaTextPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onHelpClick() }
                        .testTag("delivery_help_btn"),
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    shadowElevation = 3.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = AzoomaTextPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "مساعدة",
                            style = AppTypography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = AzoomaTextPrimary
                            )
                        )
                    }
                }
            }
        }

        // 3. Sliding Bottom Sheet / Tracking Card (Lower section, 60% height)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.60f),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp),
                contentPadding = PaddingValues(top = 10.dp, bottom = 32.dp)
            ) {
                // Drag handle
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color(0xFFD1D5DB))
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Delivery Status Title & Order Number (Video 00:43 - 02:49)
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = order.status.titleAr,
                            style = AppTypography.headlineSmall.copy(
                                fontWeight = FontWeight.Black,
                                color = when (order.status) {
                                    OrderStatus.PENDING -> AzoomaTextPrimary
                                    OrderStatus.PREPARING -> Color(0xFFEA580C)
                                    OrderStatus.AT_RESTAURANT -> Color(0xFFD97706)
                                    OrderStatus.ON_THE_WAY -> Color(0xFFE11D48)
                                    OrderStatus.DELIVERED -> Color(0xFF059669)
                                    else -> AzoomaTextPrimary
                                },
                                fontSize = 22.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Order Number", order.orderNumber)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "تم نسخ رقم الطلب: ${order.orderNumber}", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "نسخ",
                                tint = Color(0xFF9CA3AF),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "الطلب رقم ${order.orderNumber}",
                                style = AppTypography.bodySmall.copy(
                                    color = AzoomaTextSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 4. 5-Step Horizontal Stepper (Video 00:43 - 02:49)
                item {
                    Delivery5StepStepper(
                        currentStatus = order.status,
                        orderTime = order.timeText,
                        onStepClick = onAdvanceStatus
                    )

                    // Advance Simulator Button
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF3F4F6),
                            modifier = Modifier.clickable { onAdvanceStatus() }
                        ) {
                            Text(
                                text = "محاكاة: الانتقال للمرحلة التالية ⏩",
                                style = AppTypography.labelSmall.copy(
                                    color = Color(0xFF4B5563),
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 5. Customer Delivery Verification Code Card (THE REQUESTED ROOM DB FEATURE!)
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp)),
                        color = Color(0xFFFFF7ED),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFFD8A8))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFFFECE5)
                                ) {
                                    Text(
                                        text = "تأكيد سحابي مباشر ⚡",
                                        style = AppTypography.labelSmall.copy(
                                            color = AzoomaOrange,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }

                                Text(
                                    text = "رقم تسلسلي لتأكيد التسليم",
                                    style = AppTypography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = AzoomaTextPrimary
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Large 4-digit code display
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                order.deliveryVerificationCode.forEach { char ->
                                    Surface(
                                        modifier = Modifier.size(46.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color.White,
                                        border = androidx.compose.foundation.BorderStroke(1.5.dp, AzoomaOrange),
                                        shadowElevation = 2.dp
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = char.toString(),
                                                style = AppTypography.headlineMedium.copy(
                                                    fontWeight = FontWeight.Black,
                                                    color = AzoomaOrange,
                                                    fontSize = 24.sp
                                                )
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "أعطِ هذا الرقم للدلفري عند استلام وجبتك ليقوم بإدخاله وتأكيد التوصيل",
                                style = AppTypography.bodySmall.copy(
                                    color = Color(0xFF78350F),
                                    textAlign = TextAlign.Center
                                )
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Button to simulate driver handover input
                            Button(
                                onClick = { showDriverCodeDialog = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .testTag("driver_verify_handover_btn"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AzoomaOrange)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "واجهة الدلفري: إدخال كود العميل لتأكيد التوصيل",
                                        style = AppTypography.labelLarge.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 6. Driver Profile Card with Call & In-App Chat (Video 02:14 - 02:40)
                if (order.status >= OrderStatus.AT_RESTAURANT) {
                    item {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFF9FAFB),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Action buttons: Call & Chat (Left side in RTL)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    // Chat button
                                    IconButton(
                                        onClick = onChatDriver,
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFEFF6FF))
                                            .testTag("driver_chat_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ChatBubble,
                                            contentDescription = "مراسلة السائق",
                                            tint = Color(0xFF2563EB),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    // Call button
                                    IconButton(
                                        onClick = onCallDriver,
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFE8F5E9))
                                            .testTag("driver_call_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Phone,
                                            contentDescription = "اتصال بالسائق",
                                            tint = Color(0xFF16A34A),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                // Driver Details (Right side in RTL)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = order.driverName,
                                            style = AppTypography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = AzoomaTextPrimary
                                            )
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "⭐ ${order.driverRating} (${order.driverRatingCount})",
                                                style = AppTypography.labelSmall.copy(
                                                    color = Color(0xFFF59E0B),
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    // Driver Orange Helmet Avatar
                                    Surface(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(CircleShape),
                                        color = Color(0xFFFFECE5)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(text = "🛵", fontSize = 24.sp)
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                    }
                }

                // 7. Delivery Destination Card (Video 00:43)
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp)),
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFF9FAFB),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Map icon
                            Surface(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                color = Color(0xFFF3F4F6)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(text = "🗺️", fontSize = 20.sp)
                                }
                            }

                            // Address label
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "عنوان التوصيل",
                                    style = AppTypography.bodySmall.copy(
                                        color = AzoomaTextSecondary,
                                        fontSize = 11.sp
                                    )
                                )
                                Text(
                                    text = order.deliveryAddress,
                                    style = AppTypography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = AzoomaTextPrimary
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }

                // 8. Store Collapsible Card ("طلبت من: شنابو - الرحبه")
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { isStoreExpanded = !isStoreExpanded },
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFF9FAFB),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isStoreExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = Color(0xFF6B7280)
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "طلبت من",
                                        style = AppTypography.bodySmall.copy(
                                            color = AzoomaTextSecondary,
                                            fontSize = 11.sp
                                        )
                                    )
                                    Text(
                                        text = order.storeName,
                                        style = AppTypography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = AzoomaTextPrimary
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Surface(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape),
                                    color = Color.White,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(text = "🍔", fontSize = 22.sp)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                }

                // 9. Order Items List
                if (isStoreExpanded) {
                    item {
                        Text(
                            text = "عناصر الطلب (${order.itemsSummary.sumOf { it.second }})",
                            style = AppTypography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = AzoomaTextPrimary
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.End
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        order.itemsSummary.forEach { (itemName, qty) ->
                            OrderItemRow(itemName = itemName, quantity = qty, price = 30.0)
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // 10. Invoice Section
                    item {
                        Text(
                            text = "الفاتورة",
                            style = AppTypography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = AzoomaTextPrimary
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.End
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF9FAFB),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                InvoiceLineItem(title = "المدفوع نقداً", valueText = "${order.paidCash.toInt()} د.ل")
                                Spacer(modifier = Modifier.height(8.dp))
                                InvoiceLineItem(title = "المدفوع محفظة (${order.paymentMethodName})", valueText = "${order.paidElectronic.toInt()} د.ل")
                                Spacer(modifier = Modifier.height(8.dp))
                                InvoiceLineItem(title = "سعر العناصر", valueText = "${order.subtotal.toInt()} د.ل")
                                Spacer(modifier = Modifier.height(8.dp))
                                InvoiceLineItem(title = "سعر التوصيل", valueText = "${order.deliveryFee.toInt()} د.ل")
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFFFFF1EB)
                                    ) {
                                        Text(
                                            text = "مجاني",
                                            style = AppTypography.labelSmall.copy(
                                                color = AzoomaOrange,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }

                                    Text(
                                        text = "رسوم الخدمة",
                                        style = AppTypography.bodyMedium.copy(color = AzoomaTextSecondary)
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(color = Color(0xFFE5E7EB))
                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${order.totalPrice.toInt()} د.ل",
                                        style = AppTypography.titleLarge.copy(
                                            fontWeight = FontWeight.Black,
                                            color = AzoomaTextPrimary
                                        )
                                    )

                                    Text(
                                        text = "الإجمالي",
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
    } // end Column

        // 11. Top Animated Push Notification Dropdown (Video 02:44)
        AnimatedVisibility(
            visible = activeNotification != null,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onDismissNotification() },
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF1E2124),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE11D48)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "#${order.orderNumber} - تنبيه جديد",
                            style = AppTypography.titleSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = activeNotification ?: "",
                            style = AppTypography.bodySmall.copy(color = Color(0xFFD1D5DB))
                        )
                    }

                    IconButton(
                        onClick = onDismissNotification,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إغلاق",
                            tint = Color(0xFF9CA3AF),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // 12. Driver Handover Verification Dialog (The user's requested feature!)
        if (showDriverCodeDialog) {
            DriverHandoverVerificationDialog(
                orderNumber = order.orderNumber,
                expectedCode = order.deliveryVerificationCode,
                onDismiss = { showDriverCodeDialog = false },
                onConfirm = { enteredCode ->
                    showDriverCodeDialog = false
                    onVerifyDeliveryCode(enteredCode)
                }
            )
        }
    } // end Box
}

/**
 * 5-Step Horizontal Stepper matching the video (00:43 - 02:49):
 * 1. Clock (⏱️) -> "قيد الإنتظار" (8:10 م)
 * 2. Cooking Pot (🍳♨️) -> "جار التجهيز" (8:12 م)
 * 3. Restaurant (🏪) -> "في المطعم" (8:32 م)
 * 4. Motorbike (🛵) -> "في الطريق" (8:38 م)
 * 5. Door (🚪) -> "تم التوصيل"
 */
@Composable
private fun Delivery5StepStepper(
    currentStatus: OrderStatus,
    orderTime: String,
    onStepClick: () -> Unit
) {
    val stepIndex = currentStatus.stepIndex

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Step 1: Clock (⏱️) - Rightmost in RTL
        StepperNode(
            icon = Icons.Default.AccessTime,
            isActive = stepIndex >= 0,
            isCurrent = stepIndex == 0,
            timeText = orderTime,
            onClick = onStepClick
        )

        StepperConnector(isPassed = stepIndex >= 1)

        // Step 2: Cooking Pot (🍳♨️)
        StepperNode(
            icon = Icons.Default.SoupKitchen,
            isActive = stepIndex >= 1,
            isCurrent = stepIndex == 1,
            timeText = if (stepIndex >= 1) "8:12 م" else "—",
            onClick = onStepClick
        )

        StepperConnector(isPassed = stepIndex >= 2)

        // Step 3: Restaurant Storefront (🏪)
        StepperNode(
            icon = Icons.Default.Store,
            isActive = stepIndex >= 2,
            isCurrent = stepIndex == 2,
            timeText = if (stepIndex >= 2) "8:32 م" else "—",
            onClick = onStepClick
        )

        StepperConnector(isPassed = stepIndex >= 3)

        // Step 4: Motorbike Rider (🛵)
        StepperNode(
            icon = Icons.Default.TwoWheeler,
            isActive = stepIndex >= 3,
            isCurrent = stepIndex == 3,
            timeText = if (stepIndex >= 3) "8:38 م" else "—",
            onClick = onStepClick
        )

        StepperConnector(isPassed = stepIndex >= 4)

        // Step 5: Door (🚪) - Leftmost in RTL
        StepperNode(
            icon = Icons.Default.MeetingRoom,
            isActive = stepIndex >= 4,
            isCurrent = stepIndex == 4,
            timeText = if (stepIndex >= 4) "تم" else "—",
            onClick = onStepClick
        )
    }
}

@Composable
private fun StepperNode(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isActive: Boolean,
    isCurrent: Boolean,
    timeText: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Surface(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .border(
                    width = 2.dp,
                    color = when {
                        isCurrent -> Color(0xFFE11D48)
                        isActive -> AzoomaOrange
                        else -> Color(0xFFD1D5DB)
                    },
                    shape = CircleShape
                ),
            shape = CircleShape,
            color = when {
                isCurrent -> Color(0xFFFFECE5)
                isActive -> Color(0xFFFFF7ED)
                else -> Color(0xFFF9FAFB)
            }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = when {
                        isCurrent -> Color(0xFFE11D48)
                        isActive -> AzoomaOrange
                        else -> Color(0xFF9CA3AF)
                    },
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = timeText,
            style = AppTypography.labelSmall.copy(
                color = if (isActive) Color(0xFFE11D48) else Color(0xFF9CA3AF),
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            )
        )
    }
}

@Composable
private fun RowScope.StepperConnector(isPassed: Boolean) {
    val color by animateColorAsState(
        targetValue = if (isPassed) Color(0xFFE11D48) else Color(0xFFE5E7EB),
        animationSpec = tween(400),
        label = "connector"
    )

    Box(
        modifier = Modifier
            .weight(1f)
            .height(2.5.dp)
            .padding(horizontal = 2.dp)
            .background(color, RoundedCornerShape(1.5.dp))
    )
}

/**
 * Driver Handover Verification Dialog (The user's key feature connected to Room DB):
 * Driver inputs the customer's 4-digit code to complete the delivery.
 */
@Composable
private fun DriverHandoverVerificationDialog(
    orderNumber: String,
    expectedCode: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var enteredCode by remember { mutableStateOf("") }
    var hasError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier.size(54.dp),
                    shape = CircleShape,
                    color = Color(0xFFFFECE5)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = "🛵", fontSize = 28.sp)
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "تأكيد تسليم الطلب (واجهة السائق)",
                    style = AppTypography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = AzoomaTextPrimary
                    ),
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "أدخل الرقم التسلسلي الذي أعطاك إياه العميل للتحقق من قاعدة البيانات وإنهاء التوصيل:",
                    style = AppTypography.bodySmall.copy(
                        color = AzoomaTextSecondary,
                        textAlign = TextAlign.Center
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = enteredCode,
                    onValueChange = {
                        if (it.length <= 6) {
                            enteredCode = it
                            hasError = false
                        }
                    },
                    placeholder = { Text("أدخل الكود هنا (4 أرقام)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = hasError,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("driver_code_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                if (hasError) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "الكود غير صحيح! يرجى التأكد من العميل",
                        style = AppTypography.labelSmall.copy(color = MaterialTheme.colorScheme.error)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Quick Paste button for tester
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF3F4F6),
                    modifier = Modifier.clickable {
                        enteredCode = expectedCode
                        hasError = false
                    }
                ) {
                    Text(
                        text = "📋 لصق كود العميل ($expectedCode) للاختبار",
                        style = AppTypography.labelSmall.copy(
                            color = Color(0xFF374151),
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (enteredCode.trim() == expectedCode.trim()) {
                        onConfirm(enteredCode.trim())
                    } else {
                        hasError = true
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AzoomaOrange),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("submit_driver_code_btn")
            ) {
                Text("تأكيد التسليم وإنهاء الطلب", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = AzoomaTextSecondary)
            }
        }
    )
}

/**
 * Delivered Final Completion Screen ("وصلنا لك طلبك 😎") matching the video at 02:51 - 03:00:
 * - Header: وصلنا لك طلبك 😎
 * - 5-star experience rating
 * - Driver name & avatar
 * - Destination address
 * - Items list
 * - Invoice
 * - Send receipt via email
 * - Reorder button
 */
@Composable
private fun DeliveredCompletionScreen(
    order: Order,
    userRating: Int,
    onRatingChange: (Int) -> Unit,
    emailInput: String,
    onEmailChange: (String) -> Unit,
    emailSent: Boolean,
    onSendEmail: () -> Unit,
    onBackClick: () -> Unit,
    onReorderClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding(),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 16.dp)
    ) {
        // Top Navigation Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "رجوع",
                        tint = AzoomaTextPrimary
                    )
                }

                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = AzoomaTextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
        }

        // Header Title: "وصلنا لك طلبك 😎"
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "وصلنا لك طلبك 😎",
                    style = AppTypography.headlineMedium.copy(
                        fontWeight = FontWeight.Black,
                        color = AzoomaTextPrimary,
                        fontSize = 24.sp
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "4 أكتوبر، 2026، 8:10 م",
                        style = AppTypography.bodySmall.copy(color = AzoomaTextSecondary)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "الطلب رقم ${order.orderNumber}",
                        style = AppTypography.bodySmall.copy(
                            color = AzoomaTextSecondary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
        }

        // Card 1: "كيف كانت تجربتك مع الطلب؟" with 5 Stars
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFF9FAFB),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(44.dp),
                            shape = CircleShape,
                            color = Color(0xFFFFECE5)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = "🎁", fontSize = 24.sp)
                            }
                        }

                        Text(
                            text = "كيف كانت تجربتك مع الطلب؟",
                            style = AppTypography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = AzoomaTextPrimary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 5 Clickable Stars
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 1..5) {
                            IconButton(
                                onClick = { onRatingChange(i) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = if (i <= userRating) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = "$i نجوم",
                                    tint = if (i <= userRating) Color(0xFFF59E0B) else Color(0xFFD1D5DB),
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        // Card 2: Driver Name
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFF9FAFB),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(42.dp),
                        shape = CircleShape,
                        color = Color(0xFFFFECE5)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = "🛵", fontSize = 22.sp)
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "اسم السائق",
                            style = AppTypography.bodySmall.copy(color = AzoomaTextSecondary)
                        )
                        Text(
                            text = order.driverName,
                            style = AppTypography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = AzoomaTextPrimary
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        // Card 3: Destination Address
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFF9FAFB),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(42.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF3F4F6)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = "🗺️", fontSize = 22.sp)
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "عنوان التوصيل",
                            style = AppTypography.bodySmall.copy(color = AzoomaTextSecondary)
                        )
                        Text(
                            text = order.deliveryAddress,
                            style = AppTypography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = AzoomaTextPrimary
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Order Items List
        item {
            Text(
                text = "عناصر الطلب (${order.itemsSummary.sumOf { it.second }})",
                style = AppTypography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = AzoomaTextPrimary
                ),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )

            Spacer(modifier = Modifier.height(8.dp))

            order.itemsSummary.forEach { (itemName, qty) ->
                OrderItemRow(itemName = itemName, quantity = qty, price = 30.0)
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Invoice
        item {
            Text(
                text = "الفاتورة",
                style = AppTypography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = AzoomaTextPrimary
                ),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFF9FAFB),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    InvoiceLineItem(title = "المدفوع نقداً", valueText = "${order.paidCash.toInt()} د.ل")
                    Spacer(modifier = Modifier.height(8.dp))
                    InvoiceLineItem(title = "المدفوع محفظة (${order.paymentMethodName})", valueText = "${order.paidElectronic.toInt()} د.ل")
                    Spacer(modifier = Modifier.height(8.dp))
                    InvoiceLineItem(title = "سعر العناصر", valueText = "${order.subtotal.toInt()} د.ل")
                    Spacer(modifier = Modifier.height(8.dp))
                    InvoiceLineItem(title = "سعر التوصيل", valueText = "${order.deliveryFee.toInt()} د.ل")
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFFFF1EB)
                        ) {
                            Text(
                                text = "مجاني",
                                style = AppTypography.labelSmall.copy(
                                    color = AzoomaOrange,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }

                        Text(
                            text = "رسوم الخدمة",
                            style = AppTypography.bodyMedium.copy(color = AzoomaTextSecondary)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = Color(0xFFE5E7EB))
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${order.totalPrice.toInt()} د.ل",
                            style = AppTypography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                color = AzoomaTextPrimary
                            )
                        )

                        Text(
                            text = "الإجمالي",
                            style = AppTypography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = AzoomaTextPrimary
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
        }

        // Email Receipt Section (Video 02:56)
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFF9FAFB),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onSendEmail,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AzoomaOrange),
                        modifier = Modifier.testTag("send_receipt_btn")
                    ) {
                        Text("إرسال", fontWeight = FontWeight.Bold)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "إرسال الفاتورة عبر البريد",
                            style = AppTypography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = AzoomaTextPrimary
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = Color(0xFF6B7280),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
        }

        // Reorder Button (Video 02:57)
        item {
            OutlinedButton(
                onClick = onReorderClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("reorder_delivered_btn"),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, AzoomaOrange)
            ) {
                Text(
                    text = "إعادة الطلب",
                    style = AppTypography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = AzoomaOrange
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * Pickup Order Lifecycle Tracking View (from previous implementation)
 */
@Composable
private fun PickupOrderTrackingView(
    order: Order,
    currentStatus: PickupStatus,
    activeNotification: String?,
    onBackClick: () -> Unit,
    onHelpClick: () -> Unit,
    onAdvancePickupStatus: () -> Unit,
    onDismissNotification: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isStoreExpanded by remember { mutableStateOf(true) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFE9EDF0))
    ) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.40f)
        ) {
            InteractiveMapCanvas(
                modifier = Modifier.fillMaxSize(),
                driverProgress = 0f,
                showDriver = false,
                destinationLabel = "الرحبه - شنابو"
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .clickable { onBackClick() },
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 3.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "رجوع",
                            tint = AzoomaTextPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onHelpClick() },
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    shadowElevation = 3.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = AzoomaTextPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "مساعدة",
                            style = AppTypography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = AzoomaTextPrimary
                            )
                        )
                    }
                }
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.60f),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp),
                contentPadding = PaddingValues(top = 10.dp, bottom = 32.dp)
            ) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color(0xFFD1D5DB))
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.End
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Pickup Code", order.pickupNumber)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "تم نسخ رقم الاستلام: ${order.pickupNumber}", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "نسخ",
                                tint = Color(0xFF6B7280),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "رقم الاستلام  ${order.pickupNumber}",
                                style = AppTypography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = AzoomaTextPrimary,
                                    fontSize = 20.sp
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = currentStatus.titleAr,
                            style = AppTypography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                color = when (currentStatus) {
                                    PickupStatus.PENDING -> AzoomaTextPrimary
                                    PickupStatus.PREPARING -> Color(0xFFE11D48)
                                    PickupStatus.READY -> Color(0xFF059669)
                                    PickupStatus.COMPLETED -> Color(0xFF2563EB)
                                },
                                fontSize = 21.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "الطلب رقم ${order.orderNumber}",
                            style = AppTypography.bodySmall.copy(
                                color = AzoomaTextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }

                item {
                    PickupStepperRow(
                        currentStatus = currentStatus,
                        orderTime = order.timeText,
                        onStepClick = onAdvancePickupStatus
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF3F4F6),
                            modifier = Modifier.clickable { onAdvancePickupStatus() }
                        ) {
                            Text(
                                text = "محاكاة: الانتقال للمرحلة التالية ⏩",
                                style = AppTypography.labelSmall.copy(
                                    color = Color(0xFF4B5563),
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                }

                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { isStoreExpanded = !isStoreExpanded },
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFF9FAFB),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isStoreExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = Color(0xFF6B7280)
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "طلبت من",
                                        style = AppTypography.bodySmall.copy(
                                            color = AzoomaTextSecondary,
                                            fontSize = 12.sp
                                        )
                                    )
                                    Text(
                                        text = order.storeName,
                                        style = AppTypography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = AzoomaTextPrimary
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Surface(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape),
                                    color = Color.White,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(text = "🍔", fontSize = 24.sp)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                }

                if (isStoreExpanded) {
                    item {
                        Text(
                            text = "عناصر الطلب (${order.itemsSummary.sumOf { it.second }})",
                            style = AppTypography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = AzoomaTextPrimary
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.End
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        order.itemsSummary.forEach { (itemName, qty) ->
                            OrderItemRow(itemName = itemName, quantity = qty, price = 13.0)
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    item {
                        Text(
                            text = "الفاتورة",
                            style = AppTypography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = AzoomaTextPrimary
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.End
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF9FAFB),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                InvoiceLineItem(title = "المدفوع نقداً", valueText = "${order.paidCash.toInt()} د.ل")
                                Spacer(modifier = Modifier.height(8.dp))
                                InvoiceLineItem(title = "المدفوع إلكترونياً (${order.paymentMethodName})", valueText = "${order.paidElectronic.toInt()} د.ل")
                                Spacer(modifier = Modifier.height(8.dp))
                                InvoiceLineItem(title = "سعر العناصر", valueText = "${order.subtotal.toInt()} د.ل")
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFFFFF1EB)
                                    ) {
                                        Text(
                                            text = "مجاني",
                                            style = AppTypography.labelSmall.copy(
                                                color = AzoomaOrange,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }

                                    Text(
                                        text = "سعر التوصيل",
                                        style = AppTypography.bodyMedium.copy(color = AzoomaTextSecondary)
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFFFFF1EB)
                                    ) {
                                        Text(
                                            text = "مجاني",
                                            style = AppTypography.labelSmall.copy(
                                                color = AzoomaOrange,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }

                                    Text(
                                        text = "رسوم الخدمة",
                                        style = AppTypography.bodyMedium.copy(color = AzoomaTextSecondary)
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(color = Color(0xFFE5E7EB))
                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${order.totalPrice.toInt()} د.ل",
                                        style = AppTypography.titleLarge.copy(
                                            fontWeight = FontWeight.Black,
                                            color = AzoomaTextPrimary
                                        )
                                    )

                                    Text(
                                        text = "الإجمالي",
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
    } // end Column

        AnimatedVisibility(
            visible = activeNotification != null,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onDismissNotification() },
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF1E2124),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE11D48)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "#${order.orderNumber} - تم تأكيد الطلب",
                            style = AppTypography.titleSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = activeNotification ?: "جاري تحضير طلبك، تابع معنا!",
                            style = AppTypography.bodySmall.copy(color = Color(0xFFD1D5DB))
                        )
                    }

                    IconButton(
                        onClick = onDismissNotification,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إغلاق",
                            tint = Color(0xFF9CA3AF),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    } // end Box
}


@Composable
private fun PickupStepperRow(
    currentStatus: PickupStatus,
    orderTime: String,
    onStepClick: () -> Unit
) {
    val isStep2Active = currentStatus.stepIndex >= PickupStatus.PREPARING.stepIndex
    val isStep3Active = currentStatus.stepIndex >= PickupStatus.READY.stepIndex

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickable { onStepClick() }
        ) {
            Surface(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .border(width = 2.dp, color = Color(0xFFE11D48), shape = CircleShape),
                shape = CircleShape,
                color = Color(0xFFFFECE5)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = "قيد الإنتظار",
                        tint = Color(0xFFE11D48),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = orderTime,
                style = AppTypography.labelSmall.copy(
                    color = Color(0xFFE11D48),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .height(3.dp)
                .padding(horizontal = 4.dp)
                .background(if (isStep2Active) Color(0xFFE11D48) else Color(0xFFE5E7EB), RoundedCornerShape(1.5.dp))
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickable { onStepClick() }
        ) {
            Surface(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .border(
                        width = 2.dp,
                        color = if (isStep2Active) Color(0xFFE11D48) else Color(0xFFD1D5DB),
                        shape = CircleShape
                    ),
                shape = CircleShape,
                color = if (isStep2Active) Color(0xFFFFECE5) else Color(0xFFF9FAFB)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.SoupKitchen,
                        contentDescription = "جار التجهيز",
                        tint = if (isStep2Active) Color(0xFFE11D48) else Color(0xFF9CA3AF),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (isStep2Active) orderTime else "—",
                style = AppTypography.labelSmall.copy(
                    color = if (isStep2Active) Color(0xFFE11D48) else Color(0xFF9CA3AF),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .height(3.dp)
                .padding(horizontal = 4.dp)
                .background(if (isStep3Active) Color(0xFFE11D48) else Color(0xFFE5E7EB), RoundedCornerShape(1.5.dp))
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickable { onStepClick() }
        ) {
            Surface(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .border(
                        width = 2.dp,
                        color = if (isStep3Active) Color(0xFF059669) else Color(0xFFD1D5DB),
                        shape = CircleShape
                    ),
                shape = CircleShape,
                color = if (isStep3Active) Color(0xFFECFDF5) else Color(0xFFF9FAFB)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.DirectionsWalk,
                        contentDescription = "جاهز للاستلام",
                        tint = if (isStep3Active) Color(0xFF059669) else Color(0xFF9CA3AF),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (isStep3Active) "جاهز!" else "—",
                style = AppTypography.labelSmall.copy(
                    color = if (isStep3Active) Color(0xFF059669) else Color(0xFF9CA3AF),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            )
        }
    }
}

@Composable
private fun OrderItemRow(
    itemName: String,
    quantity: Int,
    price: Double
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF3F4F6))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${(price * quantity).toInt()} د.ل",
                style = AppTypography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = AzoomaTextPrimary
                )
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = itemName,
                    style = AppTypography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        color = AzoomaTextPrimary
                    )
                )

                Spacer(modifier = Modifier.width(12.dp))

                Box(modifier = Modifier.size(44.dp)) {
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp)),
                        color = Color(0xFFF3F4F6)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = "🍗", fontSize = 22.sp)
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = 4.dp, y = 4.dp),
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFF1E2124)
                    ) {
                        Text(
                            text = "x$quantity",
                            style = AppTypography.labelSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            ),
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InvoiceLineItem(
    title: String,
    valueText: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = valueText,
            style = AppTypography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = AzoomaTextPrimary
            )
        )

        Text(
            text = title,
            style = AppTypography.bodyMedium.copy(color = AzoomaTextSecondary)
        )
    }
}
