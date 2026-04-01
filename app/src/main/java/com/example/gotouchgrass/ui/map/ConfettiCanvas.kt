package com.example.gotouchgrass.ui.map

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

private val ConfettiColors = listOf(
    Color(0xFFE8B931), // golden yellow
    Color(0xFF2D5A3D), // forest green
    Color(0xFF4CAF50), // bright green
    Color(0xFFE91E63), // pink
    Color(0xFF9C27B0), // purple
    Color(0xFF2196F3), // blue
    Color(0xFFFF5722), // orange
    Color(0xFF00BCD4), // cyan
    Color(0xFFFFFFFF), // white
    Color(0xFFF5D060), // gold light
)

private enum class ConfettiShape { CIRCLE, RECT, DIAMOND }

private data class Particle(
    val startX: Float,       // 0..1 normalized
    val startY: Float,       // starts above screen: -0.2..0
    val speed: Float,        // fall speed multiplier
    val swayAmp: Float,      // horizontal sway amplitude
    val swayPhase: Float,    // sway phase offset
    val color: Color,
    val size: Float,
    val shape: ConfettiShape,
    val rotationSpeed: Float
)

private fun generateParticles(count: Int, seed: Long = 42L): List<Particle> {
    val rng = Random(seed)
    return List(count) {
        Particle(
            startX = rng.nextFloat(),
            startY = -rng.nextFloat() * 0.3f,
            speed = 0.4f + rng.nextFloat() * 0.6f,
            swayAmp = 0.03f + rng.nextFloat() * 0.06f,
            swayPhase = rng.nextFloat() * (2f * PI.toFloat()),
            color = ConfettiColors[rng.nextInt(ConfettiColors.size)],
            size = 8f + rng.nextFloat() * 14f,
            shape = ConfettiShape.entries[rng.nextInt(3)],
            rotationSpeed = rng.nextFloat() * 360f
        )
    }
}

@Composable
fun ConfettiCanvas(
    modifier: Modifier = Modifier,
    particleCount: Int = 80,
    durationMs: Int = 4000
) {
    val particles = remember { generateParticles(particleCount) }
    val infiniteTransition = rememberInfiniteTransition(label = "confetti")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "confettiProgress"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        particles.forEach { p ->
            // Each particle falls at its own speed; stagger start so they don't all begin at once
            val t = ((progress * 1.5f) + p.startY + p.startX * 0.3f).mod(1.5f) / 1.5f
            if (t < 0f || t > 1f) return@forEach

            val x = (p.startX + sin(t * 4f * PI.toFloat() + p.swayPhase) * p.swayAmp) * w
            val y = (t * 1.3f - 0.05f) * h
            if (y > h + p.size) return@forEach

            val alpha = when {
                t < 0.1f -> t / 0.1f
                t > 0.8f -> 1f - (t - 0.8f) / 0.2f
                else -> 1f
            }

            val rotation = t * p.rotationSpeed
            drawParticle(x, y, p.size, p.shape, p.color.copy(alpha = alpha), rotation)
        }
    }
}

private fun DrawScope.drawParticle(
    x: Float, y: Float, size: Float, shape: ConfettiShape, color: Color, rotation: Float
) {
    rotate(rotation, pivot = Offset(x, y)) {
        when (shape) {
            ConfettiShape.CIRCLE -> drawCircle(
                color = color, radius = size / 2f, center = Offset(x, y)
            )
            ConfettiShape.RECT -> drawRect(
                color = color,
                topLeft = Offset(x - size / 2f, y - size / 3f),
                size = Size(size, size * 0.6f)
            )
            ConfettiShape.DIAMOND -> {
                val path = Path().apply {
                    moveTo(x, y - size / 2f)
                    lineTo(x + size / 2.5f, y)
                    lineTo(x, y + size / 2f)
                    lineTo(x - size / 2.5f, y)
                    close()
                }
                drawPath(path, color)
            }
        }
    }
}
