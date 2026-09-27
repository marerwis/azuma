package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
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
import com.example.model.PaymentType
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentMethodSheet(
    selectedType: PaymentType,
    walletBalance: Double,
    totalAmount: Double,
    onSelect: (PaymentType) -> Unit,
    onDismiss: () -> Unit,
    onRechargeClick: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(48.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFFD1D5DB))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(
                text = "حدد طريقة الدفع",
                style = AppTypography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = AzoomaTextPrimary
                ),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(PaymentType.values()) { type ->
                    val isSelected = type == selectedType
                    PaymentOptionRow(
                        type = type,
                        isSelected = isSelected,
                        walletBalance = walletBalance,
                        onRechargeClick = onRechargeClick,
                        onClick = { onSelect(type) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "اضغط مطولاً على طريقة الدفع لتحديدها كافتراضية.",
                style = AppTypography.bodySmall.copy(
                    color = AzoomaTextMuted,
                    fontSize = 11.sp
                ),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("confirm_payment_method_btn"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AzoomaOrange)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "استمرار",
                        style = AppTypography.titleMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "%.2f د.ل".format(totalAmount),
                        style = AppTypography.titleMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PaymentOptionRow(
    type: PaymentType,
    isSelected: Boolean,
    walletBalance: Double,
    onRechargeClick: () -> Unit,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) AzoomaOrange else AzoomaCardBorder,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onClick() },
        color = Color.White,
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Radio Circle
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .border(
                        width = if (isSelected) 6.dp else 1.5.dp,
                        color = if (isSelected) AzoomaOrange else Color(0xFFCCCCCC),
                        shape = CircleShape
                    )
                    .background(Color.White)
            )

            // Right side: Method Title & Badge/Balance
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                if (type == PaymentType.WALLET) {
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onRechargeClick() },
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, AzoomaOrange),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "+ شحن",
                                style = AppTypography.labelSmall.copy(
                                    color = AzoomaOrange,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "%.0f د.ل".format(walletBalance),
                        style = AppTypography.bodyMedium.copy(
                            color = AzoomaTextSecondary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Text(
                    text = type.titleAr,
                    style = AppTypography.titleMedium.copy(
                        color = AzoomaTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Brand Badge / Icon
                PaymentBrandIcon(type)
            }
        }
    }
}

@Composable
fun PaymentBrandIcon(type: PaymentType) {
    val (bgColor, iconText) = when (type) {
        PaymentType.CASH -> Color(0xFF28A745) to "$"
        PaymentType.WALLET -> Color(0xFFB07D62) to "💼"
        PaymentType.LIBYANA -> Color(0xFF6F42C1) to "LU"
        PaymentType.SADAD -> Color(0xFFFF9900) to "سداد"
        PaymentType.BANK_CARD -> Color(0xFF007BFF) to "💳"
        PaymentType.EDFA3LY -> Color(0xFF20C997) to "ادفعلي"
        PaymentType.MOBI_CASH -> Color(0xFF17A2B8) to "موبي"
        PaymentType.MASRAFI_PAY -> Color(0xFF0056B3) to "مصرفي"
        PaymentType.YUSR_ONLINE -> Color(0xFF343A40) to "يسر"
    }

    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = iconText,
            style = AppTypography.labelSmall.copy(
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = if (iconText.length > 2) 9.sp else 12.sp
            )
        )
    }
}
