package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
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
fun PersonalInfoScreen(
    name: String,
    phone: String,
    email: String,
    onSaveProfile: (name: String, phone: String, email: String) -> Unit,
    onDeleteAccount: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentName by remember { mutableStateOf(name) }
    var currentPhone by remember { mutableStateOf(phone.removePrefix("+218-")) }
    var currentEmail by remember { mutableStateOf(email) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        // Top Bar (Screenshot 13)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.testTag("personal_info_back_btn")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "رجوع",
                    tint = AzoomaTextPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Text(
                text = "المعلومات الشخصية",
                style = AppTypography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = AzoomaTextPrimary
                )
            )

            Spacer(modifier = Modifier.size(48.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Avatar with Camera Badge (Screenshot 13)
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(110.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color(0xFFE5E7EB), CircleShape)
                    .background(Color(0xFFF3F4F6)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "الصورة الشخصية",
                    tint = Color(0xFF9CA3AF),
                    modifier = Modifier.size(60.dp)
                )
            }

            // Camera Orange Badge on bottom-start (or bottom-end)
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .size(34.dp),
                shape = CircleShape,
                color = AzoomaOrange,
                shadowElevation = 2.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "تغيير الصورة",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Fields Container (Screenshot 13)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Phone Field
            ProfileFieldRow(
                value = currentPhone,
                buttonText = "تغيير",
                isButtonOrange = true,
                onButtonClick = { /* change phone */ }
            )

            // 2. Name Field
            ProfileFieldRow(
                value = currentName,
                buttonText = "تطبيق",
                isButtonOrange = false,
                onButtonClick = { onSaveProfile(currentName, currentPhone, currentEmail) }
            )

            // 3. Password Field
            ProfileFieldRow(
                value = "••••••••••••",
                buttonText = "تغيير",
                isButtonOrange = true,
                onButtonClick = { /* change password */ }
            )

            // 4. Email Field
            ProfileFieldRow(
                value = currentEmail,
                buttonText = "تطبيق",
                isButtonOrange = false,
                onButtonClick = { onSaveProfile(currentName, currentPhone, currentEmail) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtext Notice
            Text(
                text = "سنرسل لك إشعارات حول طلباتك وعروضنا الخاصة",
                style = AppTypography.bodySmall.copy(
                    color = AzoomaTextSecondary,
                    fontSize = 12.5.sp
                ),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Delete Account Button (Screenshot 13)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDeleteDialog = true }
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "حذف حسابي",
                    style = AppTypography.titleMedium.copy(
                        color = Color(0xFFDC2626),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "حذف حسابي",
                    tint = Color(0xFFDC2626),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    text = "تأكيد حذف الحساب",
                    style = AppTypography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    text = "هل أنت متأكد من رغبتك في حذف حسابك نهائياً؟ سيتم مسح كافة بياناتك وسجل الطلبات.",
                    style = AppTypography.bodyMedium.copy(lineHeight = 22.sp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteAccount()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("نعم، حذف الحساب", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("إلغاء", color = AzoomaTextSecondary)
                }
            }
        )
    }
}

@Composable
private fun ProfileFieldRow(
    value: String,
    buttonText: String,
    isButtonOrange: Boolean,
    onButtonClick: () -> Unit
) {
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
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Action button on start/left in RTL
            Text(
                text = buttonText,
                style = AppTypography.labelLarge.copy(
                    color = if (isButtonOrange) AzoomaOrange else Color(0xFF6B7280),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                ),
                modifier = Modifier.clickable { onButtonClick() }
            )

            // Text on end/right in RTL
            Text(
                text = value,
                style = AppTypography.titleMedium.copy(
                    color = AzoomaTextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
            )
        }
    }
}
