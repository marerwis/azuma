package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*

@Composable
fun WelcomeScreen(
    onRegisterClick: () -> Unit,
    onLoginClick: () -> Unit,
    onContinueAsGuestClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeSlideIndex by remember { mutableIntStateOf(0) }
    val slides = listOf(
        "مجموعة متنوعة من المطاعم\nللطلب منها",
        "توصيل سريع وفوري\nحتى باب منزلك",
        "عروض حصرية وخصومات\nيومية مميزة",
        "تتبع طلبك لحظة بلحظة\nعلى الخريطة"
    )

    // Auto-advance carousel smoothly
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(4000)
            activeSlideIndex = (activeSlideIndex + 1) % slides.size
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // 1. Fullscreen Pizza Food Background (Matching user screenshot)
        Image(
            painter = painterResource(id = R.drawable.welcome_pizza_bg),
            contentDescription = "عزومة بيتزا",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Dark gradients to enhance top status and bottom text contrast
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x80000000),
                            Color.Transparent,
                            Color(0x40000000),
                            Color(0xCC000000)
                        )
                    )
                )
        )

        // 2. Top Bar: Floating Pills for "اللغة" and "ليبيا" (Matching user screenshot)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pill 1: Language ("اللغة v")
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .clickable { /* Language Selector */ }
                    .testTag("welcome_lang_btn"),
                shape = RoundedCornerShape(24.dp),
                color = Color.White.copy(alpha = 0.95f),
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Translate,
                        contentDescription = "اللغة",
                        tint = AzoomaTextPrimary,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "اللغة",
                        style = AppTypography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = AzoomaTextPrimary,
                            fontSize = 13.sp
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = AzoomaOrange,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }

            // Pill 2: Country ("ليبيا v" with Libyan Flag)
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .clickable { /* Country Selector */ }
                    .testTag("welcome_country_btn"),
                shape = RoundedCornerShape(24.dp),
                color = Color.White.copy(alpha = 0.95f),
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Libyan Flag emoji
                    Text(text = "🇱🇾", fontSize = 16.sp)

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "ليبيا",
                        style = AppTypography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = AzoomaTextPrimary,
                            fontSize = 13.sp
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = AzoomaOrange,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }

        // 3. Bottom Content Wrapper (Text, Dots, and Bottom Sheet)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            // Promotional Text & Indicator Dots
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = slides[activeSlideIndex],
                    style = AppTypography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 25.sp,
                        lineHeight = 34.sp
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Carousel Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in slides.indices) {
                        if (i == activeSlideIndex) {
                            Box(
                                modifier = Modifier
                                    .width(24.dp)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color.White)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.45f))
                            )
                        }
                    }
                }
            }

            // 4. Bottom Sheet White Container with 3 Stacked Buttons
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = Color.White,
                shadowElevation = 16.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 20.dp)
                        .navigationBarsPadding(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Button 1: "تسجيل حساب جديد"
                    Button(
                        onClick = onRegisterClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("welcome_register_btn"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AzoomaOrange)
                    ) {
                        Text(
                            text = "تسجيل حساب جديد",
                            style = AppTypography.titleMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        )
                    }

                Spacer(modifier = Modifier.height(12.dp))

                // Button 2: "لدي حساب من قبل" (Light Gray Button with dark text)
                Button(
                    onClick = onLoginClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("welcome_login_btn"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFEDF1F7),
                        contentColor = AzoomaTextPrimary
                    )
                ) {
                    Text(
                        text = "لدي حساب من قبل",
                        style = AppTypography.titleMedium.copy(
                            color = AzoomaTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Button 3: "الاستمرار كزائر" (Text-only Orange Button)
                Text(
                    text = "الاستمرار كزائر",
                    style = AppTypography.titleMedium.copy(
                        color = AzoomaOrange,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onContinueAsGuestClick() }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("welcome_guest_btn")
                )
            }
        }
    }
}
}
