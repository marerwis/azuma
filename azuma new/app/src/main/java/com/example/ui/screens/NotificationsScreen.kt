package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.example.model.NotificationItem
import com.example.ui.theme.*

@Composable
fun NotificationsScreen(
    notifications: List<NotificationItem>,
    onBackClick: () -> Unit,
    onNotificationClick: (NotificationItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Header (Matching Screenshot 0)
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
                text = "الإشعارات",
                style = AppTypography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = AzoomaTextPrimary
                )
            )

            Spacer(modifier = Modifier.size(40.dp))
        }

        if (notifications.isEmpty()) {
            // Empty State (Matching Screenshot 0 with 3D Bell)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.empty_bell_3d),
                    contentDescription = "لا توجد إشعارات",
                    modifier = Modifier.size(160.dp)
                )

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = "لا توجد إشعارات حتى الآن",
                    style = AppTypography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = AzoomaTextPrimary
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "ابقَ على اطلاع على التحديثات هنا بمجرد توفرها.",
                    style = AppTypography.bodyMedium.copy(
                        color = AzoomaTextSecondary,
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp
                    )
                )
            }
        } else {
            // List of Notifications
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(notifications) { notif ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onNotificationClick(notif) }
                            .testTag("notif_item_${notif.id}"),
                        color = AzoomaBackground,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = notif.timeAgo,
                                    style = AppTypography.bodySmall.copy(
                                        color = AzoomaTextMuted,
                                        fontSize = 11.sp
                                    )
                                )
                                Text(
                                    text = notif.title,
                                    style = AppTypography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = AzoomaTextPrimary
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = notif.message,
                                style = AppTypography.bodySmall.copy(
                                    color = AzoomaTextSecondary,
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp
                                ),
                                textAlign = TextAlign.Right
                            )
                        }
                    }
                }
            }
        }
    }
}
