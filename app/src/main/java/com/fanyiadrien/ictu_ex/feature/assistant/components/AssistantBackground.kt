package com.fanyiadrien.ictu_ex.feature.assistant.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.fanyiadrien.ictu_ex.feature.assistant.*

@Composable
fun AssistantBackground(modifier: Modifier = Modifier) {
    val dark = isSystemInDarkTheme()

    val inf = rememberInfiniteTransition(label = "bg")

    // Slow floating offsets for the blobs
    val blob1X by inf.animateFloat(
        initialValue = -60f, targetValue = 60f,
        animationSpec = infiniteRepeatable(tween(8000, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "b1x"
    )
    val blob2Y by inf.animateFloat(
        initialValue = 40f, targetValue = -40f,
        animationSpec = infiniteRepeatable(tween(10000, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "b2y"
    )
    val blob3X by inf.animateFloat(
        initialValue = 30f, targetValue = -50f,
        animationSpec = infiniteRepeatable(tween(12000, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "b3x"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // ── Base gradient ─────────────────────────────────────────────────────
        drawRect(
            brush = if (dark) Brush.linearGradient(
                colors = listOf(BgDarkDeep, BgDarkViolet, BgDarkBlue),
                start = Offset(0f, 0f), end = Offset(w, h)
            ) else Brush.linearGradient(
                colors = listOf(BgPurpleLight, BgLavender, BgPink, BgBlue, BgWhite),
                start = Offset(0f, 0f), end = Offset(w, h)
            )
        )

        // ── Blob 1 — top-left radial ──────────────────────────────────────────
        drawCircle(
            brush = Brush.radialGradient(
                colors = if (dark)
                    listOf(Color(0x55AB5CF7), Color.Transparent)
                else
                    listOf(Color(0x66C4B5FD), Color.Transparent),
                center = Offset(w * 0.15f + blob1X, h * 0.18f),
                radius = w * 0.55f
            ),
            radius = w * 0.55f,
            center = Offset(w * 0.15f + blob1X, h * 0.18f)
        )

        // ── Blob 2 — bottom-right radial ──────────────────────────────────────
        drawCircle(
            brush = Brush.radialGradient(
                colors = if (dark)
                    listOf(Color(0x447C3AED), Color.Transparent)
                else
                    listOf(Color(0x55F9A8D4), Color.Transparent),
                center = Offset(w * 0.85f, h * 0.75f + blob2Y),
                radius = w * 0.5f
            ),
            radius = w * 0.5f,
            center = Offset(w * 0.85f, h * 0.75f + blob2Y)
        )

        // ── Blob 3 — center ambient ───────────────────────────────────────────
        drawCircle(
            brush = Brush.radialGradient(
                colors = if (dark)
                    listOf(Color(0x33BB86FC), Color.Transparent)
                else
                    listOf(Color(0x44E9D5FF), Color.Transparent),
                center = Offset(w * 0.5f + blob3X, h * 0.45f),
                radius = w * 0.65f
            ),
            radius = w * 0.65f,
            center = Offset(w * 0.5f + blob3X, h * 0.45f)
        )

        // ── Top arc glow ──────────────────────────────────────────────────────
        drawCircle(
            brush = Brush.radialGradient(
                colors = if (dark)
                    listOf(Color(0x229B59F5), Color.Transparent)
                else
                    listOf(Color(0x33DDD6FE), Color.Transparent),
                center = Offset(w * 0.5f, 0f),
                radius = w * 0.8f
            ),
            radius = w * 0.8f,
            center = Offset(w * 0.5f, 0f)
        )
    }
}
