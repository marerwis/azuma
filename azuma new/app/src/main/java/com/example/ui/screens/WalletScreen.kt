package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.SouthWest
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.WalletTransaction
import com.example.ui.theme.*

@Composable
fun WalletScreen(
    balance: Double,
    transactions: List<WalletTransaction>,
    onBackClick: () -> Unit,
    onRechargeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AzoomaBackground),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // Header (Screenshot 5: "المحفظة")
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
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
                    text = "المحفظة",
                    style = AppTypography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = AzoomaTextPrimary
                    )
                )

                Spacer(modifier = Modifier.size(40.dp))
            }
        }

        // Orange Balance Card (Matching Screenshot 5)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .height(180.dp),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFFF7A00),
                                    Color(0xFFFF3D00)
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
                            Text(
                                text = "رصيد المحفظة",
                                style = AppTypography.bodyMedium.copy(
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontWeight = FontWeight.Medium
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "%.0f د.ل".format(balance),
                                style = AppTypography.displayMedium.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        // "+ شحن المحفظة" Button (Screenshot 5)
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .clip(RoundedCornerShape(23.dp))
                                .clickable { onRechargeClick() }
                                .testTag("wallet_recharge_btn"),
                            color = Color.White,
                            shape = RoundedCornerShape(23.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "+ شحن المحفظة",
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

        // Section: "المعاملات السابقة" (Matching Screenshot 5)
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "المعاملات السابقة",
                style = AppTypography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = AzoomaTextPrimary
                ),
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
            )
        }

        items(transactions) { tx ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(16.dp)),
                color = Color.White,
                shape = RoundedCornerShape(16.dp),
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Amount (-40 د.ل or +40 د.ل)
                    Text(
                        text = if (tx.isDeduction) "-%.0f د.ل".format(tx.amount) else "+%.0f د.ل".format(tx.amount),
                        style = AppTypography.titleMedium.copy(
                            color = if (tx.isDeduction) AzoomaRed else AzoomaGreen,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    // Right: Description, Reference, Date & Direction Arrow
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = tx.title,
                                style = AppTypography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = AzoomaTextPrimary
                                )
                            )
                            Text(
                                text = tx.referenceNumber,
                                style = AppTypography.bodySmall.copy(
                                    color = AzoomaTextSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                            Text(
                                text = tx.dateText,
                                style = AppTypography.bodySmall.copy(
                                    color = AzoomaTextMuted,
                                    fontSize = 11.sp
                                )
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Direction icon (Red arrow top-right for deduction, green arrow bottom-left for recharge)
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (tx.isDeduction) AzoomaRedLight else AzoomaGreenLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (tx.isDeduction) Icons.Default.NorthEast else Icons.Default.SouthWest,
                                contentDescription = null,
                                tint = if (tx.isDeduction) AzoomaRed else AzoomaGreen,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
