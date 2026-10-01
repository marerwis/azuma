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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.Store
import com.example.ui.theme.*

@Composable
fun FavoritesScreen(
    favoriteStoreIds: Set<String>,
    stores: List<Store>,
    onBackClick: () -> Unit,
    onStoreClick: (Store) -> Unit,
    onToggleFavorite: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("المطاعم", "المتاجر")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        // Top Bar (Matching Screenshot 5 & 6)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.testTag("favorites_back_btn")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "رجوع",
                    tint = AzoomaTextPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Text(
                text = "المفضلة",
                style = AppTypography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = AzoomaTextPrimary
                )
            )

            // Balance spacer
            Spacer(modifier = Modifier.size(48.dp))
        }

        // Two Tabs: المطاعم | المتاجر
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = Color.White,
            contentColor = AzoomaOrange,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = AzoomaOrange,
                    height = 3.dp
                )
            },
            divider = { HorizontalDivider(color = Color(0xFFF0F0F0)) }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            style = AppTypography.titleMedium.copy(
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedTabIndex == index) AzoomaOrange else AzoomaTextSecondary,
                                fontSize = 16.sp
                            )
                        )
                    }
                )
            }
        }

        if (selectedTabIndex == 0) {
            // Restaurants Tab (Screenshot 5)
            val favoriteStores = stores.filter { favoriteStoreIds.contains(it.id) }

            if (favoriteStores.isEmpty()) {
                EmptyFavoritesState(
                    title = "قائمة المفضلة فارغة",
                    subtitle = "يمكنك حفظ المطاعم المفضلة للرجوع إليها بسهولة لاحقًا."
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(favoriteStores) { store ->
                        FavoriteStoreCard(
                            store = store,
                            onClick = { onStoreClick(store) },
                            onToggleFavorite = { onToggleFavorite(store.id) }
                        )
                    }
                }
            }
        } else {
            // Stores Tab (Screenshot 6 - Empty state)
            EmptyFavoritesState(
                title = "قائمة المفضلة فارغة",
                subtitle = "يمكنك حفظ المتاجر والمطاعم في قائمة المفضلة للرجوع إليها بسهولة لاحقًا."
            )
        }
    }
}

@Composable
private fun FavoriteStoreCard(
    store: Store,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .testTag("fav_store_${store.id}"),
        color = Color.White,
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F2F4))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Favorite Red Heart Icon on start/right
            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "إزالة من المفضلة",
                    tint = Color(0xFFFF3B30),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Store Info in Middle
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = store.name,
                    style = AppTypography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = AzoomaTextPrimary,
                        fontSize = 16.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "(${store.ratingCount})",
                        style = AppTypography.bodySmall.copy(color = AzoomaTextSecondary, fontSize = 11.sp)
                    )
                    Text(
                        text = "${store.rating}",
                        style = AppTypography.bodySmall.copy(fontWeight = FontWeight.Bold, color = AzoomaTextPrimary)
                    )
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = AzoomaYellow,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "•",
                        style = AppTypography.bodySmall.copy(color = AzoomaTextSecondary)
                    )
                    Text(
                        text = "%.2f د.ل".format(store.deliveryFee),
                        style = AppTypography.bodySmall.copy(fontWeight = FontWeight.Bold, color = AzoomaTextPrimary, fontSize = 12.sp)
                    )
                }

                if (store.hasOffer) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFE8F1FF)
                    ) {
                        Text(
                            text = store.offerTitle ?: "توصيل 4 دينار",
                            style = AppTypography.labelSmall.copy(
                                color = Color(0xFF1E60D5),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Store Photo Circle on End / Left
            Surface(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape),
                color = Color(0xFFF5F5F5)
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

@Composable
private fun EmptyFavoritesState(
    title: String,
    subtitle: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 3D Orange Heart Illustration (Screenshot 6)
        Box(
            modifier = Modifier
                .size(120.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier.size(90.dp),
                shape = CircleShape,
                color = Color(0xFFFFF3ED)
            ) {}
            Text(
                text = "🧡",
                fontSize = 64.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = title,
            style = AppTypography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = AzoomaTextPrimary
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = subtitle,
            style = AppTypography.bodyMedium.copy(
                color = AzoomaTextSecondary,
                lineHeight = 22.sp
            ),
            textAlign = TextAlign.Center
        )
    }
}
