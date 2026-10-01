package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackSheet(
    onDismiss: () -> Unit,
    onSubmitFeedback: (isProblem: Boolean, text: String, allowContact: Boolean) -> Unit
) {
    var selectedTypeIsProblem by remember { mutableStateOf(true) }
    var feedbackText by remember { mutableStateOf("") }
    var allowContact by remember { mutableStateOf(true) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
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
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 3D Speech Bubble Icon (Screenshots 18-20)
            Box(
                modifier = Modifier.size(100.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = CircleShape,
                    color = Color(0xFFFFECE5)
                ) {}
                Text(
                    text = "💬",
                    fontSize = 54.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Title: "شاركنا آرائك"
            Text(
                text = "شاركنا آرائك",
                style = AppTypography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = AzoomaTextPrimary,
                    fontSize = 22.sp
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Subtitle: "رأيك يهمنا ويساعدنا على تحسين خدماتنا."
            Text(
                text = "رأيك يهمنا ويساعدنا على تحسين خدماتنا.",
                style = AppTypography.bodyMedium.copy(
                    color = AzoomaTextSecondary,
                    fontSize = 14.sp
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Toggle Bar: مشكلة | ملاحظة عامة (Screenshots 18-20)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFFF3F5F7)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp)
                ) {
                    // "مشكلة" Tab
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { selectedTypeIsProblem = true },
                        color = if (selectedTypeIsProblem) Color.White else Color.Transparent,
                        shape = RoundedCornerShape(20.dp),
                        shadowElevation = if (selectedTypeIsProblem) 2.dp else 0.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "مشكلة",
                                style = AppTypography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedTypeIsProblem) AzoomaTextPrimary else Color(0xFF6B7280)
                                )
                            )
                        }
                    }

                    // "ملاحظة عامة" Tab
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { selectedTypeIsProblem = false },
                        color = if (!selectedTypeIsProblem) Color.White else Color.Transparent,
                        shape = RoundedCornerShape(20.dp),
                        shadowElevation = if (!selectedTypeIsProblem) 2.dp else 0.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "ملاحظة عامة",
                                style = AppTypography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (!selectedTypeIsProblem) AzoomaTextPrimary else Color(0xFF6B7280)
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Text Area Input
            OutlinedTextField(
                value = feedbackText,
                onValueChange = { feedbackText = it },
                placeholder = {
                    Text(
                        text = if (selectedTypeIsProblem) "شاركنا مشكلتك و سيتم العمل على حلها..." else "شاركنا بملاحظاتك وآرائك...",
                        style = AppTypography.bodyMedium.copy(color = Color(0xFF9E9E9E), fontSize = 13.5.sp)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AzoomaOrange,
                    unfocusedBorderColor = Color(0xFFE5E7EB)
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Dashed Attachment Box: "إدراج صورة (إختياري)"
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, Color(0xFFD1D5DB), RoundedCornerShape(14.dp)),
                color = Color(0xFFF9FAFB)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "إدراج صورة (إختياري)",
                        style = AppTypography.bodyMedium.copy(
                            color = Color(0xFF4B5563),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.5.sp
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "إدراج صورة",
                        tint = Color(0xFF6B7280),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Contact Permission Switch Card (Screenshots 19 & 20)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp)),
                color = Color(0xFFF9FAFB)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Switch(
                        checked = allowContact,
                        onCheckedChange = { allowContact = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = AzoomaOrange,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color(0xFFE5E7EB)
                        )
                    )

                    Text(
                        text = "أوافق على أن يتم التواصل معي\nلغرض تطوير الخدمة.",
                        style = AppTypography.bodySmall.copy(
                            color = AzoomaTextPrimary,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 18.sp
                        ),
                        textAlign = TextAlign.Start
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Submit Button
            Button(
                onClick = {
                    onSubmitFeedback(selectedTypeIsProblem, feedbackText, allowContact)
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(14.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AzoomaOrange
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = "إرسال",
                    style = AppTypography.titleMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Footnote Notice
            Text(
                text = "هذا الاستبيان مخصّص للمشكلات التقنية والاقتراحات العامة فقط. لماذا لا يشمل الطلبات؟",
                style = AppTypography.bodySmall.copy(
                    color = Color(0xFF6B7280),
                    fontSize = 11.5.sp,
                    lineHeight = 16.sp
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))
        }
    }
}
