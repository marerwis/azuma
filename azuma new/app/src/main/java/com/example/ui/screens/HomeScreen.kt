package com.example.ui.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.model.AppBanner
import com.example.model.AppCategory
import com.example.model.Store
import com.example.ui.components.TopLocationBar
import com.example.ui.theme.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    currentAddressName: String,
    categories: List<AppCategory>,
    stores: List<Store>,
    banners: List<AppBanner>,
    onAddressClick: () -> Unit,
    onSearchClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onCategoryClick: (AppCategory) -> Unit,
    onStoreClick: (Store) -> Unit,
    onViewAllOffersClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AzoomaBackground),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // 1. Top Bar
        item {
            TopLocationBar(
                currentAddressName = currentAddressName,
                onAddressClick = onAddressClick,
                onSearchClick = onSearchClick,
                onNotificationClick = onNotificationClick,
                unreadNotifCount = 1
            )
        }

        // 2. Promotional Banners Carousel (only shown when live data is available)
        if (banners.isNotEmpty()) {
            item {
                TopPromotionalCarousel(
                    banners = banners,
                    onBannerClick = { banner ->
                        if (banner.storeId != null) {
                            val store = stores.find { it.id == banner.storeId }
                            if (store != null) onStoreClick(store)
                        }
                    }
                )
            }
        }

        // 3. Categories Section (only shown when live data is available)
        if (categories.isNotEmpty()) {
            item {
                SectionHeader(title = "الأقسام", onSeeAllClick = null)
                HorizontalCategoriesSection(
                    categories = categories,
                    onCategoryClick = onCategoryClick
                )
            }
        }

        // 4. Top Rated Stores
        if (stores.isNotEmpty()) {
            item {
                SectionHeader(title = "الأعلى تقييماً", onSeeAllClick = null)
                TopRatedRow(stores = stores, onStoreClick = onStoreClick)
            }
        }

        // 5. Offers
        val offerStores = stores.filter { it.hasOffer }
        if (offerStores.isNotEmpty()) {
            item {
                SectionHeader(title = "العروض 🏷️", onSeeAllClick = onViewAllOffersClick)
                OffersRow(offerStores = offerStores, onStoreClick = onStoreClick)
            }
        }
    }
}

/**
 * Horizontally scrollable promotional carousel with 4 cards and indicator dots.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TopPromotionalCarousel(
    banners: List<AppBanner>,
    onBannerClick: (AppBanner) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { banners.size })

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(165.dp)
                .testTag("hero_carousel"),
            contentPadding = PaddingValues(horizontal = 16.dp),
            pageSpacing = 12.dp
        ) { page ->
            val banner = banners[page]
            PromoBannerCard(
                banner = banner,
                onClick = { onBannerClick(banner) }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Indicator dots for the 4 promotional cards
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(banners.size) { index ->
                val isSelected = pagerState.currentPage == index
                val width by animateDpAsState(
                    targetValue = if (isSelected) 22.dp else 7.dp,
                    label = "indicator_width"
                )
                Box(
                    modifier = Modifier
                        .height(7.dp)
                        .width(width)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            if (isSelected) AzoomaOrange else Color(0xFFD6D9DE)
                        )
                )
            }
        }
    }
}

@Composable
private fun PromoBannerCard(
    banner: AppBanner,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxSize()
            .clickable { onClick() }
            .testTag("banner_${banner.id}"),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(banner.absoluteImageUrl)
                    .crossfade(true)
                    .placeholder(R.drawable.food_hero_banner)
                    .error(R.drawable.food_hero_banner)
                    .build(),
                contentDescription = "Promo Banner",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

/**
 * Exactly 16 categories arranged in a 2x4 layout (2 rows x 4 columns = 8 items visible at once),
 * with the remaining 8 items accessible via smooth horizontal scrolling.
 */
