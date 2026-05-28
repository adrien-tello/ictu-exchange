package com.fanyiadrien.ictu_ex.feature.assistant

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.fanyiadrien.ictu_ex.feature.assistant.components.*

@Composable
fun AssistantScreen(
    navController: NavController,
    viewModel: AssistantViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState
    val dark = isSystemInDarkTheme()

    // Entrance fade
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(800),
        label = "entrance"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(alpha)
    ) {
        // ── Animated background ───────────────────────────────────────────────
        AssistantBackground(modifier = Modifier.fillMaxSize())

        // ── Main content ──────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(Modifier.height(8.dp))

            // ── Header row ────────────────────────────────────────────────────
            AssistantHeader(
                dark = dark,
                onMenuClick = { navController.popBackStack() },
                onSettingsClick = {}
            )

            Spacer(Modifier.height(28.dp))

            // ── Greeting ──────────────────────────────────────────────────────
            GreetingSection(userName = uiState.userName, dark = dark)

            Spacer(Modifier.height(32.dp))

            // ── AI Orb ────────────────────────────────────────────────────────
            AssistantOrbSection(dark = dark)

            Spacer(Modifier.height(36.dp))

            // ── Last prompt feedback ──────────────────────────────────────────
            AnimatedVisibility(
                visible = uiState.lastPrompt != null,
                enter = fadeIn() + slideInVertically(),
                exit  = fadeOut()
            ) {
                uiState.lastPrompt?.let { prompt ->
                    LastPromptBubble(prompt = prompt, dark = dark)
                    Spacer(Modifier.height(16.dp))
                }
            }

            // ── Quick action chips ────────────────────────────────────────────
            QuickActionChips(
                dark = dark,
                onChipSelected = viewModel::onChipSelected
            )

            Spacer(Modifier.weight(1f))

            // ── Bottom input bar ──────────────────────────────────────────────
            BottomInputBar(
                inputText    = uiState.inputText,
                onInputChanged = viewModel::onInputChanged,
                onAddClick   = {},
                onMicClick   = viewModel::onMicToggle,
                isListening  = uiState.isListening,
                modifier     = Modifier.padding(bottom = 16.dp)
            )
        }
    }
}

// ── Header ────────────────────────────────────────────────────────────────────

@Composable
private fun AssistantHeader(
    dark: Boolean,
    onMenuClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        GlassIconButton(icon = Icons.Rounded.Menu, dark = dark, onClick = onMenuClick)

        // App title pill
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(
                    if (dark) Color(0x44BB86FC) else Color(0x44C4B5FD)
                )
                .border(
                    0.8.dp,
                    if (dark) GlassBorderD else GlassBorderL,
                    RoundedCornerShape(20.dp)
                )
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Text(
                text = "ICTU AI",
                color = if (dark) TextSecondaryD else TextSecondaryL,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        GlassIconButton(icon = Icons.Rounded.Settings, dark = dark, onClick = onSettingsClick)
    }
}

@Composable
private fun GlassIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    dark: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .shadow(8.dp, CircleShape, ambientColor = OrbGlow.copy(0.2f))
            .clip(CircleShape)
            .background(if (dark) Color(0x55251545) else Color(0xCCFFFFFF))
            .border(0.8.dp, if (dark) GlassBorderD else GlassBorderL, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        IconButton(onClick = onClick, modifier = Modifier.size(42.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (dark) TextSecondaryD else TextSecondaryL,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ── Greeting ──────────────────────────────────────────────────────────────────

@Composable
private fun GreetingSection(userName: String, dark: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Hello, $userName!",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = if (dark) TextPrimaryD else TextPrimaryL,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "How can I help you today?",
            fontSize = 15.sp,
            fontWeight = FontWeight.Normal,
            color = if (dark) TextSecondaryD.copy(alpha = 0.8f) else TextSecondaryL.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
    }
}

// ── Orb section ───────────────────────────────────────────────────────────────

@Composable
private fun AssistantOrbSection(dark: Boolean) {
    Box(contentAlignment = Alignment.Center) {
        // Soft platform shadow under orb
        Box(
            modifier = Modifier
                .size(120.dp, 20.dp)
                .offset(y = 90.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            OrbGlow.copy(alpha = if (dark) 0.4f else 0.2f),
                            Color.Transparent
                        )
                    ),
                    CircleShape
                )
        )
        AssistantOrb(size = 180.dp)
    }
}

// ── Last prompt bubble ────────────────────────────────────────────────────────

@Composable
private fun LastPromptBubble(prompt: String, dark: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (dark) Color(0x44251545) else Color(0xAAF5F0FF)
            )
            .border(
                0.8.dp,
                if (dark) GlassBorderD else GlassBorderL,
                RoundedCornerShape(16.dp)
            )
            .padding(12.dp)
    ) {
        Text(
            text = "\"$prompt\"",
            color = if (dark) TextSecondaryD else TextSecondaryL,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

// ── Quick action chips ────────────────────────────────────────────────────────

private data class ChipData(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

@Composable
private fun QuickActionChips(
    dark: Boolean,
    onChipSelected: (String) -> Unit
) {
    val chips = listOf(
        ChipData("Create an image",   Icons.Rounded.Image),
        ChipData("Give me ideas",     Icons.Rounded.Lightbulb),
        ChipData("Do the task",       Icons.Rounded.TaskAlt),
        ChipData("Translate the text",Icons.Rounded.Translate)
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        chips.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                row.forEach { chip ->
                    PromptChip(
                        label   = chip.label,
                        icon    = chip.icon,
                        onClick = { onChipSelected(chip.label) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}
