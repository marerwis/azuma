package com.example.ui.components

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.LibyanFlagIcon
import com.example.ui.theme.*
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthSheet(
    isRegister: Boolean,
    onDismiss: () -> Unit,
    /** Called with (idToken, fullName) after Firebase verifies the OTP and we have a JWT */
    onSuccess: (idToken: String, fullName: String) -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity

    // ── State ──────────────────────────────────────────────────────────────
    var step by remember { mutableIntStateOf(1) }   // 1 = Phone, 2 = OTP
    var fullName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }

    // Firebase phone auth state
    var verificationId by remember { mutableStateOf<String?>(null) }
    var resendToken by remember { mutableStateOf<PhoneAuthProvider.ForceResendingToken?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // ── Firebase Phone Auth callbacks ──────────────────────────────────────
    val callbacks = remember {
        object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            // Auto-retrieval or instant verification (e.g. same device that sent SMS)
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                isLoading = true
                errorMessage = null
                FirebaseAuth.getInstance()
                    .signInWithCredential(credential)
                    .addOnSuccessListener { result ->
                        result.user?.getIdToken(true)
                            ?.addOnSuccessListener { tokenResult ->
                                isLoading = false
                                tokenResult.token?.let { token ->
                                    onSuccess(token, fullName)
                                }
                            }
                            ?.addOnFailureListener { e ->
                                isLoading = false
                                errorMessage = "فشل الحصول على التوكن: ${e.localizedMessage}"
                            }
                    }
                    .addOnFailureListener { e ->
                        isLoading = false
                        errorMessage = "فشل التحقق التلقائي: ${e.localizedMessage}"
                    }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                isLoading = false
                errorMessage = when (e) {
                    is FirebaseAuthInvalidCredentialsException -> "رقم الهاتف غير صالح"
                    else -> "فشل إرسال الرمز: ${e.localizedMessage}"
                }
            }

            override fun onCodeSent(
                id: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                isLoading = false
                verificationId = id
                resendToken = token
                step = 2
            }
        }
    }

    // ── Helper: send SMS OTP ───────────────────────────────────────────────
    fun sendOtp(forceResend: Boolean = false) {
        if (activity == null) {
            errorMessage = "خطأ في التطبيق: لا يمكن إطلاق المصادقة"
            return
        }
        val phone = "+218${phoneNumber.trimStart('0')}"
        isLoading = true
        errorMessage = null

        val optionsBuilder = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
            .setPhoneNumber(phone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)

        if (forceResend && resendToken != null) {
            optionsBuilder.setForceResendingToken(resendToken!!)
        }

        PhoneAuthProvider.verifyPhoneNumber(optionsBuilder.build())
    }

    // ── Helper: verify OTP manually ────────────────────────────────────────
    fun verifyOtp() {
        val vid = verificationId ?: run {
            errorMessage = "انتهت الجلسة، أعد إرسال الرمز"
            return
        }
        if (otpCode.length != 6) {
            errorMessage = "الرمز يجب أن يتكون من 6 أرقام"
            return
        }
        isLoading = true
        errorMessage = null

        val credential = PhoneAuthProvider.getCredential(vid, otpCode)
        FirebaseAuth.getInstance()
            .signInWithCredential(credential)
            .addOnSuccessListener { result ->
                result.user?.getIdToken(true)
                    ?.addOnSuccessListener { tokenResult ->
                        isLoading = false
                        tokenResult.token?.let { token ->
                            onSuccess(token, fullName)
                        } ?: run { errorMessage = "لم يتم إصدار التوكن" }
                    }
                    ?.addOnFailureListener { e ->
                        isLoading = false
                        errorMessage = "فشل الحصول على التوكن: ${e.localizedMessage}"
                    }
            }
            .addOnFailureListener { e ->
                isLoading = false
                errorMessage = when (e) {
                    is FirebaseAuthInvalidCredentialsException -> "رمز التحقق غير صحيح"
                    else -> "فشل التحقق: ${e.localizedMessage}"
                }
            }
    }

    // ── UI ─────────────────────────────────────────────────────────────────
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(48.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFFD1D5DB))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Title ──────────────────────────────────────────────────────
            Text(
                text = if (isRegister) "تسجيل حساب جديد" else "تسجيل الدخول",
                style = AppTypography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = AzoomaTextPrimary
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (step == 1)
                    "أدخل رقم هاتفك لاستلام رمز التحقق وتأكيد حسابك"
                else
                    "تم إرسال رمز التحقق إلى +218${phoneNumber.trimStart('0')}",
                style = AppTypography.bodySmall.copy(
                    color = AzoomaTextSecondary,
                    textAlign = TextAlign.Center
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ── Error Banner ───────────────────────────────────────────────
            if (errorMessage != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFFFFEDED),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = errorMessage!!,
                        modifier = Modifier.padding(12.dp),
                        style = AppTypography.bodySmall.copy(color = Color(0xFFB00020)),
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // ── Step 1: Phone Input ────────────────────────────────────────
            if (step == 1) {
                if (isRegister) {
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("الاسم الكامل") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_name_input"),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AzoomaOrange,
                            unfocusedBorderColor = AzoomaCardBorder
                        ),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }

                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { if (it.all(Char::isDigit) && it.length <= 10) phoneNumber = it },
                    label = { Text("رقم الهاتف") },
                    leadingIcon = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 12.dp, end = 4.dp)
                        ) {
                            LibyanFlagIcon()
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "+218",
                                style = AppTypography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = AzoomaTextPrimary
                                )
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_phone_input"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AzoomaOrange,
                        unfocusedBorderColor = AzoomaCardBorder
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { sendOtp() },
                    enabled = !isLoading && phoneNumber.length >= 9,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("auth_send_otp_btn"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AzoomaOrange)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "إرسال رمز التحقق",
                            style = AppTypography.titleMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

            } else {
                // ── Step 2: OTP Verification ───────────────────────────────
                OutlinedTextField(
                    value = otpCode,
                    onValueChange = { if (it.all(Char::isDigit) && it.length <= 6) otpCode = it },
                    label = { Text("رمز التحقق (6 أرقام)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_otp_input"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AzoomaOrange,
                        unfocusedBorderColor = AzoomaCardBorder
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                TextButton(onClick = { sendOtp(forceResend = true) }, enabled = !isLoading) {
                    Text(
                        text = "لم يصلك الرمز؟ إعادة الإرسال",
                        style = AppTypography.bodySmall.copy(
                            color = AzoomaOrange,
                            fontSize = 12.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { verifyOtp() },
                    enabled = !isLoading && otpCode.length == 6,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("auth_verify_btn"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AzoomaOrange)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "تأكيد والدخول للبرنامج",
                            style = AppTypography.titleMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
