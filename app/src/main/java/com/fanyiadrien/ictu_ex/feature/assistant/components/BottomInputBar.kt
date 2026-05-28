package com.fanyiadrien.ictu_ex.feature.assistant.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fanyiadrien.ictu_ex.feature.assistant.*

@Composable
fun BottomInputBar(
    inputText: String,
    onInputChanged: (String) -> Unit,
    onAddClick: () -> Unit,
    onMicClick: () -> Unit,
    isListening: Boolean,
    modifier: Modifier = Modifier
) {
    val dark = isSystemInDarkTheme()

    // Mic glow pulse when listening
    val inf = rememberInfiniteTransition(label = "mic")
    val micGlow by inf.animateFloat(
        initialValue = 0.7f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "micGlow"
    )

    val containerBg = if (dark)
        Brush.linearGradient(listOf(Color(0xCC1A1035), Color(0xBB0D0A1A)))
    else
        Brush.linearGradient(listOf(Color(0xEEFFFFFF), Color(0xDDF5F0FF)))

    val borderColor = if (dark) GlassBorderD else GlassBorderL
    val hintColor   = if (dark) TextSecondaryD.copy(alpha = 0.5f) else TextSecondaryL.copy(alpha = 0.5f)
    val textColor   = if (dark) TextPrimaryD else TextPrimaryL

    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(24.dp, RoundedCornerShape(32.dp), ambientColor = OrbGlow.copy(0.15f))
            .clip(RoundedCornerShape(32.dp))
            .background(containerBg)
            .border(1.dp, borderColor, RoundedCornerShape(32.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // ── Add button ────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    if (dark) Color(0x44BB86FC) else Color(0x33C4B5FD)
                )
                .border(0.8.dp, borderColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            IconButton(onClick = onAddClick, modifier = Modifier.size(40.dp)) {
                Icon(
                    Icons.Rounded.Add,
                    contentDescription = "Add",
                    tint = if (dark) TextSecondaryD else TextSecondaryL,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // ── Text field ────────────────────────────────────────────────────────
        Box(modifier = Modifier.weight(1f)) {
            BasicTextField(
                value = inputText,
                onValueChange = onInputChanged,
                textStyle = TextStyle(
                    color = textColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal
                ),
                cursorBrush = SolidColor(OrbCore),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            if (inputText.isEmpty()) {
                Text(
                    text = "Ask me anything...",
                    color = hintColor,
                    fontSize = 14.sp
                )
            }
        }

        // ── Mic button ────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = if (isListening)
                            listOf(
                                OrbCore.copy(alpha = micGlow),
                                OrbGlow.copy(alpha = micGlow)
                            )
                        else listOf(OrbCore, OrbGlow)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            IconButton(onClick = onMicClick, modifier = Modifier.size(44.dp)) {
                Icon(
                    Icons.Rounded.Mic,
                    contentDescription = "Mic",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
