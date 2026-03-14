package com.fanyiadrien.ictu_ex.feature.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.fanyiadrien.ictu_ex.R
import com.fanyiadrien.ictu_ex.core.navigation.Screen
import com.fanyiadrien.ictu_ex.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OnboardingScreen(
    navController: NavController,
    viewModel: OnboardingViewModel = viewModel()
) {
    val pages = listOf(
        OnboardingPageData(
            imageRes = R.drawable.computer,
            title = "ICTU-Exchange",
            description = "A verified campus marketplace that makes trading textbooks and electronics safer and easier for students."
        ),
        OnboardingPageData(
            imageRes = R.drawable.book1,
            title = "Smart Trading",
            description = "Uses hardware sensors and biometrics to ensure secure transactions even without internet access."
        ),
        OnboardingPageData(
            imageRes = R.drawable.air,
            title = "Campus Essentials",
            description = "Find everything you need for your studies, from laptops to lab coats, all within your campus community."
        ),
        OnboardingPageData(
            imageRes = R.drawable.watch,
            title = "Stay Notified",
            description = "Get real-time updates on new listings and messages from buyers and sellers instantly."
        )
    )

    // Auto-swipe Logic (3 seconds is a normal time delay)
    LaunchedEffect(Unit) {
        while (true) {
            delay(3000)
            val nextIndex = (viewModel.currentPage + 1) % pages.size
            viewModel.goToPage(nextIndex)
        }
    }

    val currentPageData = pages[viewModel.currentPage]

    // Entrance animation
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
    }

    val entranceAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(1000),
        label = "entranceAlpha"
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .alpha(entranceAlpha)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // ── TOP: Image Slider with Distinct Containers ──────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.2f)
                        .padding(top = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedContent(
                        targetState = viewModel.currentPage,
                        transitionSpec = {
                            (slideInHorizontally(animationSpec = tween(600), initialOffsetX = { it }) + fadeIn(tween(600)))
                                .togetherWith(slideOutHorizontally(animationSpec = tween(600), targetOffsetX = { -it }) + fadeOut(tween(600)))
                        },
                        label = "imageSlider"
                    ) { pageIndex ->
                        OnboardingImageContainer(
                            imageRes = pages[pageIndex].imageRes,
                            pageIndex = pageIndex
                        )
                    }
                }

                // ── BOTTOM CONTENT ───────────────────────────────────────────
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    AnimatedContent(
                        targetState = currentPageData,
                        transitionSpec = {
                            fadeIn(tween(400)) togetherWith fadeOut(tween(400))
                        },
                        label = "textTransition"
                    ) { data ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = data.title,
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 28.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    textAlign = TextAlign.Center
                                )
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = data.description,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 24.sp
                                ),
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    PageIndicator(
                        currentPage = viewModel.currentPage,
                        totalPages = pages.size
                    )

                    Spacer(modifier = Modifier.height(40.dp))

                    // ── Buttons ──────────────────────────────────────────────
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(
                            onClick = {
                                navController.navigate(Screen.CheckStatus.route) {
                                    popUpTo(Screen.Onboarding.route) { inclusive = true }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text(
                                text = "Get Started",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }

                        // Transparent Skip Button
                        TextButton(
                            onClick = {
                                navController.navigate(Screen.CheckStatus.route) {
                                    popUpTo(Screen.Onboarding.route) { inclusive = true }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                        ) {
                            Text(
                                text = "Skip",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingImageContainer(imageRes: Int, pageIndex: Int) {
    // Distinct backgrounds/shapes for each page
    val backgroundBrush = when (pageIndex) {
        0 -> Brush.linearGradient(listOf(Purple80.copy(0.15f), Color.White))
        1 -> Brush.radialGradient(listOf(Color(0xFFE0F7FA), Color.White))
        2 -> Brush.verticalGradient(listOf(Color(0xFFFFF3E0), Color.White))
        else -> Brush.sweepGradient(listOf(Color(0xFFF3E5F5), Color.White))
    }

    val shape = when (pageIndex) {
        0 -> RoundedCornerShape(topStart = 80.dp, bottomEnd = 80.dp)
        1 -> RoundedCornerShape(40.dp)
        2 -> RoundedCornerShape(topEnd = 100.dp, bottomStart = 100.dp)
        else -> RoundedCornerShape(percent = 50) // Circle
    }

    Box(
        modifier = Modifier
            .size(280.dp)
            .clip(shape)
            .background(backgroundBrush),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = null,
            modifier = Modifier
                .size(180.dp)
                .padding(16.dp),
            contentScale = ContentScale.Fit
        )
    }
}

data class OnboardingPageData(
    val imageRes: Int,
    val title: String,
    val description: String
)

@Composable
fun PageIndicator(currentPage: Int, totalPages: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalPages) { index ->
            val isActive = index == currentPage
            Box(
                modifier = Modifier
                    .height(8.dp)
                    .width(if (isActive) 24.dp else 8.dp)
                    .background(
                        color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(4.dp)
                    )
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun OnboardingScreenPreview() {
    IctuExTheme {
        OnboardingScreen(navController = rememberNavController())
    }
}