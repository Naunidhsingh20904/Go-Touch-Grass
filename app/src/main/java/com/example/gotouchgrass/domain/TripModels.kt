package com.example.gotouchgrass.domain

data class TripSummary(
    val durationSec: Long,
    val distanceMeters: Float,
    val captureCount: Int,
    val xpEarned: Int,
    val routeName: String? = null,
    val dominantZoneId: Long? = null
) {
    val distanceKm: Float get() = distanceMeters / 1000f

    val formattedDuration: String
        get() {
            val hours = durationSec / 3600
            val minutes = (durationSec % 3600) / 60
            val seconds = durationSec % 60
            return if (hours > 0) "%d:%02d:%02d".format(hours, minutes, seconds)
            else "%d:%02d".format(minutes, seconds)
        }

    val formattedDistance: String
        get() = if (distanceMeters >= 1000) "%.2f km".format(distanceKm)
        else "${distanceMeters.toInt()} m"
}

// A zone with its parsed polygon boundary for dwell-time checking
data class TripZone(
    val id: Long,
    val name: String,
    val polygon: List<LatLng>   // parsed from bounding_box JSON
)

// A resolved route stop that has a real lat/lng for map display
data class RouteStopMapMarker(
    val stopIndex: Int,
    val landmarkId: Long?,
    val placeId: String,
    val placeName: String,
    val latLng: com.google.android.gms.maps.model.LatLng,
    val hintText: String?
)
