package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun CouponsScreen(
    activeCoupons: List<String>,
    onAddCoupon: (String) -> Boolean,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var couponInput by remember { mutableStateOf("") }
    var selectedFilterIndex by remember { mutableIntStateOf(0) } // 0: نشطة, 1: غير نشطة
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        // Top Bar (Screenshot 11 & 12)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.testTag("coupons_back_btn")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "رجوع",
                    tint = AzoomaTextPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Text(
                text = "كوبونات",
                style = AppTypography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = AzoomaTextPrimary
                )
            )

            Spacer(modifier = Modifier.size(48.dp))
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Coupon Code Input Card with Add Button
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp)),
                color = Color.White
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // "إضافة" text button on the left (RTL start)
                    TextButton(
                        onClick = {
                            if (couponInput.isNotBlank()) {
                                val added = onAddCoupon(couponInput.trim())
                                if (added) {
                                    successMessage = "تم تفعيل الكوبون بنجاح!"
                                    errorMessage = null
                                    couponInput = ""
                                } else {
                                    errorMessage = "الكوبون غير صالح أو تم تفعيله مسبقاً"
                                    successMessage = null
                                }
                            }
                        }
                    ) {
                        Text(
                            text = "إضافة",
                            style = AppTypography.labelLarge.copy(
                                color = if (couponInput.isNotBlank()) AzoomaOrange else Color(0xFF9E9E9E),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        )
                    }

                    // Input Field in center
                    Box(modifier = Modifier.weight(1f)) {
                        if (couponInput.isEmpty()) {
                            Text(
                                text = "أدخل رمز الكوبون",
                                style = AppTypography.bodyMedium.copy(color = Color(0xFF9E9E9E)),
                                modifier = Modifier.align(Alignment.CenterEnd)
                            )
                        }
                        androidx.compose.foundation.text.BasicTextField(
                            value = couponInput,
                            onValueChange = {
                                couponInput = it
                                errorMessage = null
                                successMessage = null
                            },
                            textStyle = AppTypography.bodyMedium.copy(
                                color = AzoomaTextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.End
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Ticket Icon on the right (RTL start)
                    Icon(
                        imageVector = Icons.Default.ConfirmationNumber,
                        contentDescription = "كوبون",
                        tint = Color(0xFF6B7280),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            if (successMessage != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = successMessage!!,
                    style = AppTypography.bodySmall.copy(color = Color(0xFF10B981), fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = errorMessage!!,
                    style = AppTypography.bodySmall.copy(color = Color(0xFFEF4444), fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Two Filter Pills: نشطة | غير نشطة
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                // "نشطة" Pill
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { selectedFilterIndex = 0 },
                    color = if (selectedFilterIndex == 0) Color(0xFFFFECE5) else Color.White,
                    shape = RoundedCornerShape(20.dp),
                    border = if (selectedFilterIndex == 0) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
                ) {
                    Text(
                        text = "نشطة",
                        style = AppTypography.labelLarge.copy(
                            color = if (selectedFilterIndex == 0) AzoomaOrange else Color(0xFF6B7280),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        ),
                        modifier = Modifier.padding(horizontal = 22.dp, vertical = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // "غير نشطة" Pill
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { selectedFilterIndex = 1 },
                    color = if (selectedFilterIndex == 1) Color(0xFFFFECE5) else Color.White,
                    shape = RoundedCornerShape(20.dp),
                    border = if (selectedFilterIndex == 1) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
                ) {
                    Text(
                        text = "غير نشطة",
                        style = AppTypography.labelLarge.copy(
                            color = if (selectedFilterIndex == 1) AzoomaOrange else Color(0xFF6B7280),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        ),
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    )
                }
            }
        }

        // Content Area
        if (selectedFilterIndex == 0) {
            // Active Coupons
            if (activeCoupons.isEmpty()) {
                EmptyCouponsState(
                    title = "لا توجد كوبونات متاحة",
                    subtitle = "عندما تحصل على كوبون جديد، سيظهر هنا."
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(activeCoupons) { coupon ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFFFF7ED),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, AzoomaOrange.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = AzoomaOrange
                                ) {
                                    Text(
                                        text = "خصم 20%",
                                        style = AppTypography.labelSmall.copy(color = Color.White, fontWeight = FontWeight.Bold),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = coupon,
                                        style = AppTypography.titleMedium.copy(fontWeight = FontWeight.Bold, color = AzoomaTextPrimary)
                                    )
                                    Text(
                                        text = "صالح على جميع الطلبات حتى نهاية الشهر",
                                        style = AppTypography.bodySmall.copy(color = AzoomaTextSecondary, fontSize = 11.sp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Inactive Coupons
            EmptyCouponsState(
                title = "لا توجد كوبونات غير متاحة",
                subtitle = "بعد استخدامك لأي كوبون أو عند انتهاء صلاحيته، سيتم حفظه هنا."
            )
        }
    }
}

@Composable
private fun EmptyCouponsState(
    title: String,
    subtitle: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 3D Orange Ticket Illustration (Screenshot 11 & 12)
        Box(
            modifier = Modifier.size(140.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier.size(100.dp),
                shape = CircleShape,
                color = Color(0xFFFFF5EB)
            ) {}
            Text(
                text = "🎟️",
                fontSize = 72.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = title,
            style = AppTypography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = AzoomaTextPrimary,
                fontSize = 20.sp
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = subtitle,
            style = AppTypography.bodyMedium.copy(
                color = AzoomaTextSecondary,
                lineHeight = 22.sp,
                fontSize = 14.sp
            ),
            textAlign = TextAlign.Center
        )
    }
}
