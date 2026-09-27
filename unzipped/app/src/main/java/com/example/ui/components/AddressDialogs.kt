package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAddressSheet(
    onDismiss: () -> Unit,
    onSave: (name: String, details: String) -> Unit
) {
    var addressTitle by remember { mutableStateOf("الكعب العالي") }
    var addressDetails by remember { mutableStateOf("") }
    var showMapPicker by remember { mutableStateOf(false) }

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
                text = "بيانات إضافية للموقع",
                style = AppTypography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = AzoomaTextPrimary
                ),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "موقعك يحدد الخدمات المتاحة بالقرب منك",
                style = AppTypography.bodySmall.copy(
                    color = AzoomaTextSecondary,
                    fontSize = 12.sp
                ),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Map preview box
            if (showMapPicker) {
                InteractiveMapCanvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    destinationLabel = addressTitle.ifBlank { "موقعي" }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Location Plus Code row with "تغيير"
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, AzoomaCardBorder, RoundedCornerShape(12.dp)),
                color = Color.White
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { showMapPicker = !showMapPicker }
                            .padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "تغيير الموقع",
                            tint = AzoomaOrange,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (showMapPicker) "إخفاء الخريطة" else "تغيير",
                            style = AppTypography.labelMedium.copy(
                                color = AzoomaOrange,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "موقعك",
                            style = AppTypography.bodySmall.copy(
                                color = AzoomaTextSecondary,
                                fontSize = 11.sp
                            )
                        )
                        Text(
                            text = "436G+585، بنغازي",
                            style = AppTypography.titleSmall.copy(
                                color = AzoomaTextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Address title input
            OutlinedTextField(
                value = addressTitle,
                onValueChange = { addressTitle = it },
                label = { Text("حدد اسم العنوان (مثلاً: المنزل، العمل)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("address_title_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AzoomaOrange,
                    unfocusedBorderColor = AzoomaCardBorder
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Address description input
            OutlinedTextField(
                value = addressDetails,
                onValueChange = { addressDetails = it },
                label = { Text("وصف العنوان (اختياري، مثلاً: بجانب مختبر الحياة)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("address_details_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AzoomaOrange,
                    unfocusedBorderColor = AzoomaCardBorder
                ),
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { onSave(addressTitle, addressDetails) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_address_btn"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AzoomaOrange)
            ) {
                Text(
                    text = "حفظ وتحديث العنوان",
                    style = AppTypography.titleMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
