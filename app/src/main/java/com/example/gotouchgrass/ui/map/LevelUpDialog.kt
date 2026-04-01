package com.example.gotouchgrass.ui.map

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import com.example.gotouchgrass.ui.theme.GoTouchGrassDimens
import kotlinx.coroutines.delay

private val GoldenYellow = Color(0xFFE8B931)
private val ForestGreen = Color(0xFF2D5A3D)
private val LevelUpBg = Color(0xEE060F0A)

@Composable
fun LevelUpDialog(
    newLevel: Int,
    onDismiss: () -> Unit
) {
    var animIn by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (animIn) 1f else 0.3f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium),
        label = "lvlScale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (animIn) 1f else 0f,
        animationSpec = tween(250),
        label = "lvlAlpha"
    )

    LaunchedEffect(Unit) {
        delay(60)
        animIn = true
        // Auto-dismiss after 5s
        delay(5000)
        onDismiss()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LevelUpBg),
        contentAlignment = Alignment.Center
    ) {
        ConfettiCanvas(
            modifier = Modifier.fillMaxSize(),
            particleCount = 120,
            durationMs = 3000
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(GoTouchGrassDimens.SpacingMd)
                .scale(scale),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF1C3A26), Color(0xFF0F2218))
                        )
                    )
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Star burst
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(3) {
                        Icon(
                            Icons.Default.Star, contentDescription = null,
                            tint = GoldenYellow, modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Text(
                    text = "LEVEL UP!",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 4.sp,
                    color = GoldenYellow
                )

                // Level badge
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(GoldenYellow.copy(alpha = 0.3f), Color.Transparent)
                            ),
                            CircleShape
                        )
                        .background(GoldenYellow.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$newLevel",
                        fontSize = 56.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = GoldenYellow,
                        textAlign = TextAlign.Center
                    )
                }

                Text(
                    text = "You're a Level $newLevel Explorer!",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Keep exploring to unlock more rewards.",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )

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
                    Text("Keep Exploring!", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                }
            }
        }
    }
}
