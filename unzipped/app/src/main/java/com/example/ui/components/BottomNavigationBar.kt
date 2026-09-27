package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.BottomTab

@Composable
fun AzoomaBottomNav(
    currentTab: BottomTab,
    cartItemCount: Int,
    onTabSelected: (BottomTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
        color = Color.White,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp)
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavItem(
                title = "الرئيسية",
                iconFilled = Icons.Default.Home,
                iconOutlined = Icons.Outlined.Home,
                isSelected = currentTab == BottomTab.HOME,
                onClick = { onTabSelected(BottomTab.HOME) },
                testTag = "tab_home"
            )

            NavItem(
                title = "البحث",
                iconFilled = Icons.Default.Search,
                iconOutlined = Icons.Outlined.Search,
                isSelected = currentTab == BottomTab.SEARCH,
                onClick = { onTabSelected(BottomTab.SEARCH) },
                testTag = "tab_search"
            )

            NavItem(
                title = "السلة",
                iconFilled = Icons.Default.ShoppingCart,
                iconOutlined = Icons.Outlined.ShoppingCart,
                isSelected = currentTab == BottomTab.CART,
                badgeCount = cartItemCount,
                onClick = { onTabSelected(BottomTab.CART) },
                testTag = "tab_cart"
            )

            NavItem(
                title = "الطلبات",
                iconFilled = Icons.Default.ReceiptLong,
                iconOutlined = Icons.Outlined.ReceiptLong,
                isSelected = currentTab == BottomTab.ORDERS,
                onClick = { onTabSelected(BottomTab.ORDERS) },
                testTag = "tab_orders"
            )

            NavItem(
                title = "حسابي",
                iconFilled = Icons.Default.Person,
                iconOutlined = Icons.Outlined.Person,
                isSelected = currentTab == BottomTab.ACCOUNT,
                onClick = { onTabSelected(BottomTab.ACCOUNT) },
                testTag = "tab_account"
            )
        }
    }
}

@Composable
private fun NavItem(
    title: String,
    iconFilled: ImageVector,
    iconOutlined: ImageVector,
    isSelected: Boolean,
    badgeCount: Int = 0,
    onClick: () -> Unit,
    testTag: String
) {
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = Modifier
            .testTag(testTag)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 4.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(if (isSelected) AzoomaOrangeLight else Color.Transparent)
                .padding(horizontal = 14.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isSelected) iconFilled else iconOutlined,
                contentDescription = title,
                tint = if (isSelected) AzoomaOrange else AzoomaTextSecondary,
                modifier = Modifier.size(24.dp)
            )

            if (badgeCount > 0) {
                Box(
                    modifier = Modifier
                        .offset(x = 10.dp, y = (-10).dp)
                        .clip(CircleShape)
                        .background(AzoomaOrange)
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "$badgeCount",
                        style = AppTypography.labelSmall.copy(
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = title,
            style = AppTypography.labelSmall.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) AzoomaOrange else AzoomaTextSecondary,
                fontSize = 12.sp
            )
        )
    }
}