@Composable
private fun HorizontalCategoriesSection(
    categories: List<AppCategory>,
    onCategoryClick: (AppCategory) -> Unit
) {
    val allSixteen = categories.take(16)
    val row1 = allSixteen.take(8)
    val row2 = allSixteen.drop(8).take(8)

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(205.dp)
            .testTag("categories_horizontal_grid")
    ) {
        val horizontalPadding = 16.dp
        val spacing = 10.dp
        val visibleColumns = 4
        // Exactly 4 columns fit on screen at one time (2 rows x 4 cols = 8 visible items)
        val itemWidth = (maxWidth - (horizontalPadding * 2) - (spacing * (visibleColumns - 1))) / visibleColumns

        LazyRow(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = horizontalPadding),
            horizontalArrangement = Arrangement.spacedBy(spacing)
        ) {
            val columnCount = maxOf(row1.size, row2.size)
            items(columnCount) { index ->
                Column(
                    modifier = Modifier
                        .width(itemWidth)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (index < row1.size) {
                        CategoryItemCard(
                            category = row1[index],
                            itemWidth = itemWidth,
                            onClick = { onCategoryClick(row1[index]) }
                        )
                    }
                    if (index < row2.size) {
                        CategoryItemCard(
                            category = row2[index],
                            itemWidth = itemWidth,
                            onClick = { onCategoryClick(row2[index]) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryItemCard(
    category: AppCategory,
    itemWidth: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(itemWidth)
            .clickable { onClick() }
            .testTag("category_${category.id}"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Large, prominent category card with elevated white surface
        Surface(
            modifier = Modifier
                .size(68.dp)
                .shadow(3.dp, RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF0F1F5))
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                val isEmoji = category.imageUrl.length <= 10 && !category.imageUrl.contains(".")
                if (isEmoji) {
                    Text(
                        text = category.imageUrl,
                        fontSize = 32.sp
                    )
                } else {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(category.absoluteImageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = category.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = category.name,
            style = AppTypography.bodySmall.copy(
                fontWeight = FontWeight.SemiBold,
                color = AzoomaTextPrimary,
                fontSize = 12.sp
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SectionHeader(
    title: String,
    onSeeAllClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = AppTypography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = AzoomaTextPrimary,
                fontSize = 17.sp
            )
        )

        if (onSeeAllClick != null) {
            Text(
                text = "عرض المزيد",
                style = AppTypography.labelMedium.copy(
                    color = AzoomaOrange,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.clickable { onSeeAllClick() }
            )
        }
    }
}

@Composable
private fun TopRatedRow(stores: List<Store>, onStoreClick: (Store) -> Unit) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(stores.take(4)) { store ->
            Card(
                modifier = Modifier
                    .width(220.dp)
                    .clickable { onStoreClick(store) }
                    .testTag("top_rated_${store.id}"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column {
                    // Food cover with store logo badge
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(115.dp)
                            .background(Color(0xFF2B201B))
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(store.absoluteLogoUrl)
                                .crossfade(true)
                                .placeholder(R.drawable.food_hero_banner)
                                .error(R.drawable.food_hero_banner)
                                .build(),
                            contentDescription = store.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Floating store badge like in Screenshot 2
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .offset(x = 12.dp, y = 14.dp)
                                .size(44.dp)
                                .shadow(4.dp, CircleShape)
                                .clip(CircleShape),
                            color = Color.White
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = store.logoText.take(2),
                                    style = AppTypography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = AzoomaOrange
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                        Text(
                            text = store.name,
                            style = AppTypography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = AzoomaTextPrimary
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "تقييم",
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

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "${store.deliveryTime} • %.0f د.ل".format(store.deliveryFee),
                            style = AppTypography.bodySmall.copy(
                                color = AzoomaTextSecondary,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OffersRow(offerStores: List<Store>, onStoreClick: (Store) -> Unit) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(offerStores) { store ->
            Card(
                modifier = Modifier
                    .width(260.dp)
                    .clickable { onStoreClick(store) }
                    .testTag("offer_store_${store.id}"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(store.absoluteLogoUrl)
                                .crossfade(true)
                                .placeholder(R.drawable.food_hero_banner)
                                .error(R.drawable.food_hero_banner)
                                .build(),
                            contentDescription = store.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Discount Tag badge
                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF007BFF)
                        ) {
                            Text(
                                text = store.offerTitle ?: "عرض خاص",
                                style = AppTypography.labelSmall.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = store.name,
                            style = AppTypography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = AzoomaTextPrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "توصيل %.0f د.ل".format(store.deliveryFee),
                                style = AppTypography.labelMedium.copy(
                                    color = AzoomaOrange,
                                    fontWeight = FontWeight.Bold
                                )
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = AzoomaYellow,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "${store.rating}",
                                    style = AppTypography.bodySmall.copy(
                                        color = AzoomaTextSecondary,
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
