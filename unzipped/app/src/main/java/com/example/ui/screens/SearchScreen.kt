package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.example.data.SampleData
import com.example.model.Store
import com.example.ui.theme.*

@Composable
fun SearchScreen(
    searchQuery: String,
    recentSearches: List<String>,
    onQueryChange: (String) -> Unit,
    onSearchSubmit: (String) -> Unit,
    onRemoveRecentSearch: (String) -> Unit,
    onStoreClick: (Store) -> Unit,
    modifier: Modifier = Modifier
) {
    val searchResults = if (searchQuery.isBlank()) {
        emptyList()
    } else {
        SampleData.stores.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.tags.any { tag -> tag.contains(searchQuery, ignoreCase = true) }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AzoomaBackground),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // Search Input Bar (Matching Screenshot 1)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onQueryChange,
                    placeholder = {
                        Text(
                            text = "البحث",
                            style = AppTypography.bodyMedium.copy(color = AzoomaTextSecondary)
                        )
                    },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "بحث",
                            tint = AzoomaTextSecondary
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("main_search_input"),
                    shape = RoundedCornerShape(16.dp),
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

        if (searchQuery.isBlank()) {
            // "رائج في منطقتك" (Matching Screenshot 1)
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "رائج في منطقتك",
                    style = AppTypography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = AzoomaTextPrimary
                    ),
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                )

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.5.dp, AzoomaOrange, RoundedCornerShape(16.dp))
                        .clickable {
                            val store = SampleData.stores.find { it.id == "albaron" } ?: SampleData.stores.first()
                            onStoreClick(store)
                        }
                        .testTag("trending_item_btn"),
                    color = Color.White,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = AzoomaTextPrimary
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "مطعم اللافندي - الدقادوستا",
                                style = AppTypography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = AzoomaTextPrimary
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = AzoomaOrange,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // "عمليات بحثك" (Recent Searches Matching Screenshot 1)
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "عمليات بحثك",
                    style = AppTypography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = AzoomaTextPrimary
                    ),
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    recentSearches.forEach { query ->
                        Surface(
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, AzoomaCardBorder, RoundedCornerShape(12.dp)),
                            color = Color.White,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "إزالة",
                                    tint = AzoomaTextSecondary,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable { onRemoveRecentSearch(query) }
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = query,
                                    style = AppTypography.bodyMedium.copy(
                                        color = AzoomaTextPrimary,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    modifier = Modifier.clickable {
                                        onQueryChange(query)
                                        onSearchSubmit(query)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Live Search Results
            item {
                Text(
                    text = "نتائج البحث (${searchResults.size})",
                    style = AppTypography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = AzoomaTextPrimary
                    ),
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }

            items(searchResults) { store ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onStoreClick(store) },
                    color = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    shadowElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
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
                            Text(
                                text = "${store.deliveryTime} • توصيل %.0f د.ل".format(store.deliveryFee),
                                style = AppTypography.bodySmall.copy(color = AzoomaTextSecondary)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(AzoomaOrangeLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = store.logoText.take(2),
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
    }
}
