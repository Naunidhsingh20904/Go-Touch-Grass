package com.example.gotouchgrass.ui.map.capture

import androidx.lifecycle.ViewModel
import com.example.gotouchgrass.domain.LandmarkCategory
import com.example.gotouchgrass.domain.iconResForLandmarkCategory
import com.example.gotouchgrass.domain.rarityScoreForLandmarkCategoryName

data class CaptureUiState(
    val title: String, val rarityLabel: String, val xpReward: Int, val imageRes: Int
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

private fun parseCategoryOrOther(categoryName: String?): LandmarkCategory {
    val normalized = categoryName?.trim().orEmpty()
    if (normalized.isBlank()) return LandmarkCategory.OTHER
    return runCatching {
        LandmarkCategory.valueOf(normalized.uppercase())
    }.getOrDefault(LandmarkCategory.OTHER)
}

class CaptureViewModel(
    private val placeId: String,
    private val seededPlaceName: String? = null,
    private val seededCategoryName: String? = null
) : ViewModel() {

    val uiState: CaptureUiState = run {
        val title = seededPlaceName?.takeIf { it.isNotBlank() } ?: "Selected location"
        val category = parseCategoryOrOther(seededCategoryName)
        val rarityScore = rarityScoreForLandmarkCategoryName(category.name)

        CaptureUiState(
            title = title,
            rarityLabel = rarityLabelFromScore(rarityScore),
            xpReward = 120,
            imageRes = iconResForLandmarkCategory(category)
        )
    }

    val unknownPlaceError: String? = null
}