package com.example.gotouchgrass.ui.map.capture

import com.example.gotouchgrass.R
import androidx.lifecycle.ViewModel
import com.example.gotouchgrass.domain.FakeData
import com.example.gotouchgrass.domain.LandmarkCategory

data class CaptureUiState(
    val title: String,
    val zoneName: String,
    val rarityLabel: String,
    val xpReward: Int,
    val ownerName: String,
    val description: String,
    val imageRes: Int
)

private fun rarityLabelFromScore(score: Double): String {
    return when {
        score >= 0.9 -> "Legendary"
        score >= 0.75 -> "Epic"
        score >= 0.55 -> "Rare"
        score >= 0.35 -> "Uncommon"
        else -> "Common"
    }
}

private fun imageForCategory(category: LandmarkCategory): Int {
    return when (category) {
        LandmarkCategory.CAFE -> R.drawable.coffee_24
        LandmarkCategory.PARK -> R.drawable.nature_24
        LandmarkCategory.STUDY_SPOT -> R.drawable.book_2_24dp
        LandmarkCategory.LOUNGE -> R.drawable.chair_24dp
        LandmarkCategory.MURAL -> R.drawable.image_inset_24dp
        LandmarkCategory.STATUE -> R.drawable.architecture_24dp
        LandmarkCategory.OTHER -> R.drawable.map_placeholder
    }
}

class CaptureViewModel(
    private val placeId: String
) : ViewModel() {

    val uiState: CaptureUiState? = run {
        val landmark = FakeData.landmarks.firstOrNull { it.id == placeId } ?: return@run null

        val zone = FakeData.zones.firstOrNull { it.id == landmark.zoneId }
        val existingCapture = FakeData.captures.firstOrNull { it.landmarkId == landmark.id }
        val owner = FakeData.zoneOwnership
            .firstOrNull { it.zoneId == landmark.zoneId }
            ?.ownerUserId
            ?.let { ownerId -> FakeData.users.firstOrNull { it.id == ownerId }?.displayName }
            ?: "Unclaimed"

        val rarityScore = existingCapture?.rarityAtTime?.value
            ?: when (landmark.category) {
                com.example.gotouchgrass.domain.LandmarkCategory.PARK -> 0.10
                com.example.gotouchgrass.domain.LandmarkCategory.STUDY_SPOT -> 0.91
                com.example.gotouchgrass.domain.LandmarkCategory.CAFE -> 0.35
                com.example.gotouchgrass.domain.LandmarkCategory.LOUNGE -> 0.52
                com.example.gotouchgrass.domain.LandmarkCategory.MURAL -> 0.92
                com.example.gotouchgrass.domain.LandmarkCategory.STATUE -> 0.83
                com.example.gotouchgrass.domain.LandmarkCategory.OTHER -> 0.20
            }

        CaptureUiState(
            title = landmark.name,
            zoneName = zone?.name ?: "Unknown zone",
            rarityLabel = rarityLabelFromScore(rarityScore),
            xpReward = existingCapture?.xpAwarded ?: 120,
            ownerName = owner,
            description = landmark.description ?: "No description available.",
            imageRes = imageForCategory(landmark.category)
        )
    }

    val unknownPlaceError: String? = if (uiState == null) {
        "Unknown location. This place is not in the current dataset."
    } else {
        null
    }
}