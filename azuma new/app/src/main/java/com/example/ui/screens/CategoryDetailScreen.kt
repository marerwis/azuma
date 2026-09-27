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
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
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
import com.example.data.SampleData
import com.example.model.Store
import com.example.model.StoreCategory
import com.example.ui.theme.*

@Composable
fun CategoryDetailScreen(
    category: StoreCategory,
    onBackClick: () -> Unit,
    onStoreClick: (Store) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("الكل") }
    val subcategories = listOf(
        "ياغورت مثلج" to "🍦",
        "مخبوزات" to "🥖",
        "معجنات" to "🥐",
        "أكل شعبي" to "🍲",
        "مشويات" to "🍢",
        "بيتزا" to "🍕"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AzoomaBackground),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // Top Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "رجوع",
                        tint = AzoomaTextPrimary
                    )
                }

                Text(
                    text = category.name,
                    style = AppTypography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = AzoomaTextPrimary
                    )
                )

                Row {
                    IconButton(onClick = { /* Search */ }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "بحث",
                            tint = AzoomaTextPrimary
                        )
                    }
                    IconButton(onClick = { /* Favorite */ }) {
                        Icon(
                            imageVector = Icons.Default.FavoriteBorder,
                            contentDescription = "المفضلة",
                            tint = AzoomaTextPrimary
                        )
                    }
                }
            }
        }

        // Subcategories Chips (Matching Screenshot 18)
        item {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(subcategories) { (name, emoji) ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { selectedFilter = name }
                    ) {
                        Surface(
                            modifier = Modifier
                                .size(56.dp)
                                .shadow(2.dp, CircleShape),
                            shape = CircleShape,
                            color = Color.White
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = emoji, fontSize = 26.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = name,
                            style = AppTypography.bodySmall.copy(
                                color = if (selectedFilter == name) AzoomaOrange else AzoomaTextSecondary,
                                fontWeight = if (selectedFilter == name) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }
        }

        // Filter Buttons Row (Matching Screenshot 18: الترتيب v, عروض, استلام, الفئات v)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChipItem(title = "الترتيب", hasDropdown = true)
                FilterChipItem(title = "عروض", hasDropdown = false)
                FilterChipItem(title = "استلام", hasDropdown = false)
                FilterChipItem(title = "الفئات", hasDropdown = true)
            }
        }

        // Section: "مطاعم مميزة ⭐" (Matching Screenshot 18)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "مطاعم مميزة ⭐",
                    style = AppTypography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = AzoomaTextPrimary
                    )
                )
                Text(
                    text = "عرض المزيد",
                    style = AppTypography.labelMedium.copy(
                        color = AzoomaOrange,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            val featured = SampleData.stores.filter { it.isFeatured }
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(featured) { store ->
                    Surface(
                        modifier = Modifier
                            .width(150.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onStoreClick(store) }
                            .testTag("featured_${store.id}"),
                        color = Color.White,
                        shape = RoundedCornerShape(16.dp),
                        shadowElevation = 2.dp
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(AzoomaOrangeLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = store.logoText.take(2),
                                    style = AppTypography.titleMedium.copy(
                                        color = AzoomaOrange,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = store.name,
                                style = AppTypography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = AzoomaYellow,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "${store.rating} (${store.ratingCount})",
                                    style = AppTypography.bodySmall.copy(
                                        fontSize = 10.sp,
                                        color = AzoomaTextSecondary
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section: "جميع المطاعم" (All Restaurants list matching Screenshot 19)
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "جميع المطاعم",
                    style = AppTypography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = AzoomaTextPrimary
                    )
                )
            }
        }

        items(SampleData.stores) { store ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onStoreClick(store) }
                    .testTag("all_store_${store.id}"),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = store.name,
                            style = AppTypography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = AzoomaTextPrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "%.2f د.ل".format(store.deliveryFee),
                                style = AppTypography.bodySmall.copy(
                                    color = AzoomaTextSecondary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(text = "•", color = AzoomaTextSecondary)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = AzoomaYellow,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "${store.rating} (${store.ratingCount})",
                                    style = AppTypography.bodySmall.copy(
                                        color = AzoomaTextSecondary,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        if (store.pickupAvailable) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = AzoomaGreenLight
                            ) {
                                Text(
                                    text = "الاستلام متاح",
                                    style = AppTypography.labelSmall.copy(
                                        color = AzoomaGreen,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Store thumbnail
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFF3E7DC)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.food_hero_banner),
                            contentDescription = store.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterChipItem(title: String, hasDropdown: Boolean) {
    Surface(
        modifier = Modifier.clip(RoundedCornerShape(20.dp)),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, AzoomaCardBorder),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = AppTypography.labelMedium.copy(
                    color = AzoomaTextSecondary,
                    fontWeight = FontWeight.Medium
                )
            )
            if (hasDropdown) {
                Spacer(modifier = Modifier.width(2.dp))
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = AzoomaTextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
