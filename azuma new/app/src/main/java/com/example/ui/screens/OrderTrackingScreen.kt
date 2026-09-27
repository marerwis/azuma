package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Order
import com.example.model.OrderStatus
import com.example.ui.components.InteractiveMapCanvas
import com.example.ui.theme.*

@Composable
fun OrderTrackingScreen(
    order: Order,
    driverProgress: Float,
    etaMinutes: Int,
    onBackClick: () -> Unit,
    onCallDriver: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AzoomaBackground),
        contentPadding = PaddingValues(bottom = 40.dp)
    ) {
        // Header
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

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "تتبع الطلب",
                        style = AppTypography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = AzoomaTextPrimary
                        )
                    )
                    Text(
                        text = "طلب #${order.orderNumber}",
                        style = AppTypography.bodySmall.copy(color = AzoomaTextSecondary)
                    )
                }

                Spacer(modifier = Modifier.size(40.dp))
            }
        }

        // Live Map Canvas with moving courier
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(20.dp)),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                InteractiveMapCanvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                    driverProgress = driverProgress,
                    showDriver = true,
                    destinationLabel = order.deliveryAddress
                )
            }
        }

        // Estimated Arrival ETA Card
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(16.dp)),
                color = AzoomaOrangeLight,
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = "الوقت المقدر للوصول",
                            style = AppTypography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = AzoomaOrangeDark
                            ),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Start
                        )
                        Text(
                            text = "السائق في طريقه إلى موقعك",
                            style = AppTypography.bodySmall.copy(color = AzoomaTextSecondary),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Start
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = AzoomaOrange
                    ) {
                        Text(
                            text = "$etaMinutes دقيقة",
                            style = AppTypography.titleMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // Stepper Progress (Accepted -> Preparing -> On the Way -> Delivered)
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(16.dp)),
                color = Color.White,
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "حالة الطلب",
                        style = AppTypography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = AzoomaTextPrimary
                        ),
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    TrackingStepRow(
                        title = "تم قبول الطلب",
                        subtitle = "تم استلام الطلب وتأكيده",
                        isCompleted = true,
                        isCurrent = false
                    )

                    TrackingStepDivider(isCompleted = true)

                    TrackingStepRow(
                        title = "قيد التحضير",
                        subtitle = "يقوم المطعم بتجهيز وجبتك الطازجة",
                        isCompleted = true,
                        isCurrent = false
                    )

                    TrackingStepDivider(isCompleted = true)

                    TrackingStepRow(
                        title = "في الطريق إليك",
                        subtitle = "الكابتن يستلم الطلب ومتوجه نحوك",
                        isCompleted = false,
                        isCurrent = true
                    )

                    TrackingStepDivider(isCompleted = false)

                    TrackingStepRow(
                        title = "تم التوصيل",
                        subtitle = "استلم طلبك وبالهناء والشفاء",
                        isCompleted = false,
                        isCurrent = false
                    )
                }
            }
        }

        // Driver Profile Card
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(16.dp)),
                color = Color.White,
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Driver info on the RIGHT (Start in RTL)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE2E8F0)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🛵", fontSize = 24.sp)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(horizontalAlignment = Alignment.Start) {
                            Text(
                                text = order.driverName,
                                style = AppTypography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = AzoomaTextPrimary
                                ),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Start
                            )
                            Text(
                                text = order.driverVehicle,
                                style = AppTypography.bodySmall.copy(color = AzoomaTextSecondary),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Start
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = AzoomaYellow,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "${order.driverRating}",
                                    style = AppTypography.bodySmall.copy(
                                        color = AzoomaTextSecondary,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }

                    // Call & Message buttons on the LEFT (End in RTL)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .clickable { onCallDriver() }
                                .testTag("call_driver_btn"),
                            color = AzoomaGreenLight,
                            shape = CircleShape
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = "اتصال",
                                    tint = AzoomaGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .clickable { /* Message */ },
                            color = AzoomaOrangeLight,
                            shape = CircleShape
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.ChatBubbleOutline,
                                    contentDescription = "محادثة",
                                    tint = AzoomaOrange,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Return to home button
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onBackClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(50.dp)
                    .testTag("tracking_back_home_btn"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AzoomaOrange)
            ) {
                Text(
                    text = "العودة للرئيسية",
                    style = AppTypography.titleMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

@Composable
private fun TrackingStepRow(
    title: String,
    subtitle: String,
    isCompleted: Boolean,
    isCurrent: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Step Node Circle on the RIGHT (Start in RTL)
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(
                    when {
                        isCompleted -> AzoomaGreen
                        isCurrent -> AzoomaOrange
                        else -> Color(0xFFE2E8F0)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            } else if (isCurrent) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        // 2. Step Text Description to the LEFT of the circle
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = title,
                style = AppTypography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isCompleted || isCurrent) AzoomaTextPrimary else AzoomaTextMuted
                ),
                textAlign = androidx.compose.ui.text.style.TextAlign.Start
            )
            Text(
                text = subtitle,
                style = AppTypography.bodySmall.copy(
                    color = if (isCurrent) AzoomaOrange else AzoomaTextSecondary,
                    fontSize = 11.sp
                ),
                textAlign = androidx.compose.ui.text.style.TextAlign.Start
            )
        }
    }
}

@Composable
private fun TrackingStepDivider(isCompleted: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .width(2.dp)
                .height(22.dp)
                .background(if (isCompleted) AzoomaGreen else Color(0xFFE2E8F0))
        )
    }
}
