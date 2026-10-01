package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

private data class FaqItem(
    val question: String,
    val answer: String
)

private val faqList = listOf(
    FaqItem(
        question = "طلبت من عنوان خاطئ، هل يمكنني تغييره؟",
        answer = "إذا لم يتم قبول الطلب من قبل المتجر بعد، يمكنك إلغاؤه وإعادة الطلب بعنوانك الصحيح. أما إذا كان قيد التحضير أو مع السائق، فيرجى التواصل مع فريق الدعم الفني فوراً من خلال زر 'تواصل معنا' لمساعدة السائق في توجيهه للعنوان الجديد."
    ),
    FaqItem(
        question = "هل يمكنني تغيير طريقة الدفع؟",
        answer = "يمكنك اختيار وتغيير طريقة الدفع بكل حرية من شاشة السلة وتفاصيل الطلب قبل النقر على 'تأكيد الطلب'. بعد تأكيد وإرسال الطلب لا يمكن تعديل طريقة الدفع للطلب الحالي."
    ),
    FaqItem(
        question = "هل يمكن تعديل الطلب؟",
        answer = "يمكنك إلغاء الطلب مجاناً قبل قبوله من المطعم وإعادة إرسال طلب جديد مع الوجبات المطلوبة. إذا قَبِل المتجر طلبك بالفعل، يمكنك الاتصال بالمتجر مباشرة عبر رقم الهاتف المرفق."
    ),
    FaqItem(
        question = "ماذا أفعل إذا تأخر المطعم/المتجر في قبول الطلب؟",
        answer = "يتم تنبيه المطعم فوراً عبر النظام. إذا لم يستجب المطعم خلال 10 دقائق، سيقوم النظام تلقائياً بإلغاء الطلب وإرجاع أي مبالغ مدفوعة بالكامل إلى محفظتك الإلكترونية."
    ),
    FaqItem(
        question = "ماذا أفعل إذا تأخر الطلب؟",
        answer = "يمكنك متابعة خط سير السائق مباشرة في الوقت الحقيقي من خريطة التتبع، كما يمكنك الاتصال بالسائق هاتفياً مباشرة لمعرفة موقعه أو مراسلة الدعم الفني."
    ),
    FaqItem(
        question = "ماذا أفعل إذا استلمت صنف خاطئ؟",
        answer = "نعتذر بشدة عن ذلك! يرجى تصوير الصنف المستلم ومراسلتنا فوراً عبر خيار 'شاركنا ملاحظاتك' في صفحة الحساب، وسيقوم فريق الدعم بالتحقق وتعويضك فوراً."
    )
)

@Composable
fun HelpScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var expandedIndices by remember { mutableStateOf(setOf<Int>()) }

    val filteredFaq = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            faqList
        } else {
            faqList.filter {
                it.question.contains(searchQuery.trim(), ignoreCase = true) ||
                it.answer.contains(searchQuery.trim(), ignoreCase = true)
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding(),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Top Bar (Screenshot 7 & 8)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.testTag("help_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "رجوع",
                        tint = AzoomaTextPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Text(
                    text = "المساعدة",
                    style = AppTypography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = AzoomaTextPrimary
                    )
                )

                Spacer(modifier = Modifier.size(48.dp))
            }
        }

        // Top Illustration & Question (Screenshot 7 & 8)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Headset Icon with soft circular container
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFF1EB)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Headphones,
                        contentDescription = null,
                        tint = AzoomaOrange,
                        modifier = Modifier.size(44.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "كيف يمكننا مساعدتك؟",
                    style = AppTypography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = AzoomaTextPrimary,
                        fontSize = 24.sp
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = "بحث",
                            style = AppTypography.bodyMedium.copy(color = Color(0xFF9E9E9E))
                        )
                    },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "بحث",
                            tint = Color(0xFF263238),
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFFF3F5F7)),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color(0xFFF3F5F7),
                        unfocusedContainerColor = Color(0xFFF3F5F7)
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(24.dp))

                // FAQ Header
                Text(
                    text = "الأسئلة الشائعة",
                    style = AppTypography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = AzoomaTextPrimary,
                        fontSize = 17.sp
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )
            }
        }

        // Expandable FAQ Cards (Screenshot 7 & 8)
        itemsIndexed(filteredFaq) { index, item ->
            val isExpanded = expandedIndices.contains(index)
            val rotationState by animateFloatAsState(
                targetValue = if (isExpanded) 180f else 0f,
                label = "rotation"
            )

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp))
                    .clickable {
                        expandedIndices = if (isExpanded) {
                            expandedIndices - index
                        } else {
                            expandedIndices + index
                        }
                    },
                shape = RoundedCornerShape(16.dp),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isExpanded) "طي" else "توسيع",
                            tint = AzoomaTextPrimary,
                            modifier = Modifier
                                .size(24.dp)
                                .rotate(rotationState)
                        )

                        Text(
                            text = item.question,
                            style = AppTypography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = AzoomaTextPrimary,
                                fontSize = 15.sp
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 12.dp),
                            textAlign = TextAlign.Start
                        )
                    }

                    AnimatedVisibility(visible = isExpanded) {
                        Column {
                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = Color(0xFFF3F4F6))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = item.answer,
                                style = AppTypography.bodyMedium.copy(
                                    color = Color(0xFF4B5563),
                                    lineHeight = 22.sp,
                                    fontSize = 13.5.sp
                                ),
                                textAlign = TextAlign.Start
                            )
                        }
                    }
                }
            }
        }
    }
}
