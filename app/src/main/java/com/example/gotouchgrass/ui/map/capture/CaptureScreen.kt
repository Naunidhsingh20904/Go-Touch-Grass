package com.example.gotouchgrass.ui.map.capture

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gotouchgrass.ui.theme.GoTouchGrassDimens
import com.example.gotouchgrass.ui.theme.GoTouchGrassTheme

@Composable
fun CaptureScreen(
    placeId: String,
    onClose: () -> Unit = {},
    onCaptured: (String) -> Unit = {},
    viewModel: CaptureViewModel = remember(placeId) { CaptureViewModel(placeId) }
) {
    val state = viewModel.uiState
    val unknownPlaceError = viewModel.unknownPlaceError

    if (state == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(GoTouchGrassDimens.SpacingMd),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(GoTouchGrassDimens.SpacingMd),
                    verticalArrangement = Arrangement.spacedBy(GoTouchGrassDimens.SpacingSm)
                ) {
                    Text(
                        text = "Capture",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = unknownPlaceError ?: "Unknown location.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Button(
                        onClick = onClose,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Back to Map")
                    }
                }
            }
        }
        return
    }

    var capturedComplete by remember { mutableStateOf(false) }
    val captureInteractionSource = remember { MutableInteractionSource() }
    val isHolding by captureInteractionSource.collectIsPressedAsState()
    val holdProgress by animateFloatAsState(
        targetValue = if (isHolding && !capturedComplete) 1f else 0f,
        animationSpec = tween(durationMillis = 1500),
        label = "captureHoldProgress"
    )

    LaunchedEffect(holdProgress, capturedComplete) {
        if (!capturedComplete && holdProgress >= 0.99f) {
            capturedComplete = true
            onCaptured(placeId)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(GoTouchGrassDimens.SpacingMd),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(GoTouchGrassDimens.SpacingMd),
                verticalArrangement = Arrangement.spacedBy(GoTouchGrassDimens.SpacingSm)
            ) {
                Text(
                    text = "Capture",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = state.title,
                    style = MaterialTheme.typography.titleMedium
                )

                Icon(
                    painter = painterResource(id = state.imageRes),
                    contentDescription = state.title,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(72.dp)
                )

                Text("Zone: ${state.zoneName}", style = MaterialTheme.typography.bodyMedium)
                Text("Rarity: ${state.rarityLabel}", style = MaterialTheme.typography.bodyMedium)
                Text("XP Reward: ${state.xpReward}", style = MaterialTheme.typography.bodyMedium)
                Text(state.description, style = MaterialTheme.typography.bodySmall)

                LinearProgressIndicator(
                    progress = { holdProgress },
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = when {
                        capturedComplete -> "Captured! Returning to map..."
                        isHolding -> "Keep holding to capture..."
                        else -> "Press and hold to capture"
                    },
                    style = MaterialTheme.typography.bodySmall
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(GoTouchGrassDimens.SpacingSm)
                ) {
                    Button(
                        onClick = {},
                        interactionSource = captureInteractionSource,
                        enabled = !capturedComplete,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (capturedComplete) "Captured" else "Hold To Capture")
                    }

                    Button(
                        onClick = onClose,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Back to Map")
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun CaptureScreenPreview() {
    GoTouchGrassTheme {
        CaptureScreen(placeId = "lm_dc_silent_study")
    }
}