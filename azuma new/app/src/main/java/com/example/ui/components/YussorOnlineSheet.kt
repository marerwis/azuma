package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YussorOnlineSheet(
    totalAmount: Double,
    onDismiss: () -> Unit,
    onPaymentSuccess: () -> Unit
) {
    var step by remember { mutableIntStateOf(1) } // 1: Purchase Code, 2: OTP Verification
    var purchaseCode by remember { mutableStateOf("331075373") }
    var otpDigits by remember { mutableStateOf(listOf("3", "5", "4", "7", "4", "8")) }
    var isVerifying by remember { mutableStateOf(false) }

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
        if (step == 1) {
            // STEP 1: Enter Purchase Code (رمز الشراء) - Video 01:44
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Back / Close row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "رجوع",
                            tint = AzoomaTextPrimary
                        )
                    }
                }

                // Yusor Logo
                Surface(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape),
                    color = Color(0xFFF1F5F9),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "J.S.B",
                                style = AppTypography.labelLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF0F766E),
                                    letterSpacing = 1.sp
                                )
                            )
                            Text(
                                text = "YUSSOR ONLINE",
                                style = AppTypography.labelSmall.copy(
                                    fontSize = 7.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F766E)
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "يسر أونلاين",
                    style = AppTypography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = AzoomaTextPrimary,
                        fontSize = 18.sp
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Input field: Purchase Code (رمز الشراء)
                OutlinedTextField(
                    value = purchaseCode,
                    onValueChange = { purchaseCode = it },
                    label = { Text("رمز الشراء") },
                    placeholder = { Text("أدخل رمز الشراء (مثال: 331075373)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("yussor_code_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AzoomaOrange,
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Amount display
                OutlinedTextField(
                    value = "%.1f".format(totalAmount),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("المبلغ (د.ل)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFE2E8F0),
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Confirm button
                Button(
                    onClick = {
                        if (purchaseCode.isNotBlank()) {
                            step = 2
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("yussor_confirm_btn"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AzoomaOrange)
                ) {
                    Text(
                        text = "تأكيد البيانات",
                        style = AppTypography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        } else {
            // STEP 2: Enter OTP Code (رمز التأكيد) - Video 02:10 - 02:44
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with back arrow
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { step = 1 }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "رجوع",
                            tint = AzoomaTextPrimary
                        )
                    }

                    Text(
                        text = "رمز التأكيد",
                        style = AppTypography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = AzoomaTextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.size(48.dp))
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 3D Red Chat Bubble Icon
                Surface(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape),
                    color = Color(0xFFFFECE5)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = null,
                            tint = AzoomaOrange,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "أدخل رمز التحقق",
                    style = AppTypography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = AzoomaTextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "لقد قمنا بإرسال رمز التحقق إلى هاتفك المصرفي",
                    style = AppTypography.bodySmall.copy(
                        color = AzoomaTextSecondary,
                        textAlign = TextAlign.Center
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 6 OTP Digit Boxes
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    otpDigits.forEachIndexed { index, digit ->
                        Surface(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .border(
                                    width = 1.5.dp,
                                    color = if (digit.isNotBlank()) AzoomaOrange else Color(0xFFD1D5DB),
                                    shape = RoundedCornerShape(10.dp)
                                ),
                            color = if (digit.isNotBlank()) Color(0xFFFFF7ED) else Color.White
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = digit,
                                    style = AppTypography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = AzoomaTextPrimary
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Verify Button
                Button(
                    onClick = {
                        isVerifying = true
                        onPaymentSuccess()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("yussor_verify_btn"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AzoomaOrange)
                ) {
                    if (isVerifying) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text(
                            text = "تحقق",
                            style = AppTypography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
