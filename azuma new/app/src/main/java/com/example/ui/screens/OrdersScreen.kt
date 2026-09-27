package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.Order
import com.example.model.OrderStatus
import com.example.ui.theme.*

@Composable
fun OrdersScreen(
    orders: List<Order>,
    activeTrackingOrder: Order?,
    onTrackOrder: (Order) -> Unit,
    onReorder: (Order) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf("المطاعم") }
    val tabs = listOf("المطاعم", "المتاجر")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AzoomaBackground),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // Top Bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Delivery to location on the RIGHT (Start in RTL)
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = "التوصيل إلى",
                        style = AppTypography.bodySmall.copy(color = AzoomaTextSecondary),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Start
                    )
                    Text(
                        text = "الكعب العالي",
                        style = AppTypography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = AzoomaTextPrimary
                        ),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Start
                    )
                }

                // Search & Favorite buttons on the LEFT (End in RTL)
                Row {
                    IconButton(onClick = { /* Favorites */ }) {
                        Icon(
                            imageVector = Icons.Default.FavoriteBorder,
                            contentDescription = "المفضلة",
                            tint = AzoomaTextPrimary
                        )
                    }
                    IconButton(onClick = { /* Search */ }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "بحث",
                            tint = AzoomaTextPrimary
                        )
                    }
                }
            }
        }

        // Tabs: المطاعم vs المتاجر (Matching Screenshot 4)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                tabs.forEach { tabName ->
                    val isSelected = tabName == selectedTab
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { selectedTab = tabName }
                            .padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = tabName,
                            style = AppTypography.titleMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) AzoomaOrange else AzoomaTextSecondary
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .width(48.dp)
                                    .height(3.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(AzoomaOrange)
                            )
                        }
                    }
                }
            }
        }

        // Active Order Banner if currently tracking
        if (activeTrackingOrder != null) {
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onTrackOrder(activeTrackingOrder) }
                        .testTag("active_order_tracking_banner"),
                    color = AzoomaOrangeLight,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Order status text on the RIGHT (Start in RTL)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(AzoomaOrange.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "🛵", fontSize = 20.sp)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(horizontalAlignment = Alignment.Start) {
                                Text(
                                    text = "طلبك قيد التوصيل الآن",
                                    style = AppTypography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = AzoomaOrangeDark
                                    ),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Start
                                )
                                Text(
                                    text = activeTrackingOrder.storeName,
                                    style = AppTypography.bodySmall.copy(color = AzoomaTextSecondary),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Start
                                )
                            }
                        }

                        // Track button on the LEFT (End in RTL)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = AzoomaOrange
                        ) {
                            Text(
                                text = "تتبع الآن",
                                style = AppTypography.labelMedium.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Orders List (Matching Screenshot 4)
        items(orders) { order ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .testTag("order_item_${order.orderNumber}"),
                color = Color.White,
                shape = RoundedCornerShape(16.dp),
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Top Row: Restaurant Image on RIGHT, Order details in middle, Status on LEFT
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Group 1 (RIGHT / Start): Restaurant Image + Name + Date/Price
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            // Restaurant Image on the RIGHT
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.food_hero_banner),
                                    contentDescription = order.storeName,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Order details to the left of the image
                            Column(horizontalAlignment = Alignment.Start) {
                                Text(
                                    text = order.storeName,
                                    style = AppTypography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = AzoomaTextPrimary
                                    ),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Start
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${order.dateText} • %.0f د.ل • #${order.orderNumber}".format(order.totalPrice),
                                    style = AppTypography.bodySmall.copy(
                                        color = AzoomaTextSecondary,
                                        fontSize = 11.sp
                                    ),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Start
                                )
                            }
                        }

                        // Group 2 (LEFT / End): Status Badge
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (order.status == OrderStatus.DELIVERED) AzoomaGreenLight else AzoomaOrangeLight
                        ) {
                            Text(
                                text = order.status.titleAr,
                                style = AppTypography.labelSmall.copy(
                                    color = if (order.status == OrderStatus.DELIVERED) AzoomaGreen else AzoomaOrange,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Ordered Items Thumbnails (Start = Right to End = Left)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        order.itemsSummary.forEach { (itemName, qty) ->
                            Box(
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.food_hero_banner),
                                    contentDescription = itemName,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )

                                Surface(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(4.dp),
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color.White.copy(alpha = 0.92f)
                                ) {
                                    Text(
                                        text = "x$qty",
                                        style = AppTypography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = AzoomaTextPrimary,
                                            fontSize = 10.sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Reorder Button (Matching Screenshot 4: "إعادة الطلب")
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onReorder(order) },
                        color = Color(0xFFF1F3F5),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(vertical = 11.dp)
                        ) {
                            Text(
                                text = "إعادة الطلب",
                                style = AppTypography.titleSmall.copy(
                                    color = AzoomaTextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
