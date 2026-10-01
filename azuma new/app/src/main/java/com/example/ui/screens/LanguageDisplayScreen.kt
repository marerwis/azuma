package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Translate
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
fun LanguageDisplayScreen(
    currentLanguage: String,
    currentThemeMode: String,
    onSelectLanguage: (String) -> Unit,
    onSelectThemeMode: (String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showLanguageSheet by remember { mutableStateOf(false) }
    var showThemeSheet by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        // Top Bar (Screenshot 15)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.testTag("lang_display_back_btn")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "رجوع",
                    tint = AzoomaTextPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Text(
                text = "اللغة وخيارات العرض",
                style = AppTypography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = AzoomaTextPrimary
                )
            )

            Spacer(modifier = Modifier.size(48.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Settings Cards (Screenshot 15)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Language Card
            SettingOptionCard(
                title = "اللغة",
                value = currentLanguage,
                icon = {
                    Icon(
                        imageVector = Icons.Default.Translate,
                        contentDescription = "اللغة",
                        tint = Color(0xFF4B5563),
                        modifier = Modifier.size(22.dp)
                    )
                },
                onClick = { showLanguageSheet = true }
            )

            // 2. Display Theme Card
            SettingOptionCard(
                title = "خيارات العرض",
                value = currentThemeMode,
                icon = {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = "خيارات العرض",
                        tint = Color(0xFF4B5563),
                        modifier = Modifier.size(22.dp)
                    )
                },
                onClick = { showThemeSheet = true }
            )
        }
    }

    // Language Selection Bottom Sheet (Screenshot 16)
    if (showLanguageSheet) {
        ModalBottomSheet(
            onDismissRequest = { showLanguageSheet = false },
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
                    text = "لغة التطبيق",
                    style = AppTypography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = AzoomaTextPrimary,
                        fontSize = 18.sp
                    ),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(18.dp))

                val languages = listOf("العربية" to "ع", "الإنجليزية" to "A", "الفرنسية" to "FR")

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    languages.forEach { (lang, iconText) ->
                        val isSelected = currentLanguage == lang

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp))
                                .clickable {
                                    onSelectLanguage(lang)
                                    showLanguageSheet = false
                                },
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Check indicator on left
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) AzoomaOrange else Color.Transparent)
                                        .border(
                                            width = if (isSelected) 0.dp else 1.5.dp,
                                            color = if (isSelected) Color.Transparent else Color(0xFFD1D5DB),
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                // Language Title & Icon on right
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = lang,
                                        style = AppTypography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = AzoomaTextPrimary
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFFF3F4F6)
                                    ) {
                                        Text(
                                            text = iconText,
                                            style = AppTypography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Display Theme Bottom Sheet (Screenshot 17)
    if (showThemeSheet) {
        ModalBottomSheet(
            onDismissRequest = { showThemeSheet = false },
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
                    text = "خيارات العرض",
                    style = AppTypography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = AzoomaTextPrimary,
                        fontSize = 18.sp
                    ),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(18.dp))

                val modes = listOf(
                    "وضع النظام" to Icons.Default.Palette,
                    "الوضع الفاتح" to Icons.Default.LightMode,
                    "الوضع الداكن" to Icons.Default.DarkMode
                )

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    modes.forEach { (mode, icon) ->
                        val isSelected = currentThemeMode == mode

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp))
                                .clickable {
                                    onSelectThemeMode(mode)
                                    showThemeSheet = false
                                },
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Check indicator on left
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) AzoomaOrange else Color.Transparent)
                                        .border(
                                            width = if (isSelected) 0.dp else 1.5.dp,
                                            color = if (isSelected) Color.Transparent else Color(0xFFD1D5DB),
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                // Theme Title & Icon on right
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = mode,
                                        style = AppTypography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = AzoomaTextPrimary
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = Color(0xFF4B5563),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun SettingOptionCard(
    title: String,
    value: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Value text on start / left
            Text(
                text = value,
                style = AppTypography.bodyMedium.copy(
                    color = AzoomaTextSecondary,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
            )

            // Title & Icon on end / right
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    style = AppTypography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = AzoomaTextPrimary,
                        fontSize = 15.sp
                    )
                )
                Spacer(modifier = Modifier.width(12.dp))
                icon()
            }
        }
    }
}
