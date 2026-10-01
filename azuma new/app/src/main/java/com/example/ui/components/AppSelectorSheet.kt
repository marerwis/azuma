package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSelectorSheet(
    onDismiss: () -> Unit,
    onAppSelected: (String) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "تحديد التطبيق",
                style = AppTypography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = AzoomaTextPrimary,
                    fontSize = 18.sp
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Two app icons (Screenshot 22)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Facebook App 1
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable {
                        onAppSelected("Facebook")
                        onDismiss()
                    }
                ) {
                    Surface(
                        modifier = Modifier.size(56.dp),
                        shape = CircleShape,
                        color = Color(0xFF1877F2)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = "f", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "فيسبوك", style = AppTypography.bodySmall.copy(fontWeight = FontWeight.Medium))
                }

                // Facebook App 2 / Browser
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable {
                        onAppSelected("Browser")
                        onDismiss()
                    }
                ) {
                    Surface(
                        modifier = Modifier.size(56.dp),
                        shape = CircleShape,
                        color = Color(0xFF1877F2)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = "f", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "فيسبوك", style = AppTypography.bodySmall.copy(fontWeight = FontWeight.Medium))
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Cancel Button
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF3F4F6)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "إلغاء",
                    style = AppTypography.titleMedium.copy(
                        color = AzoomaTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}
