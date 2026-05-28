package com.fanyiadrien.ictu_ex.feature.assistant.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.fanyiadrien.ictu_ex.feature.assistant.*

@Composable
fun AssistantOrb(
    modifier: Modifier = Modifier,
    size: Dp = 180.dp
) {
    val dark = isSystemInDarkTheme()
    val inf = rememberInfiniteTransition(label = "orb")

    // Breathing scale
    val scale by inf.animateFloat(
        initialValue = 0.92f, targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            tween(2200, easing = EaseInOutSine), RepeatMode.Reverse
        ),
        label = "scale"
    )

    // Outer halo alpha pulse
    val haloAlpha by inf.animateFloat(
        initialValue = 0.25f, targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            tween(1800, easing = EaseInOutSine), RepeatMode.Reverse
        ),
        label = "halo"
    )

    // Inner glow alpha
    val glowAlpha by inf.animateFloat(
        initialValue = 0.5f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(2600, easing = EaseInOutSine), RepeatMode.Reverse
        ),
        label = "glow"
    )

    Canvas(modifier = modifier.size(size)) {
        val cx = this.size.width / 2f
        val cy = this.size.height / 2f
        val r  = (this.size.minDimension / 2f) * scale

        drawOrb(cx, cy, r, haloAlpha, glowAlpha, dark)
    }
}

private fun DrawScope.drawOrb(
    cx: Float, cy: Float, r: Float,
    haloAlpha: Float, glowAlpha: Float,
    dark: Boolean
) {
    // ── Outermost halo ────────────────────────────────────────────────────────
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                OrbGlow.copy(alpha = haloAlpha * 0.3f),
                Color.Transparent
            ),
            center = Offset(cx, cy),
            radius = r * 1.9f
        ),
        radius = r * 1.9f,
        center = Offset(cx, cy)
    )

    // ── Halo ring ─────────────────────────────────────────────────────────────
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.Transparent,
                OrbMid.copy(alpha = haloAlpha * 0.5f),
                Color.Transparent
            ),
            center = Offset(cx, cy),
            radius = r * 1.35f
        ),
        radius = r * 1.35f,
        center = Offset(cx, cy)
    )

    // ── Soft bloom shadow ─────────────────────────────────────────────────────
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                OrbGlow.copy(alpha = glowAlpha * 0.45f),
                Color.Transparent
            ),
            center = Offset(cx, cy + r * 0.15f),
            radius = r * 1.1f
        ),
        radius = r * 1.1f,
        center = Offset(cx, cy + r * 0.15f)
    )

    // ── Main orb body ─────────────────────────────────────────────────────────
    drawCircle(
        brush = Brush.radialGradient(
            colors = if (dark) listOf(
                Color(0xFFFFFFFF),
                OrbMid,
                OrbCore,
                OrbGlow,
                Color(0xFF4A00A0)
            ) else listOf(
                Color(0xFFFFFFFF),
                OrbOuter,
                OrbMid,
                OrbCore,
                OrbGlow
            ),
            center = Offset(cx * 0.85f, cy * 0.8f),
            radius = r
        ),
        radius = r,
        center = Offset(cx, cy)
    )

    // ── Inner glow overlay ────────────────────────────────────────────────────
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.White.copy(alpha = glowAlpha * 0.35f),
                Color.Transparent
            ),
            center = Offset(cx * 0.8f, cy * 0.75f),
            radius = r * 0.6f
        ),
        radius = r * 0.6f,
        center = Offset(cx * 0.8f, cy * 0.75f)
    )

    // ── Capsule eyes ──────────────────────────────────────────────────────────
    val eyeW = r * 0.09f
    val eyeH = r * 0.22f
    val eyeY = cy - eyeH / 2f
    val eyeGap = r * 0.18f

    // Left eye
    drawRoundRect(
        color = Color.White,
        topLeft = Offset(cx - eyeGap - eyeW, eyeY),
        size = Size(eyeW, eyeH),
        cornerRadius = CornerRadius(eyeW / 2f)
    )
    // Right eye
    drawRoundRect(
        color = Color.White,
        topLeft = Offset(cx + eyeGap, eyeY),
        size = Size(eyeW, eyeH),
        cornerRadius = CornerRadius(eyeW / 2f)
    )
}
