package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun AccountScreen(
    userName: String = "",
    userPhone: String = "",
    selectedCountry: String = "ليبيا",
    onWalletClick: () -> Unit,
    onHelpClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onAddressesClick: () -> Unit,
    onCouponsClick: () -> Unit,
    onPersonalInfoClick: () -> Unit,
    onCountryClick: () -> Unit,
    onLanguageDisplayClick: () -> Unit,
    onFeedbackClick: () -> Unit,
    onTermsClick: () -> Unit,
    onAboutClick: () -> Unit,
    onRateUsClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AzoomaBackground),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // Profile Header (Matching Screenshot 8)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                // Top Bell row (On the left in RTL, which is at the end of the row)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = onNotificationClick) {
                        Icon(
                            imageVector = Icons.Default.NotificationsNone,
                            contentDescription = "الإشعارات",
                            tint = AzoomaTextPrimary
                        )
                    }
                }

                // Profile Avatar & User Details:
                // In RTL, the FIRST child is placed on the RIGHT.
                // Right: Avatar. Middle: User details. Left: QR code.
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. Right: Avatar circle
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE9ECEF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "الصورة الشخصية",
                            tint = Color(0xFFADB5BD),
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // 2. Middle: User name & phone
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = userName,
                            style = AppTypography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = AzoomaTextPrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = userPhone,
                                style = AppTypography.bodySmall.copy(
                                    color = AzoomaTextSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = AzoomaOrangeLight
                            ) {
                                Text(
                                    text = "QR",
                                    style = AppTypography.labelSmall.copy(
                                        color = AzoomaOrange,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3 Quick Action Cards: المحفظة, المساعدة, المفضلة (Matching Screenshots 3 & 4)
        // In RTL, first child appears on the right
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickCardItem(
                    title = "المحفظة",
                    icon = Icons.Default.AccountBalanceWallet,
                    iconTint = Color(0xFF2563EB),
                    bgCircleColor = Color(0xFFDBEAFE),
                    onClick = onWalletClick,
                    modifier = Modifier.weight(1f),
                    testTag = "account_wallet_btn"
                )

                QuickCardItem(
                    title = "المساعدة",
                    icon = Icons.Default.Headphones,
                    iconTint = AzoomaOrange,
                    bgCircleColor = AzoomaOrangeLight,
                    onClick = onHelpClick,
                    modifier = Modifier.weight(1f),
                    testTag = "account_help_btn"
                )

                QuickCardItem(
                    title = "المفضلة",
                    icon = Icons.Default.Favorite,
                    iconTint = AzoomaRed,
                    bgCircleColor = AzoomaRedLight,
                    onClick = onFavoritesClick,
                    modifier = Modifier.weight(1f),
                    testTag = "account_favorites_btn"
                )
            }
        }

        // Section: "الحساب و الإعدادات" (Matching Screenshots 3, 4, 8)
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "الحساب و الإعدادات",
                style = AppTypography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = AzoomaTextPrimary
                ),
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
            )

            AccountMenuListCard {
                AccountMenuItem(
                    title = "عناويني",
                    icon = Icons.Outlined.LocationOn,
                    onClick = onAddressesClick
                )
                HorizontalDivider(color = AzoomaCardBorder, modifier = Modifier.padding(horizontal = 16.dp))
                AccountMenuItem(
                    title = "الكوبونات",
                    icon = Icons.Outlined.ConfirmationNumber,
                    onClick = onCouponsClick
                )
                HorizontalDivider(color = AzoomaCardBorder, modifier = Modifier.padding(horizontal = 16.dp))
                AccountMenuItem(
                    title = "المعلومات الشخصية",
                    icon = Icons.Outlined.Person,
                    onClick = onPersonalInfoClick
                )
                HorizontalDivider(color = AzoomaCardBorder, modifier = Modifier.padding(horizontal = 16.dp))
                AccountMenuItem(
                    title = "الدولة",
                    subText = selectedCountry,
                    icon = Icons.Outlined.Public,
                    onClick = onCountryClick
                )
                HorizontalDivider(color = AzoomaCardBorder, modifier = Modifier.padding(horizontal = 16.dp))
                AccountMenuItem(
                    title = "اللغة وخيارات العرض",
                    icon = Icons.Outlined.Settings,
                    onClick = onLanguageDisplayClick
                )
            }
        }

        // Section: "حول عزومة" (Matching Screenshots 4 & 9)
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "حول عزومة",
                style = AppTypography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = AzoomaTextPrimary
                ),
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
            )

            AccountMenuListCard {
                AccountMenuItem(
                    title = "شاركنا ملاحظاتك",
                    icon = Icons.Outlined.RateReview,
                    onClick = onFeedbackClick
                )
                HorizontalDivider(color = AzoomaCardBorder, modifier = Modifier.padding(horizontal = 16.dp))
                AccountMenuItem(
                    title = "الأحكام والشروط",
                    icon = Icons.Outlined.Description,
                    onClick = onTermsClick
                )
                HorizontalDivider(color = AzoomaCardBorder, modifier = Modifier.padding(horizontal = 16.dp))
                AccountMenuItem(
                    title = "عن التطبيق",
                    icon = Icons.Outlined.Info,
                    onClick = onAboutClick
                )
                HorizontalDivider(color = AzoomaCardBorder, modifier = Modifier.padding(horizontal = 16.dp))
                AccountMenuItem(
                    title = "قيّمنا 🧡",
                    icon = Icons.Outlined.StarOutline,
                    onClick = onRateUsClick
                )
            }
        }

        // Section: "تسجيل الخروج" (Matching Screenshot 9)
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onLogoutClick() }
                    .testTag("logout_menu_item"),
                color = Color.White,
                shape = RoundedCornerShape(16.dp),
                shadowElevation = 1.dp
            ) {
                // In RTL: Icon first (Right side), Text second
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = "تسجيل الخروج",
                        tint = AzoomaRed,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "تسجيل الخروج",
                        style = AppTypography.titleMedium.copy(
                            color = AzoomaRed,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickCardItem(
    title: String,
    icon: ImageVector,
    iconTint: Color,
    bgCircleColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag(testTag),
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 1.dp
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(bgCircleColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                style = AppTypography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = AzoomaTextPrimary,
                    fontSize = 13.sp
                )
            )
        }
    }
}

@Composable
private fun AccountMenuListCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp)),
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            content = content
        )
    }
}

@Composable
private fun AccountMenuItem(
    title: String,
    subText: String? = null,
    icon: ImageVector,
    onClick: () -> Unit
) {
    // In RTL:
    // Leading icon + title are on the RIGHT (Start of row).
    // Trailing arrow/subtext is on the LEFT (End of row).
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Leading Item: Icon on the Right + Title Text
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = AzoomaTextSecondary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                style = AppTypography.titleSmall.copy(
                    color = AzoomaTextPrimary,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        // Trailing Item: Subtext or Chevron arrow on the Left
        if (subText != null) {
            Text(
                text = subText,
                style = AppTypography.bodyMedium.copy(color = AzoomaTextSecondary)
            )
        } else {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = null,
                tint = AzoomaTextSecondary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
