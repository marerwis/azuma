package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*

@Composable
fun AboutScreen(
    onTermsClick: () -> Unit,
    onSocialClick: (String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            // Top Bar (Screenshot 21)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.testTag("about_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "رجوع",
                        tint = AzoomaTextPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Brand Logo & App Version Info (Screenshot 21)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 3D Presto / Azooma Swoosh Logo
                Surface(
                    modifier = Modifier.size(110.dp),
                    shape = CircleShape,
                    color = Color.White
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Surface(
                            modifier = Modifier.size(90.dp),
                            shape = CircleShape,
                            color = Color(0xFFFFECE5)
                        ) {}
                        Text(
                            text = "🛵",
                            fontSize = 52.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "عزومة - إصدار 53.26.32",
                    style = AppTypography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = AzoomaTextPrimary,
                        fontSize = 22.sp
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "3192",
                    style = AppTypography.bodySmall.copy(
                        color = AzoomaTextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "R.N: 7d7bcc4",
                    style = AppTypography.bodySmall.copy(
                        color = AzoomaTextSecondary,
                        fontSize = 11.sp
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                // App Description (Screenshot 21)
                Text(
                    text = "تطبيق متكامل للتوصيل في شمال أفريقيا، يتيح لك طلب الطعام من أشهر المطاعم والمقاهي، بالإضافة إلى الطلب من البقالة، العديد من المتاجر، كما يخصص لك بريستومان 'Presto Man' لتلبية مختلف احتياجاتك.",
                    style = AppTypography.bodyMedium.copy(
                        color = Color(0xFF4B5563),
                        lineHeight = 24.sp,
                        fontSize = 14.sp
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(36.dp))

                // Social Media Icons (X, Facebook, Instagram)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Instagram
                    SocialIconBadge(
                        symbol = "📷",
                        bgColor = Color(0xFFFDE8E8),
                        onClick = { onSocialClick("Instagram") }
                    )

                    // Facebook
                    SocialIconBadge(
                        symbol = "f",
                        isLetter = true,
                        bgColor = Color(0xFF1877F2),
                        textColor = Color.White,
                        onClick = { onSocialClick("Facebook") }
                    )

                    // X (Twitter)
                    SocialIconBadge(
                        symbol = "𝕏",
                        bgColor = Color(0xFF111827),
                        textColor = Color.White,
                        onClick = { onSocialClick("X") }
                    )
                }
            }
        }

        // Terms and Conditions Link at Bottom (Screenshot 21)
        Text(
            text = "الشروط والأحكام",
            style = AppTypography.titleMedium.copy(
                color = AzoomaTextSecondary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            ),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .clickable { onTermsClick() }
                .padding(12.dp)
        )
    }
}

@Composable
private fun SocialIconBadge(
    symbol: String,
    isLetter: Boolean = false,
    bgColor: Color,
    textColor: Color = Color.Black,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(46.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        color = bgColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = symbol,
                color = textColor,
                fontWeight = if (isLetter) FontWeight.Black else FontWeight.Normal,
                fontSize = if (isLetter) 24.sp else 20.sp
            )
        }
    }
}
