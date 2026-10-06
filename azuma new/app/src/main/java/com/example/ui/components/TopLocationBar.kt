package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun TopLocationBar(
    currentAddressName: String,
    onAddressClick: () -> Unit,
    onSearchClick: () -> Unit,
    onNotificationClick: () -> Unit,
    unreadNotifCount: Int = 0,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Address Pill Selector (Matches Screenshot 2: "التوصيل إلى الكعب العالي v")
        Surface(
            modifier = Modifier
                .weight(1f)
                .testTag("location_selector_btn")
                .clip(RoundedCornerShape(20.dp))
                .clickable { onAddressClick() },
            color = Color.White.copy(alpha = 0.85f),
            shape = RoundedCornerShape(20.dp),
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "التوصيل إلى ",
                    style = AppTypography.bodyMedium.copy(
                        color = AzoomaTextSecondary,
                        fontSize = 13.sp
                    )
                )
                Text(
                    text = currentAddressName,
                    style = AppTypography.titleSmall.copy(
                        color = AzoomaTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "اختيار العنوان",
                    tint = AzoomaOrange,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Search shortcut
        Surface(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .clickable { onSearchClick() },
            color = Color.White,
            shape = CircleShape,
            shadowElevation = 2.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "البحث",
                    tint = AzoomaTextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Notification button with badge
        Surface(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .clickable { onNotificationClick() },
            color = Color.White,
            shape = CircleShape,
            shadowElevation = 2.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.NotificationsNone,
                    contentDescription = "الإشعارات",
                    tint = AzoomaTextPrimary,
                    modifier = Modifier.size(22.dp)
                )
                if (unreadNotifCount > 0) {
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = (-8).dp, y = 8.dp)
                            .clip(CircleShape)
                            .background(AzoomaOrange)
                    )
                }
            }
        }
    }
}
