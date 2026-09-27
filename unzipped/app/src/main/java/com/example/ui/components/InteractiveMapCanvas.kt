package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun InteractiveMapCanvas(
    modifier: Modifier = Modifier,
    driverProgress: Float = 0.6f,
    showDriver: Boolean = false,
    destinationLabel: String = "الكعب العالي",
    onPinMoved: ((Float, Float) -> Unit)? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFE9EDF0))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Background blocks (city buildings)
            drawRect(
                color = Color(0xFFF3F5F7),
                topLeft = Offset(0f, 0f),
                size = Size(width, height)
            )

            // Building lots
            drawRoundRect(
                color = Color(0xFFE2E7EC),
                topLeft = Offset(width * 0.05f, height * 0.08f),
                size = Size(width * 0.38f, height * 0.35f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f)
            )

            drawRoundRect(
                color = Color(0xFFE2E7EC),
                topLeft = Offset(width * 0.55f, height * 0.05f),
                size = Size(width * 0.40f, height * 0.38f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f)
            )

            drawRoundRect(
                color = Color(0xFFE2E7EC),
                topLeft = Offset(width * 0.05f, height * 0.55f),
                size = Size(width * 0.42f, height * 0.38f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f)
            )

            drawRoundRect(
                color = Color(0xFFE2E7EC),
                topLeft = Offset(width * 0.58f, height * 0.52f),
                size = Size(width * 0.38f, height * 0.40f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f)
            )

            // Diagonal & intersecting streets (Off-white / light slate)
            val streetWidth = 44f
            val roadColor = Color(0xFFFFFFFF)

            // Main boulevard
            val mainRoad = Path().apply {
                moveTo(width * 0.1f, 0f)
                lineTo(width * 0.85f, height)
            }
            drawPath(
                path = mainRoad,
                color = roadColor,
                style = Stroke(width = streetWidth)
            )

            // Cross avenue
            val crossRoad = Path().apply {
                moveTo(0f, height * 0.48f)
                lineTo(width, height * 0.44f)
            }
            drawPath(
                path = crossRoad,
                color = roadColor,
                style = Stroke(width = streetWidth * 0.9f)
            )

            // Connecting street
            val subRoad = Path().apply {
                moveTo(width * 0.48f, 0f)
                lineTo(width * 0.52f, height)
            }
            drawPath(
                path = subRoad,
                color = roadColor,
                style = Stroke(width = streetWidth * 0.7f)
            )

            // Street name labels simulation
            // Delivery Route polyline if tracking
            if (showDriver) {
                val routePath = Path().apply {
                    moveTo(width * 0.22f, height * 0.18f) // Restaurant position
                    lineTo(width * 0.48f, height * 0.45f)
                    lineTo(width * 0.68f, height * 0.72f) // Customer destination
                }

                // Route shadow
                drawPath(
                    path = routePath,
                    color = Color(0x33FF4800),
                    style = Stroke(width = 16f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                )

                // Route dashed or vibrant line
                drawPath(
                    path = routePath,
                    color = AzoomaOrange,
                    style = Stroke(
                        width = 8f,
                        cap = androidx.compose.ui.graphics.StrokeCap.Round,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(25f, 15f), 0f)
                    )
                )

                // Draw Store point
                drawCircle(
                    color = Color(0xFF1E2124),
                    radius = 18f,
                    center = Offset(width * 0.22f, height * 0.18f)
                )
                drawCircle(
                    color = Color.White,
                    radius = 8f,
                    center = Offset(width * 0.22f, height * 0.18f)
                )
            }

            // Customer Destination Pin Pulse
            val destX = width * 0.68f
            val destY = height * 0.70f

            drawCircle(
                color = Color(0x33FF4800),
                radius = 28f * pulseScale,
                center = Offset(destX, destY)
            )
        }

        // Overlay Customer Pin Badge
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = 60.dp, y = 50.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White,
                    shadowElevation = 4.dp
                ) {
                    Text(
                        text = destinationLabel,
                        style = AppTypography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = AzoomaTextPrimary
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "موقع التوصيل",
                    tint = AzoomaOrange,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        // Animated Motorcycle courier if tracking
        if (showDriver) {
            val driverX = (150 * driverProgress).dp
            val driverY = (110 * driverProgress).dp
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 50.dp + driverX, y = 30.dp + driverY)
                    .shadow(8.dp, CircleShape)
                    .clip(CircleShape)
                    .background(AzoomaOrange)
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.TwoWheeler,
                    contentDescription = "السائق",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // Watermark like Google in screenshot 6
        Text(
            text = "Google",
            style = AppTypography.bodySmall.copy(
                color = Color(0x88757575),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp)
        )
    }
}
