package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.CartState
import com.example.model.MenuCategoryWithProducts
import com.example.model.MenuItem
import com.example.model.Store
import com.example.ui.theme.*

@Composable
fun StoreProfileScreen(
    store: Store,
    menuCategories: List<MenuCategoryWithProducts>,
    cartState: CartState,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onBackClick: () -> Unit,
    onAddToCart: (MenuItem) -> Unit,
    onRemoveFromCart: (MenuItem) -> Unit,
    onViewCartClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isDeliveryMode by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }

    // Derive category tabs from live data; fall back to first tab
    val storeCategories = menuCategories.map { it.name }
    var selectedCategoryTab by remember(menuCategories) {
        mutableStateOf(menuCategories.firstOrNull()?.name ?: "")
    }

    // Flatten items from the selected category
    val allItems = menuCategories.flatMap { it.products }
    val filteredItems = allItems.filter { item ->
        val matchesCategory = selectedCategoryTab.isEmpty() ||
            menuCategories.find { it.name == selectedCategoryTab }?.products?.any { it.id == item.id } == true
        val matchesSearch = searchQuery.isBlank() ||
            item.name.contains(searchQuery, ignoreCase = true) ||
            item.description.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(AzoomaBackground),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // 1. Hero Cover Image (Matching Screenshot 16)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.food_hero_banner),
                        contentDescription = "غلاف المطعم",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Top Bar action buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .clickable { onBackClick() },
                            color = Color.White.copy(alpha = 0.85f),
                            shape = CircleShape
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "رجوع",
                                    tint = AzoomaTextPrimary
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .clickable { onToggleFavorite() },
                                color = Color.White.copy(alpha = 0.85f),
                                shape = CircleShape
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = "المفضلة",
                                        tint = if (isFavorite) AzoomaRed else AzoomaTextPrimary
                                    )
                                }
                            }

                            Surface(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .clickable { /* Share */ },
                                color = Color.White.copy(alpha = 0.85f),
                                shape = CircleShape
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "مشاركة",
                                        tint = AzoomaTextPrimary
                                    )
                                }
                            }
                        }
                    }

                    // Floating Store Logo Badge (Screenshot 16)
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = (-20).dp, y = 30.dp)
                            .size(72.dp)
                            .shadow(6.dp, CircleShape)
                            .clip(CircleShape),
                        color = Color.White
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = store.logoText,
                                style = AppTypography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = AzoomaOrange
                                )
                            )
                        }
                    }
                }
            }

            // 2. Info Section (Matching Screenshot 16)
            item {
                Spacer(modifier = Modifier.height(36.dp))

                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AzoomaGreenLight
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "مفتوح",
                                    style = AppTypography.labelSmall.copy(
                                        color = AzoomaGreen,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = AzoomaGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Text(
                            text = store.name,
                            style = AppTypography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = AzoomaTextPrimary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Delivery / Pickup Toggle (Screenshot 16)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp)),
                        color = Color(0xFFF1F3F5)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            // Delivery Toggle Option
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { isDeliveryMode = true },
                                color = if (isDeliveryMode) Color.White else Color.Transparent,
                                shadowElevation = if (isDeliveryMode) 2.dp else 0.dp
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "توصيل",
                                        style = AppTypography.titleSmall.copy(
                                            color = if (isDeliveryMode) AzoomaOrange else AzoomaTextSecondary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.TwoWheeler,
                                        contentDescription = null,
                                        tint = if (isDeliveryMode) AzoomaOrange else AzoomaTextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            // Pickup Toggle Option
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { isDeliveryMode = false },
                                color = if (!isDeliveryMode) Color.White else Color.Transparent,
                                shadowElevation = if (!isDeliveryMode) 2.dp else 0.dp
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "استلام",
                                        style = AppTypography.titleSmall.copy(
                                            color = if (!isDeliveryMode) AzoomaOrange else AzoomaTextSecondary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.ShoppingBag,
                                        contentDescription = null,
                                        tint = if (!isDeliveryMode) AzoomaOrange else AzoomaTextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 3 Stats Columns (التحضير 20 دقيقة, المسافة 5 كم, التقييم 4.3)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "التحضير",
                                style = AppTypography.bodySmall.copy(color = AzoomaTextSecondary)
                            )
                            Text(
                                text = "${store.prepTimeMin} دقيقة",
                                style = AppTypography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = AzoomaTextPrimary
                                )
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "المسافة",
                                style = AppTypography.bodySmall.copy(color = AzoomaTextSecondary)
                            )
                            Text(
                                text = store.distanceKm,
                                style = AppTypography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = AzoomaTextPrimary
                                )
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "التقييم",
                                style = AppTypography.bodySmall.copy(color = AzoomaTextSecondary)
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = AzoomaYellow,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "(${store.ratingCount}) ${store.rating}",
                                    style = AppTypography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = AzoomaTextPrimary
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Mini Directions Card (Matching Screenshot 16)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp)),
                        color = Color.White,
                        shadowElevation = 1.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFE9F2FF)
                            ) {
                                Text(
                                    text = "الاتجاهات",
                                    style = AppTypography.labelMedium.copy(
                                        color = Color(0xFF0066FF),
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = store.address,
                                    style = AppTypography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = AzoomaTextPrimary
                                    )
                                )
                                Text(
                                    text = store.distanceKm,
                                    style = AppTypography.bodySmall.copy(color = AzoomaTextSecondary)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Search inside store
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("بحث") },
                        trailingIcon = {
                            Icon(imageVector = Icons.Default.Search, contentDescription = "بحث")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("store_search_input"),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AzoomaOrange,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        singleLine = true
                    )
                }
            }

            // 3. "الأكثر طلباً" Horizontal List (Matching Screenshot 16)
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "الأكثر طلباً",
                    style = AppTypography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = AzoomaTextPrimary
                    ),
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )

                val popularItems = allItems.filter { it.isPopular }
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(popularItems) { item ->
                        val inCartCount = cartState.items.find { it.menuItem.id == item.id }?.quantity ?: 0
                        Surface(
                            modifier = Modifier
                                .width(135.dp)
                                .clip(RoundedCornerShape(16.dp)),
                            color = Color.White,
                            shadowElevation = 2.dp
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(85.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.food_hero_banner),
                                        contentDescription = item.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )

                                    // Quick Add Button (+)
                                    Surface(
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .padding(6.dp)
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .clickable { onAddToCart(item) }
                                            .testTag("quick_add_${item.id}"),
                                        color = Color.White,
                                        shadowElevation = 2.dp
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = if (inCartCount > 0) "$inCartCount" else "+",
                                                style = AppTypography.labelMedium.copy(
                                                    color = AzoomaOrange,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = item.name,
                                    style = AppTypography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    ),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "%.0f د.ل".format(item.price),
                                    style = AppTypography.labelMedium.copy(
                                        color = AzoomaOrange,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // 4. Sticky Category Tabs (Screenshots 14, 15, 16)
            item {
                Spacer(modifier = Modifier.height(20.dp))
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(vertical = 10.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    items(storeCategories) { catTab ->
                        val isSelected = catTab == selectedCategoryTab
                        Column(
                            modifier = Modifier
                                .clickable { selectedCategoryTab = catTab }
                                .testTag("menu_tab_$catTab"),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = catTab,
                                style = AppTypography.titleMedium.copy(
                                    color = if (isSelected) AzoomaOrange else AzoomaTextSecondary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .width(36.dp)
                                        .height(3.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(AzoomaOrange)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = selectedCategoryTab,
                    style = AppTypography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = AzoomaTextPrimary
                    ),
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                )
            }

            // 5. Menu Items Vertical List (Matching Screenshot 14 & 15)
            items(filteredItems) { menuItem ->
                val inCart = cartState.items.find { it.menuItem.id == menuItem.id }
                val quantity = inCart?.quantity ?: 0

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    color = Color.White,
                    shadowElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Left: Item Image with add button
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .clip(RoundedCornerShape(14.dp))
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.food_hero_banner),
                                contentDescription = menuItem.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

                            // Add / Quantity Button (+ or - Qty +)
                            if (quantity == 0) {
                                Surface(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(4.dp)
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .clickable { onAddToCart(menuItem) }
                                        .testTag("add_item_${menuItem.id}"),
                                    color = Color.White,
                                    shadowElevation = 3.dp
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "+",
                                            style = AppTypography.titleMedium.copy(
                                                color = AzoomaOrange,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }
                            } else {
                                Surface(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(4.dp)
                                        .clip(RoundedCornerShape(16.dp)),
                                    color = Color.White,
                                    shadowElevation = 4.dp
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "+",
                                            style = AppTypography.titleSmall.copy(
                                                color = AzoomaOrange,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            modifier = Modifier.clickable { onAddToCart(menuItem) }
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "$quantity",
                                            style = AppTypography.labelMedium.copy(
                                                color = AzoomaTextPrimary,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "-",
                                            style = AppTypography.titleSmall.copy(
                                                color = AzoomaOrange,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            modifier = Modifier.clickable { onRemoveFromCart(menuItem) }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        // Right: Title, description, price
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = menuItem.name,
                                style = AppTypography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = AzoomaTextPrimary
                                )
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = menuItem.description,
                                style = AppTypography.bodySmall.copy(
                                    color = AzoomaTextSecondary,
                                    lineHeight = 16.sp
                                ),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "%.0f د.ل".format(menuItem.price),
                                style = AppTypography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = AzoomaOrange
                                )
                            )
                        }
                    }
                }
            }
        }

        // 6. Floating Cart Pill at Bottom (Matching Screenshot 14)
        if (cartState.totalItemCount > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 20.dp, start = 20.dp, end = 20.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .clickable { onViewCartClick() }
                        .testTag("floating_view_cart_btn"),
                    color = AzoomaOrange,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "%.0f د.ل".format(cartState.subtotal),
                            style = AppTypography.titleMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )

                        Text(
                            text = "عرض السلة",
                            style = AppTypography.titleMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )

                        // Count badge in circle
                        Surface(
                            modifier = Modifier.size(28.dp),
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.25f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "${cartState.totalItemCount}",
                                    style = AppTypography.labelMedium.copy(
                                        color = Color.White,
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
}
