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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.example.model.CartState
import com.example.model.MenuItem
import com.example.ui.theme.*

@Composable
fun CartScreen(
    cartState: CartState,
    onBackClick: () -> Unit,
    onAddToCart: (MenuItem) -> Unit,
    onRemoveFromCart: (MenuItem) -> Unit,
    onDeleteItem: (MenuItem) -> Unit,
    onAddMoreItemsClick: () -> Unit,
    onStoreNoteChange: (String) -> Unit,
    onContinueClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (cartState.items.isEmpty()) {
        EmptyCartView(onStartShoppingClick = onAddMoreItemsClick, modifier = modifier)
        return
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(AzoomaBackground),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            // Header: "السلة" with Back Button on the Right
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع",
                            tint = AzoomaTextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "السلة",
                        style = AppTypography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = AzoomaTextPrimary
                        )
                    )
                }
            }

            // Store Info Card (Matching Screenshot 10: Avatar on Right, Chevron on Left)
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    color = Color.White,
                    shadowElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Right side (First child in RTL): Store Avatar + Name + Rating
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(AzoomaOrangeLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "S",
                                    style = AppTypography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = AzoomaOrange
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = cartState.storeName.ifBlank { "شنابو - طريق المطار" },
                                    style = AppTypography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = AzoomaTextPrimary
                                    )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = AzoomaYellow,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "(103633) 4.3",
                                        style = AppTypography.bodySmall.copy(color = AzoomaTextSecondary)
                                    )
                                }
                            }
                        }

                        // Left side (Last child in RTL): Trailing arrow
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = null,
                            tint = AzoomaTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Cart Items List (Matching Screenshot 10)
            items(cartState.items) { cartItem ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    color = Color.White,
                    shadowElevation = 1.dp
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Right side (First child in RTL): Photo + Name + Price
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(68.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.food_hero_banner),
                                        contentDescription = cartItem.menuItem.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = cartItem.menuItem.name,
                                        style = AppTypography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = AzoomaTextPrimary
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = "%.0f د.ل".format(cartItem.totalPrice),
                                        style = AppTypography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = AzoomaOrange
                                        )
                                    )
                                }
                            }

                            // Left side (Last child in RTL): Quantity and Delete Controls
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    modifier = Modifier.clip(RoundedCornerShape(12.dp)),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, AzoomaCardBorder),
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.White
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "+",
                                            style = AppTypography.titleSmall.copy(
                                                color = AzoomaOrange,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            modifier = Modifier
                                                .clickable { onAddToCart(cartItem.menuItem) }
                                                .padding(horizontal = 4.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "${cartItem.quantity}",
                                            style = AppTypography.titleSmall.copy(
                                                color = AzoomaTextPrimary,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "-",
                                            style = AppTypography.titleSmall.copy(
                                                color = AzoomaOrange,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            modifier = Modifier
                                                .clickable { onRemoveFromCart(cartItem.menuItem) }
                                                .padding(horizontal = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                IconButton(
                                    onClick = { onDeleteItem(cartItem.menuItem) },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "حذف",
                                        tint = AzoomaRed,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        // "+ إضافة عناصر" (Add more items)
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onAddMoreItemsClick() }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "+ إضافة عناصر",
                                style = AppTypography.titleSmall.copy(
                                    color = AzoomaOrange,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }

            // Store Note (Matching Screenshot 10: "ملاحظة للمتجر")
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    color = Color.White,
                    shadowElevation = 1.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "📝", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ملاحظة للمتجر",
                                style = AppTypography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = AzoomaTextPrimary
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = cartState.storeNote,
                            onValueChange = onStoreNoteChange,
                            placeholder = { Text("اكتب ملاحظتك هنا...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("store_note_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AzoomaOrange,
                                unfocusedBorderColor = AzoomaCardBorder,
                                focusedContainerColor = Color(0xFFFAFAFA),
                                unfocusedContainerColor = Color(0xFFFAFAFA)
                            ),
                            maxLines = 2
                        )
                    }
                }
            }

            // Recommendations: "توصيات مختارة خصيصاً لك" (Matching Screenshot 10)
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "توصيات مختارة خصيصاً لك",
                    style = AppTypography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = AzoomaTextPrimary
                    ),
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                )

                val recommendations = SampleData.menuItems.filter { it.isPopular }.take(4)
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(recommendations) { recItem ->
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
                                        contentDescription = recItem.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )

                                    Surface(
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .padding(6.dp)
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .clickable { onAddToCart(recItem) },
                                        color = Color.White,
                                        shadowElevation = 2.dp
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "+",
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
                                    text = recItem.name,
                                    style = AppTypography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    ),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "%.0f د.ل".format(recItem.price),
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
        }

        // Bottom Bar (Matching Screenshot 10: "استمرار" on Right, "9 د.ل" on Left)
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            shadowElevation = 8.dp
        ) {
            Button(
                onClick = onContinueClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("cart_continue_btn"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AzoomaOrange)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "استمرار",
                        style = AppTypography.titleMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Text(
                        text = "%.2f د.ل".format(cartState.subtotal),
                        style = AppTypography.titleMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyCartView(
    onStartShoppingClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AzoomaBackground)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(AzoomaOrangeLight),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "🛒", fontSize = 54.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "السلة فارغة حالياً",
            style = AppTypography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = AzoomaTextPrimary
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "تصفح أشهى المطاعم والمتاجر وأضف ما تحب إلى سلتك!",
            style = AppTypography.bodyMedium.copy(
                color = AzoomaTextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        )

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = onStartShoppingClick,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AzoomaOrange),
            modifier = Modifier.fillMaxWidth(0.7f)
        ) {
            Text(
                text = "ابدأ التسوق",
                style = AppTypography.titleMedium.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}
