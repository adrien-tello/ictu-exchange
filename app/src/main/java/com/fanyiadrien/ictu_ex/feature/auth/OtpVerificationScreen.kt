package com.fanyiadrien.ictu_ex.feature.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.MarkEmailRead
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.fanyiadrien.ictu_ex.core.navigation.Screen

@Composable
fun OtpVerificationScreen(
    navController: NavController,
    viewModel: OtpViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val digits  by viewModel.digits.collectAsStateWithLifecycle()
    val isDark  = isSystemInDarkTheme()
    val focusManager = LocalFocusManager.current

    // ── Theme tokens (same palette as SignIn/SignUp) ───────────────────────────
    val gradientTop    = if (isDark) Color(0xFF4A00A0) else Color(0xFF6200EE)
    val gradientBottom = if (isDark) Color(0xFF2B2930) else Color(0xFF9C4DCC)
    val cardBg         = if (isDark) Color(0xFF2B2930) else Color.White
    val cardContent    = if (isDark) Color(0xFFEADDFF) else Color(0xFF1C1B1F)
    val subtitleColor  = if (isDark) Color(0xFFCCC2DC) else Color(0xFF49454F)
    val primaryPurple  = if (isDark) Color(0xFFBB86FC) else Color(0xFF6200EE)
    val errorColor     = if (isDark) Color(0xFFF2B8B5) else Color(0xFFB3261E)
    val digitBg        = if (isDark) Color(0xFF1C1B1F) else Color(0xFFF6F0FF)
    val digitBorder    = if (isDark) Color(0xFF625B71) else Color(0xFFCAC4D0)

    // Focus requesters for each digit box
    val focusRequesters = remember { List(6) { FocusRequester() } }

    // Auto-request focus on first box when screen opens
    LaunchedEffect(Unit) {
        focusRequesters[0].requestFocus()
    }

    // Show resend confirmation snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.resendMessage) {
        uiState.resendMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissResendMessage()
        }
    }

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost   = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {

            // ── Curved gradient header ────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.40f)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(gradientTop, gradientBottom)
                        ),
                        shape = RoundedCornerShape(bottomStart = 56.dp, bottomEnd = 56.dp)
                    )
            )

            // ── Back button ───────────────────────────────────────────────
            IconButton(
                onClick  = { navController.popBackStack() },
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(16.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f))
            ) {
                Icon(
                    imageVector        = Icons.Rounded.ArrowBackIosNew,
                    contentDescription = "Back",
                    tint               = Color.White
                )
            }

            AnimatedVisibility(
                visible = visible,
                enter   = fadeIn(tween(400)) + slideInVertically(
                    tween(400), initialOffsetY = { it / 10 }
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(56.dp))

                    // ── Email icon badge ──────────────────────────────────
                    Surface(
                        modifier        = Modifier.size(80.dp),
                        shape           = RoundedCornerShape(20.dp),
                        color           = Color.White,
                        shadowElevation = 12.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector        = Icons.Rounded.MarkEmailRead,
                                contentDescription = null,
                                modifier           = Modifier.size(44.dp),
                                tint               = primaryPurple
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // ── Main card ─────────────────────────────────────────
                    Card(
                        modifier  = Modifier.fillMaxWidth(),
                        shape     = RoundedCornerShape(28.dp),
                        colors    = CardDefaults.cardColors(containerColor = cardBg),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = if (isDark) 8.dp else 4.dp
                        )
                    ) {
                        Column(
                            modifier            = Modifier
                                .padding(28.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text  = "Verify Your Email",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = cardContent
                            )

                            Spacer(Modifier.height(8.dp))

                            Text(
                                text      = "We sent a 6-digit code to",
                                style     = MaterialTheme.typography.bodyMedium,
                                color     = subtitleColor,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text      = uiState.email,
                                style     = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color     = primaryPurple,
                                textAlign = TextAlign.Center
                            )

                            Spacer(Modifier.height(32.dp))

                            // ── 6-digit OTP boxes ─────────────────────────
                            Row(
                                modifier              = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                digits.forEachIndexed { index, digit ->
                                    OtpDigitBox(
                                        digit          = digit,
                                        isFocused      = false,
                                        isDark         = isDark,
                                        digitBg        = digitBg,
                                        digitBorder    = digitBorder,
                                        primaryPurple  = primaryPurple,
                                        errorColor     = errorColor,
                                        hasError       = uiState.errorMessage != null,
                                        focusRequester = focusRequesters[index],
                                        modifier       = Modifier.weight(1f),
                                        onValueChange  = { newVal ->
                                            viewModel.onDigitChanged(index, newVal)
                                            when {
                                                // Typed a digit → move to next
                                                newVal.isNotEmpty() && index < 5 -> {
                                                    focusRequesters[index + 1].requestFocus()
                                                }
                                                // Cleared → move to previous
                                                newVal.isEmpty() && index > 0 -> {
                                                    focusRequesters[index - 1].requestFocus()
                                                }
                                                // Last digit filled → hide keyboard
                                                newVal.isNotEmpty() && index == 5 -> {
                                                    focusManager.clearFocus()
                                                }
                                            }
                                        }
                                    )
                                }
                            }

                            // ── Error message ─────────────────────────────
                            AnimatedVisibility(visible = uiState.errorMessage != null) {
                                Text(
                                    text      = uiState.errorMessage ?: "",
                                    color     = errorColor,
                                    style     = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.Center,
                                    modifier  = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 10.dp)
                                )
                            }

                            Spacer(Modifier.height(28.dp))

                            // ── Verify button ─────────────────────────────
                            Button(
                                onClick  = {
                                    focusManager.clearFocus()
                                    viewModel.verify {
                                        navController.navigate(Screen.Home.route) {
                                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape    = RoundedCornerShape(16.dp),
                                colors   = ButtonDefaults.buttonColors(
                                    containerColor         = primaryPurple,
                                    contentColor           = Color.White,
                                    disabledContainerColor = primaryPurple.copy(alpha = 0.4f),
                                    disabledContentColor   = Color.White.copy(alpha = 0.6f)
                                ),
                                enabled  = viewModel.getCode().length == 6 && !uiState.isVerifying
                            ) {
                                if (uiState.isVerifying) {
                                    CircularProgressIndicator(
                                        modifier    = Modifier.size(22.dp),
                                        color       = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(
                                        "Verify Code",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }

                            Spacer(Modifier.height(20.dp))

                            // ── Resend row ────────────────────────────────
                            Row(
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier              = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text  = "Didn't receive the code? ",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = subtitleColor
                                )

                                if (uiState.secondsLeft > 0) {
                                    // Countdown
                                    Text(
                                        text  = "Resend in ${uiState.secondsLeft}s",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = primaryPurple
                                    )
                                } else {
                                    // Resend button
                                    TextButton(
                                        onClick  = viewModel::resendOtp,
                                        enabled  = !uiState.isResending,
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        if (uiState.isResending) {
                                            CircularProgressIndicator(
                                                modifier    = Modifier.size(14.dp),
                                                color       = primaryPurple,
                                                strokeWidth = 2.dp
                                            )
                                        } else {
                                            Text(
                                                text  = "Resend",
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                color = primaryPurple
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}

// ── Single OTP digit box ──────────────────────────────────────────────────────
@Composable
private fun OtpDigitBox(
    digit: String,
    isFocused: Boolean,
    isDark: Boolean,
    digitBg: Color,
    digitBorder: Color,
    primaryPurple: Color,
    errorColor: Color,
    hasError: Boolean,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = when {
            hasError  -> errorColor
            digit.isNotEmpty() -> primaryPurple
            else      -> digitBorder
        },
        animationSpec = tween(150),
        label = "digitBorderColor"
    )

    val textColor = if (isDark) Color(0xFFEADDFF) else Color(0xFF1C1B1F)

    BasicTextField(
        value         = digit,
        onValueChange = onValueChange,
        modifier      = modifier
            .aspectRatio(0.85f)
            .clip(RoundedCornerShape(12.dp))
            .background(digitBg)
            .border(
                width = if (digit.isNotEmpty() || hasError) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .focusRequester(focusRequester),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        singleLine      = true,
        cursorBrush     = SolidColor(primaryPurple),
        textStyle       = TextStyle(
            fontSize   = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign  = TextAlign.Center,
            color      = textColor
        ),
        decorationBox = { innerTextField ->
            Box(contentAlignment = Alignment.Center) {
                innerTextField()
            }
        }
    )
}
