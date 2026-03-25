package com.example.gotouchgrass.ui.map.capture

import android.graphics.Bitmap
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gotouchgrass.data.GoTouchGrassRepository
import com.example.gotouchgrass.data.SoundFeedback
import com.example.gotouchgrass.ui.theme.GoTouchGrassDimens
import com.example.gotouchgrass.ui.theme.GoTouchGrassTheme
import kotlinx.coroutines.launch

@Composable
fun CaptureScreen(
    placeId: String,
    placeName: String? = null,
    categoryName: String? = null,
    placePhotoBitmap: Bitmap? = null,
    repository: GoTouchGrassRepository? = null,
    currentUserId: String? = null,
    onClose: () -> Unit = {},
    onCaptured: (String) -> Unit = {},
    viewModel: CaptureViewModel = remember(placeId, placeName, categoryName) {
        CaptureViewModel(
            placeId = placeId, seededPlaceName = placeName, seededCategoryName = categoryName
        )
    }
) {
    val context = LocalContext.current
    val state = viewModel.uiState
    val scope = rememberCoroutineScope()

    var capturedComplete by remember { mutableStateOf(false) }
    var isSavingCapture by remember { mutableStateOf(false) }
    var saveCaptureError by remember { mutableStateOf<String?>(null) }
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
            saveCaptureError = null
            scope.launch {
                isSavingCapture = true
                val result = if (repository != null && !currentUserId.isNullOrBlank()) {
                    repository.recordCaptureByPlaceId(currentUserId, placeId)
                } else {
                    Result.success(Unit)
                }

                if (result.isSuccess) {
                    onCaptured(placeId)
                } else {
                    capturedComplete = false
                    saveCaptureError =
                        result.exceptionOrNull()?.message ?: "Failed to save capture."
                }
                isSavingCapture = false
            }
        }
    }

    LaunchedEffect(capturedComplete) {
        if (capturedComplete) {
            SoundFeedback.playCaptureSuccess(context)
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
                    text = "Capture", style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = state.title, style = MaterialTheme.typography.titleMedium
                )

                if (placePhotoBitmap != null) {
                    Image(
                        bitmap = placePhotoBitmap.asImageBitmap(),
                        contentDescription = state.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(MaterialTheme.shapes.medium)
                    )
                } else {
                    Icon(
                        painter = painterResource(id = state.imageRes),
                        contentDescription = state.title,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .size(72.dp)
                    )
                }

                Text("Rarity: ${state.rarityLabel}", style = MaterialTheme.typography.bodyMedium)
                Text("XP Reward: ${state.xpReward}", style = MaterialTheme.typography.bodyMedium)

                LinearProgressIndicator(
                    progress = { holdProgress },
                    modifier = Modifier.fillMaxWidth(),
                    drawStopIndicator = { })

                Text(
                    text = when {
                        isSavingCapture -> "Saving capture..."
                        capturedComplete -> "Captured! Returning to map..."
                        isHolding -> "Keep holding to capture..."
                        else -> "Press and hold to capture"
                    }, style = MaterialTheme.typography.bodySmall
                )

                if (!saveCaptureError.isNullOrBlank()) {
                    Text(
                        text = saveCaptureError ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(GoTouchGrassDimens.SpacingSm)
                ) {
                    Button(
                        onClick = {},
                        interactionSource = captureInteractionSource,
                        enabled = !capturedComplete && !isSavingCapture,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (capturedComplete) "Captured" else "Hold To Capture")
                    }

                    Button(
                        onClick = onClose, modifier = Modifier.weight(1f)
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