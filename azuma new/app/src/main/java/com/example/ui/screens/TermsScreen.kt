package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun TermsScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.testTag("terms_back_btn")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "رجوع",
                    tint = AzoomaTextPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Text(
                text = "الأحكام والشروط",
                style = AppTypography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = AzoomaTextPrimary
                )
            )

            Spacer(modifier = Modifier.size(48.dp))
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                TermsSection(
                    title = "١. مقدمة والشروط العامة",
                    content = "أهلاً بك في تطبيق عزومة (Azooma). باستخدامك لهذا التطبيق، فإنك توافق على الالتزام الكامل بهذه الشروط والأحكام وجميع السياسات المعمول بها في خدمات التوصيل داخل ليبيا وشمال أفريقيا."
                )
            }
            item {
                TermsSection(
                    title = "٢. إنشاء الحساب والأمان",
                    content = "يجب على المستخدم تقديم معلومات صحيحة ودقيقة، بما في ذلك رقم الهاتف والاسم. المستخدم مسؤول عن حماية سرية حسابه وأي أنشطة تتم من خلاله."
                )
            }
            item {
                TermsSection(
                    title = "٣. الطلبات والدفع",
                    content = "تعتبر جميع الأسعار المعروضة بالدينار الليبي (د.ل) شاملة لرسوم الخدمة ومصاريف التوصيل المحددة قبل إتمام الطلب. تتوفر وسائل دفع متعددة تشمل الدفع نقداً عند الاستلام ومحفظة التطبيق وبطاقات الدفع المصرفية المحلية (سداد، تداول، يسر، ادفعلي)."
                )
            }
            item {
                TermsSection(
                    title = "٤. سياسة الإلغاء والاسترجاع",
                    content = "يحق للمستخدم إلغاء الطلب مجاناً قبل قبول المطعم له. في حال حدوث أي خطأ في الأصناف أو تأخر غير مبرر، يلتزم التطبيق بتعويض العميل فوراً من خلال رصيد المحفظة."
                )
            }
            item {
                TermsSection(
                    title = "٥. الخصوصية وحماية البيانات",
                    content = "يلتزم تطبيق عزومة بالحفاظ التام على خصوصية بيانات المستخدمين وعدم مشاركتها مع أي طرف ثالث باستثناء معلومات التوصيل الضرورية للسائق لتسليم الوجبة بنجاح."
                )
            }
        }
    }
}

@Composable
private fun TermsSection(
    title: String,
    content: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFFF9FAFB),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = AppTypography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = AzoomaTextPrimary,
                    fontSize = 16.sp
                ),
                textAlign = TextAlign.Start
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = content,
                style = AppTypography.bodyMedium.copy(
                    color = Color(0xFF4B5563),
                    lineHeight = 24.sp,
                    fontSize = 13.5.sp
                ),
                textAlign = TextAlign.Start
            )
        }
    }
}
