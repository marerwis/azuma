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
import com.example.model.AppCategory
import com.example.model.Store
import com.example.ui.components.TopLocationBar
import com.example.ui.theme.*

private data class PromoBannerData(
    val id: String,
    val title: String,
    val subtitle: String,
    val badge: String,
    val buttonText: String,
    val imageUrl: String,
    val gradientColors: List<Color>,
    val targetStoreId: String
)

private val promoBannersList = listOf(
    PromoBannerData(
        id = "banner_1",
        title = "وجباتك المفضلة\nتصلك بسرعة فائقة 🛵",
        subtitle = "خصم 20% على أول طلب في التطبيق",
        badge = "عرض الأسبوع",
        buttonText = "اطلب الآن",
        imageUrl = "https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=800&auto=format&fit=crop&q=80",
        gradientColors = listOf(Color(0xE60E2A47), Color(0xCCFF4800)),
        targetStoreId = "shnabo"
    ),
    PromoBannerData(
        id = "banner_2",
        title = "عروض الجمعة للمشاوي 🥩\nأشهى المأكولات على الفحم",
        subtitle = "كباب وشاورما طازجة ولذيذة",
        badge = "خصم 30% 🔥",
        buttonText = "استكشف المشاوي",
        imageUrl = "https://images.unsplash.com/photo-1555939594-58d7cb561ad1?w=800&auto=format&fit=crop&q=80",
        gradientColors = listOf(Color(0xE65D1003), Color(0xCCFF5722)),
        targetStoreId = "british_doner"
    ),
    PromoBannerData(
        id = "banner_3",
        title = "توصيل مجاني للبقالة 🛍️\nوسوبرماركت متكامل",
        subtitle = "استخدم كود AZOOMA26 لطلبك",
        badge = "توصيل 0 د.ل",
        buttonText = "تسوق الآن",
        imageUrl = "https://images.unsplash.com/photo-1542838132-92c53300491e?w=800&auto=format&fit=crop&q=80",
        gradientColors = listOf(Color(0xE6003B2E), Color(0xCC00897B)),
        targetStoreId = "albaron"
    ),
    PromoBannerData(
        id = "banner_4",
        title = "أحلى قهوة وحلويات ☕🍰\nمن أرقى كافيهات بنغازي",
        subtitle = "اشرب قهوتك المفضلة واستمتع",
        badge = "عرض 1+1 مجاناً",
        buttonText = "اختر كافيهك",
        imageUrl = "https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=800&auto=format&fit=crop&q=80",
        gradientColors = listOf(Color(0xE62D1914), Color(0xCC8D6E63)),
        targetStoreId = "abu_hajar"
    )
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    currentAddressName: String,
    stores: List<Store>,
    categories: List<AppCategory>,
    onAddressClick: () -> Unit,
    onSearchClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onCategoryClick: (com.example.model.AppCategory) -> Unit,
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

        // 2. Horizontally scrollable Carousel of 4 Promotional Banners
        item {
            TopPromotionalCarousel(
                banners = promoBannersList,
                onBannerClick = { banner ->
                    val store = stores.find { it.id == banner.targetStoreId }
                        ?: stores.firstOrNull()
                    if (store != null) {
                        onStoreClick(store)
                    }
                }
            )
        }

        // 3. Exactly 16 Categories arranged in 2 horizontally scrollable rows
        item {
            SectionHeader(
                title = "الأقسام",
                onSeeAllClick = null
            )
            HorizontalCategoriesSection(
                categories = categories,
                onCategoryClick = onCategoryClick
            )
        }

        // 4. Directly jump to "الأعلى تقييماً" (Top Rated)
        item {
            SectionHeader(
                title = "الأعلى تقييماً",
                onSeeAllClick = null
            )
            TopRatedRow(stores = stores, onStoreClick = onStoreClick)
        }

        // 5. "العروض" (Offers)
        item {
            SectionHeader(
                title = "العروض 🏷️",
                onSeeAllClick = onViewAllOffersClick
            )
            OffersRow(stores = stores, onStoreClick = onStoreClick)
        }
    }
}

/**
 * Horizontally scrollable promotional carousel with 4 cards and indicator dots.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TopPromotionalCarousel(
    banners: List<PromoBannerData>,
    onBannerClick: (PromoBannerData) -> Unit
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
    banner: PromoBannerData,
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
            // Food image background from Unsplash with fallback to local drawable
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(banner.imageUrl)
                    .crossfade(true)
                    .placeholder(R.drawable.food_hero_banner)
                    .error(R.drawable.food_hero_banner)
                    .build(),
                contentDescription = banner.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Gradient Overlay for perfect readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = banner.gradientColors + listOf(Color.Transparent)
                        )
                    )
            )

            // Discount Badge at Top-End
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp),
                shape = RoundedCornerShape(12.dp),
                color = AzoomaOrange
            ) {
                Text(
                    text = banner.badge,
                    style = AppTypography.labelSmall.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    ),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            // Text and CTA Button
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.Start
            ) {
                Column {
                    Text(
                        text = banner.title,
                        style = AppTypography.titleMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            lineHeight = 22.sp
                        ),
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = banner.subtitle,
                        style = AppTypography.bodySmall.copy(
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 11.5.sp
                        ),
                        maxLines = 1
                    )
                }

                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp)),
                    color = Color.White.copy(alpha = 0.95f),
                    shape = RoundedCornerShape(20.dp),
                    shadowElevation = 2.dp
                ) {
                    Text(
                        text = banner.buttonText,
                        style = AppTypography.labelMedium.copy(
                            color = AzoomaOrange,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        ),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
            }
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
                if (category.absoluteImageUrl.isNotBlank()) {
                    AsyncImage(
                        model = category.absoluteImageUrl,
                        contentDescription = category.name,
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = category.name.take(1),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = AzoomaOrange
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
                        Image(
                            painter = painterResource(id = R.drawable.food_hero_banner),
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
private fun OffersRow(stores: List<Store>, onStoreClick: (Store) -> Unit) {
    val offerStores = stores.filter { it.hasOffer }
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
                        Image(
                            painter = painterResource(id = R.drawable.food_hero_banner),
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
