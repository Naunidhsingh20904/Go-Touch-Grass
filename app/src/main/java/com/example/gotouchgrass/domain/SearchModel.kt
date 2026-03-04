package com.example.gotouchgrass.domain

enum class Rarity {
    COMMON,
    UNCOMMON,
    RARE,
    EPIC,
    LEGENDARY
}

data class SearchLocation(
    val id: String,
    val title: String,
    val description: String,
    val rarity: Rarity
)

class SearchModel(
    private val currentUserId: String
) {
    private val zonesById = FakeData.zones.associateBy { it.id }
    private val recentSearches = mutableListOf("DC Library", "Laurier University", "Lazeez")

    fun getRecentSearches(limit: Int = 5): List<String> {
        val safeLimit = limit.coerceAtLeast(0)
        return recentSearches.take(safeLimit)
    }

    fun recordRecentSearch(query: String) {
        val normalized = query.trim()
        if (normalized.isBlank()) return

        recentSearches.removeAll { it.equals(normalized, ignoreCase = true) }
        recentSearches.add(0, normalized)

        val maxRecent = 8
        if (recentSearches.size > maxRecent) {
            recentSearches.subList(maxRecent, recentSearches.size).clear()
        }
    }

    private fun getZoneLocations(limit: Int = 5): List<SearchLocation> {
        val safeLimit = limit.coerceAtLeast(0)

        return FakeData.zones
            .map { zone ->
                SearchLocation(
                    id = zone.id,
                    title = zone.name,
                    description = zone.type.name.lowercase().replaceFirstChar { it.uppercase() },
                    rarity = rarityForZone(zone.id)
                )
            }
            .take(safeLimit)
    }

    fun getSuggestedLocations(limit: Int = 5): List<SearchLocation> {
        val safeLimit = limit.coerceAtLeast(0)

        val visitedZoneIds = FakeData.visitSessions
            .filter { it.userId == currentUserId }
            .sortedByDescending { it.durationSec }
            .map { it.zoneId }
            .distinct()

        val visitedSuggestions = visitedZoneIds.mapNotNull { zoneId ->
            zonesById[zoneId]?.let { zone ->
                SearchLocation(
                    id = zone.id,
                    title = zone.name,
                    description = "Based on your visits",
                    rarity = rarityForZone(zone.id)
                )
            }
        }

        if (visitedSuggestions.size >= safeLimit) {
            return visitedSuggestions.take(safeLimit)
        }

        val additionalZones = getZoneLocations(limit = safeLimit)
            .filterNot { zone -> visitedSuggestions.any { it.id == zone.id } }

        return (visitedSuggestions + additionalZones).take(safeLimit)
    }

    private fun rarityForZone(zoneId: String): Rarity {
        val captures = FakeData.captures.filter { it.zoneId == zoneId }
        if (captures.isNotEmpty()) {
            val avgRarityScore = captures.map { it.rarityAtTime.value }.average()
            return rarityFromScore(avgRarityScore)
        }

        val fallbackZoneType = zonesById[zoneId]?.type
        return when (fallbackZoneType) {
            ZoneType.BUILDING -> Rarity.COMMON
            ZoneType.CAMPUS -> Rarity.UNCOMMON
            ZoneType.PARK -> Rarity.RARE
            ZoneType.NEIGHBORHOOD -> Rarity.UNCOMMON
            null -> Rarity.COMMON
        }
    }

    private fun rarityFromScore(score: Double): Rarity {
        return when {
            score >= 0.90 -> Rarity.LEGENDARY
            score >= 0.75 -> Rarity.EPIC
            score >= 0.60 -> Rarity.RARE
            score >= 0.45 -> Rarity.UNCOMMON
            else -> Rarity.COMMON
        }
    }

}
