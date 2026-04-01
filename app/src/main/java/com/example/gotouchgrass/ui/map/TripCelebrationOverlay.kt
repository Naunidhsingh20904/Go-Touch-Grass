package com.example.gotouchgrass.ui.map

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gotouchgrass.domain.TripSummary
import com.example.gotouchgrass.ui.theme.GoTouchGrassDimens
import kotlinx.coroutines.delay

private val GoldenYellow = Color(0xFFE8B931)
private val GoldenDark = Color(0xFFBF9520)
private val ForestGreen = Color(0xFF2D5A3D)
private val OverlayBg = Color(0xDD0A1A0F)

@Composable
fun TripCelebrationOverlay(
    summary: TripSummary,
    onDismiss: () -> Unit
) {
    var animIn by remember { mutableStateOf(false) }
    val cardScale by animateFloatAsState(
        targetValue = if (animIn) 1f else 0.5f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "cardScale"
    )
    val cardAlpha by animateFloatAsState(
        targetValue = if (animIn) 1f else 0f,
        animationSpec = tween(300),
        label = "cardAlpha"
    )
    // XP rolls up from 0
    val displayXp by animateIntAsState(
        targetValue = if (animIn) summary.xpEarned else 0,
        animationSpec = tween(1200),
        label = "xpRoll"
    )

    LaunchedEffect(Unit) {
        delay(80)
        animIn = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OverlayBg),
        contentAlignment = Alignment.Center
    ) {
        // Confetti layer behind card
        ConfettiCanvas(modifier = Modifier.fillMaxSize(), particleCount = 100, durationMs = 3500)

        // Main celebration card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(GoTouchGrassDimens.SpacingMd)
                .scale(cardScale),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F2218)),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF152C1E), Color(0xFF0F1F15))
                        )
                    )
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Trophy icon with golden glow
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(GoldenYellow.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = GoldenYellow,
                        modifier = Modifier.size(44.dp)
                    )
                }

                Text(
                    text = "TRIP COMPLETE",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 3.sp,
                    color = GoldenYellow.copy(alpha = 0.8f)
                )

                // Big XP counter — rolls up like a slot machine
                Text(
                    text = "+$displayXp XP",
                    fontSize = 52.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = GoldenYellow,
                    textAlign = TextAlign.Center
                )

                // Mini stats row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    CelebrationStat(label = "Distance", value = summary.formattedDistance)
                    CelebrationStat(label = "Duration", value = summary.formattedDuration)
                    CelebrationStat(label = "Captures", value = summary.captureCount.toString())
                }

                Spacer(Modifier.height(4.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldenYellow,
                        contentColor = ForestGreen
                    )
                ) {
                    Text(
                        "See Full Summary",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun CelebrationStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = Color.White.copy(alpha = 0.5f)
        )
    }
}
