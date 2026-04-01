package com.example.gotouchgrass.domain

/**
 * Centralized in-memory fake data set that models
 * the Go Touch Grass domain around the
 * University of Waterloo and nearby areas.
 *
 * This is meant for prototyping screens and flows
 * before a real backend exists.
 */
object FakeData {

    // --- Core world setup: Waterloo + UW area ---

    val cities: List<City> = listOf(
        City(
            id = "city_waterloo",
            name = "Waterloo",
            country = "Canada",
            timezone = "America/Toronto",
            // Rough bounding box around UW + Uptown + Waterloo Park
            boundingBox = listOf(
                LatLng(43.5100, -80.5650),
                LatLng(43.5100, -80.5000),
                LatLng(43.4500, -80.5000),
                LatLng(43.4500, -80.5650)
            )
        )
    )

    val zones: List<Zone> = listOf(
        Zone(
            id = "zone_uw_main_campus",
            cityId = "city_waterloo",
            name = "UW Main Campus",
            type = ZoneType.CAMPUS,
            boundary = listOf(
                LatLng(43.4745, -80.5520),
                LatLng(43.4745, -80.5260),
                LatLng(43.4630, -80.5260),
                LatLng(43.4630, -80.5520)
            ),
            centerLatLng = LatLng(43.4689, -80.5410)
        ),
        Zone(
            id = "zone_dc_library",
            cityId = "city_waterloo",
            name = "Davis Centre Library",
            type = ZoneType.BUILDING,
            boundary = listOf(
                LatLng(43.4726, -80.5425),
                LatLng(43.4726, -80.5417),
                LatLng(43.4721, -80.5417),
                LatLng(43.4721, -80.5425)
            ),
            centerLatLng = LatLng(43.4723, -80.5421)
        ),
        Zone(
            id = "zone_slc",
            cityId = "city_waterloo",
            name = "Student Life Centre",
            type = ZoneType.BUILDING,
            boundary = listOf(
                LatLng(43.4710, -80.5452),
                LatLng(43.4710, -80.5444),
                LatLng(43.4704, -80.5444),
                LatLng(43.4704, -80.5452)
            ),
            centerLatLng = LatLng(43.4707, -80.5448)
        ),
        Zone(
            id = "zone_cif_fields",
            cityId = "city_waterloo",
            name = "CIF Fields",
            type = ZoneType.PARK,
            boundary = listOf(
                LatLng(43.4725, -80.5515),
                LatLng(43.4725, -80.5485),
                LatLng(43.4695, -80.5485),
                LatLng(43.4695, -80.5515)
            ),
            centerLatLng = LatLng(43.4710, -80.5500)
        ),
        Zone(
            id = "zone_waterloo_park",
            cityId = "city_waterloo",
            name = "Waterloo Park",
            type = ZoneType.PARK,
            boundary = listOf(
                LatLng(43.4715, -80.5315),
                LatLng(43.4715, -80.5245),
                LatLng(43.4660, -80.5245),
                LatLng(43.4660, -80.5315)
            ),
            centerLatLng = LatLng(43.4687, -80.5280)
        ),
        Zone(
            id = "zone_uptown_waterloo",
            cityId = "city_waterloo",
            name = "Uptown Waterloo",
            type = ZoneType.NEIGHBORHOOD,
            boundary = listOf(
                LatLng(43.4765, -80.5285),
                LatLng(43.4765, -80.5210),
                LatLng(43.4720, -80.5210),
                LatLng(43.4720, -80.5285)
            ),
            centerLatLng = LatLng(43.4743, -80.5245)
        ),
        Zone(
            id = "zone_conestoga_mall",
            cityId = "city_waterloo",
            name = "Conestoga Mall",
            type = ZoneType.NEIGHBORHOOD,
            boundary = listOf(
                LatLng(43.4985, -80.5235),
                LatLng(43.4985, -80.5170),
                LatLng(43.4940, -80.5170),
                LatLng(43.4940, -80.5235)
            ),
            centerLatLng = LatLng(43.4964, -80.5202)
        )
    )

    val landmarks: List<Landmark> = listOf(
        Landmark(
            id = "lm_dc_silent_study",
            zoneId = "zone_dc_library",
            name = "DC Silent Study Tiles",
            category = LandmarkCategory.STUDY_SPOT,
            latLng = LatLng(43.4724, -80.5421),
            radiusMeters = 20.0,
            description = "Iconic yellow tiles and rows of study desks in Davis Centre.",
            photoRef = null,
            createdByUserId = "user_you",
            isVerified = true
        ),
        Landmark(
            id = "lm_slc_foosball",
            zoneId = "zone_slc",
            name = "SLC Games Corner",
            category = LandmarkCategory.LOUNGE,
            latLng = LatLng(43.4707, -80.5448),
            radiusMeters = 18.0,
            description = "Foosball tables and couches where group projects never end.",
            photoRef = null,
            createdByUserId = null,
            isVerified = true
        ),
        Landmark(
            id = "lm_uw_egg_fountain",
            zoneId = "zone_uw_main_campus",
            name = "University of Waterloo Egg Fountain",
            category = LandmarkCategory.STATUE,
            latLng = LatLng(43.4720, -80.5433),
            radiusMeters = 22.0,
            description = "The iconic Egg Fountain landmark at the University of Waterloo campus core.",
            photoRef = null,
            createdByUserId = null,
            isVerified = true
        ),
        Landmark(
            id = "lm_cif_soccer_fields",
            zoneId = "zone_cif_fields",
            name = "CIF Soccer Fields",
            category = LandmarkCategory.PARK,
            latLng = LatLng(43.4710, -80.5500),
            radiusMeters = 40.0,
            description = "Grass fields for intramurals, pick‑up games, and sunset walks.",
            photoRef = null,
            createdByUserId = null,
            isVerified = true
        ),
        Landmark(
            id = "lm_waterloo_park_boathouse",
            zoneId = "zone_waterloo_park",
            name = "Waterloo Park Boathouse",
            category = LandmarkCategory.PARK,
            latLng = LatLng(43.4690, -80.5275),
            radiusMeters = 35.0,
            description = "Boardwalk views over the water and geese supervision duty.",
            photoRef = null,
            createdByUserId = "user_world_explorer",
            isVerified = true
        ),
        Landmark(
            id = "lm_uptown_public_square",
            zoneId = "zone_uptown_waterloo",
            name = "Uptown Public Square",
            category = LandmarkCategory.LOUNGE,
            latLng = LatLng(43.4744, -80.5246),
            radiusMeters = 25.0,
            description = "Festivals, live music, and late‑night bubble tea rendezvous.",
            photoRef = null,
            createdByUserId = null,
            isVerified = true
        ),
        Landmark(
            id = "lm_conestoga_food_court",
            zoneId = "zone_conestoga_mall",
            name = "Conestoga Food Court",
            category = LandmarkCategory.CAFE,
            latLng = LatLng(43.4965, -80.5200),
            radiusMeters = 30.0,
            description = "Post‑midterm shawarma and mall laps with friends.",
            photoRef = null,
            createdByUserId = "user_you",
            isVerified = false
        )
    )

    // --- Users & settings ---

    val users: List<User> = listOf(
        User(
            id = "user_you",
            displayName = "You",
            username = "uw_grasswalker",
            email = "you@uwaterloo.ca",
            avatarUrl = null,
            createdAtIso = "2025-09-01T10:00:00Z",
            homeCityId = "city_waterloo",
            level = 8,
            xpTotal = 12450
        ),
        User(
            id = "user_world_explorer",
            displayName = "WorldExplorer",
            username = "world_explorer",
            email = "world@example.com",
            avatarUrl = null,
            createdAtIso = "2024-05-10T14:30:00Z",
            homeCityId = "city_waterloo",
            level = 42,
            xpTotal = 125000
        )
    )

    val userSettings: List<UserSettings> = listOf(
        UserSettings(
            userId = "user_you",
            locationTrackingEnabled = true,
            shareApproxLocation = true,
            mapVisibilityMode = MapVisibilityMode.FRIENDS_ONLY,
            pushEnabled = true,
            units = UnitsPreference.METRIC,
            theme = ThemePreference.SYSTEM
        )
    )

    // --- Location + sessions ---

    val visitSessions: List<VisitSession> = listOf(
        VisitSession(
            id = "vs_morning_dc",
            userId = "user_you",
            zoneId = "zone_dc_library",
            startedAtIso = "2025-10-01T14:00:00Z",
            endedAtIso = "2025-10-01T17:00:00Z",
            durationSec = 3 * 60 * 60,
            confidenceScore = 0.95,
            source = VisitSessionSource.AUTO,
            isStudySession = true
        ),
        VisitSession(
            id = "vs_lunch_slc",
            userId = "user_you",
            zoneId = "zone_slc",
            startedAtIso = "2025-10-01T17:15:00Z",
            endedAtIso = "2025-10-01T18:00:00Z",
            durationSec = 45 * 60,
            confidenceScore = 0.90,
            source = VisitSessionSource.AUTO,
            isStudySession = false
        ),
        VisitSession(
            id = "vs_evening_walk_park",
            userId = "user_you",
            zoneId = "zone_waterloo_park",
            startedAtIso = "2025-10-02T22:00:00Z",
            endedAtIso = "2025-10-02T23:00:00Z",
            durationSec = 60 * 60,
            confidenceScore = 0.87,
            source = VisitSessionSource.MANUAL_ADJUSTED,
            isStudySession = false
        )
    )

    val locationPings: List<LocationPing> = listOf(
        LocationPing(
            id = "ping_dc_1",
            userId = "user_you",
            timestampIso = "2025-10-01T14:05:00Z",
            approxLatLng = LatLng(43.4723, -80.5421),
            accuracyMeters = 8.0,
            speedMetersPerSecond = 0.1,
            source = LocationSource.GPS,
            hashCell = GeoCell("dpz83dc")
        ),
        LocationPing(
            id = "ping_park_1",
            userId = "user_you",
            timestampIso = "2025-10-02T22:15:00Z",
            approxLatLng = LatLng(43.4688, -80.5279),
            accuracyMeters = 12.0,
            speedMetersPerSecond = 1.4,
            source = LocationSource.GPS,
            hashCell = GeoCell("dpz83wp")
        )
    )

    val zoneEntryEvents: List<ZoneEntryEvent> = listOf(
        ZoneEntryEvent(
            id = "zee_dc_enter",
            userId = "user_you",
            zoneId = "zone_dc_library",
            type = ZoneEntryEventType.ENTER,
            timestampIso = "2025-10-01T14:00:00Z"
        ),
        ZoneEntryEvent(
            id = "zee_dc_exit",
            userId = "user_you",
            zoneId = "zone_dc_library",
            type = ZoneEntryEventType.EXIT,
            timestampIso = "2025-10-01T17:00:00Z"
        ),
        ZoneEntryEvent(
            id = "zee_park_enter",
            userId = "user_you",
            zoneId = "zone_waterloo_park",
            type = ZoneEntryEventType.ENTER,
            timestampIso = "2025-10-02T22:00:00Z"
        ),
        ZoneEntryEvent(
            id = "zee_park_exit",
            userId = "user_you",
            zoneId = "zone_waterloo_park",
            type = ZoneEntryEventType.EXIT,
            timestampIso = "2025-10-02T23:00:00Z"
        )
    )

    // --- Captures, ownership, XP ---

    val captures: List<Capture> = listOf(
        Capture(
            id = "cap_dc_study",
            userId = "user_you",
            zoneId = "zone_dc_library",
            landmarkId = "lm_dc_silent_study",
            capturedAtIso = "2025-10-01T14:10:00Z",
            proofType = CaptureProofType.GEOFENCE,
            rarityAtTime = RarityScore(0.70),
            xpAwarded = 150
        ),
        Capture(
            id = "cap_waterloo_park",
            userId = "user_you",
            zoneId = "zone_waterloo_park",
            landmarkId = "lm_waterloo_park_boathouse",
            capturedAtIso = "2025-10-02T22:20:00Z",
            proofType = CaptureProofType.GPS,
            rarityAtTime = RarityScore(0.85),
            xpAwarded = 200
        ),
        Capture(
            id = "cap_conestoga_food",
            userId = "user_you",
            zoneId = "zone_conestoga_mall",
            landmarkId = "lm_conestoga_food_court",
            capturedAtIso = "2025-10-03T19:10:00Z",
            proofType = CaptureProofType.QR,
            rarityAtTime = RarityScore(0.40),
            xpAwarded = 75
        )
    )

    val zoneOwnership: List<ZoneOwnership> = listOf(
        ZoneOwnership(
            zoneId = "zone_dc_library",
            ownerUserId = "user_you",
            ownedSinceIso = "2025-10-01T14:10:00Z",
            ownershipScore = 0.82,
            lastContestedAtIso = null
        ),
        ZoneOwnership(
            zoneId = "zone_waterloo_park",
            ownerUserId = "user_world_explorer",
            ownedSinceIso = "2025-09-20T18:00:00Z",
            ownershipScore = 0.91,
            lastContestedAtIso = "2025-09-28T12:00:00Z"
        )
    )

    val zoneOwnershipHistory: List<ZoneOwnershipHistory> = listOf(
        ZoneOwnershipHistory(
            id = "zoh_dc_claim",
            zoneId = "zone_dc_library",
            previousOwnerId = null,
            newOwnerId = "user_you",
            changedAtIso = "2025-10-01T14:10:00Z",
            reason = OwnershipChangeReason.CAPTURED
        )
    )

    val xpEvents: List<XPEvent> = listOf(
        XPEvent(
            id = "xp_dc_study_time",
            userId = "user_you",
            type = XPEventType.VISIT_TIME,
            amount = 120,
            createdAtIso = "2025-10-01T17:00:00Z",
            refId = "vs_morning_dc"
        ),
        XPEvent(
            id = "xp_dc_capture",
            userId = "user_you",
            type = XPEventType.CAPTURE,
            amount = 150,
            createdAtIso = "2025-10-01T14:10:00Z",
            refId = "cap_dc_study"
        ),
        XPEvent(
            id = "xp_park_capture",
            userId = "user_you",
            type = XPEventType.CAPTURE,
            amount = 200,
            createdAtIso = "2025-10-02T22:20:00Z",
            refId = "cap_waterloo_park"
        ),
        XPEvent(
            id = "xp_daily_bonus",
            userId = "user_you",
            type = XPEventType.BONUS,
            amount = 50,
            createdAtIso = "2025-10-02T23:05:00Z",
            refId = null
        )
    )

    // --- Badges, milestones, streaks, completion ---

    val badges: List<Badge> = listOf(
        Badge(
            id = "badge_first_dc_capture",
            name = "DC Pioneer",
            description = "Capture any landmark in Davis Centre.",
            icon = "ic_badge_dc",
            ruleType = BadgeRuleType.SIMPLE_THRESHOLD,
            ruleConfigJson = """{"type":"CAPTURE_IN_ZONE","zoneId":"zone_dc_library","threshold":1}"""
        ),
        Badge(
            id = "badge_ring_road_runner",
            name = "Ring Road Runner",
            description = "Walk the full ring road in a week.",
            icon = "ic_badge_ring_road",
            ruleType = BadgeRuleType.EXPLORATION,
            ruleConfigJson = """{"type":"DISTANCE_IN_ZONE","zoneId":"zone_uw_main_campus","km":5.0}"""
        ),
        Badge(
            id = "badge_park_reset",
            name = "Park Reset",
            description = "Spend 5 hours in Waterloo Park.",
            icon = "ic_badge_park",
            ruleType = BadgeRuleType.STREAK,
            ruleConfigJson = """{"type":"TIME_IN_ZONE","zoneId":"zone_waterloo_park","minutes":300}"""
        )
    )

    val userBadges: List<UserBadge> = listOf(
        UserBadge(
            userId = "user_you",
            badgeId = "badge_first_dc_capture",
            earnedAtIso = "2025-10-01T14:10:00Z"
        )
    )

    val milestoneProgress: List<MilestoneProgress> = listOf(
        MilestoneProgress(
            userId = "user_you",
            milestoneId = "milestone_unique_uw_zones_10",
            progressValue = 4.0,
            lastUpdatedAtIso = "2025-10-02T23:00:00Z"
        )
    )

    val streaks: List<Streak> = listOf(
        Streak(
            userId = "user_you",
            type = StreakType.DAILY_EXPLORE,
            currentCount = 7,
            bestCount = 14,
            lastCountedDateIso = "2025-10-02"
        ),
        Streak(
            userId = "user_you",
            type = StreakType.WEEKLY_CAPTURE,
            currentCount = 3,
            bestCount = 5,
            lastCountedDateIso = "2025-10-01"
        )
    )

    val cityCompletion: List<CityCompletion> = listOf(
        CityCompletion(
            userId = "user_you",
            cityId = "city_waterloo",
            zonesVisitedCount = 5,
            zonesTotal = 12,
            completionPct = 41.7
        )
    )

    // --- Challenges & routes ---

    val challenges: List<Challenge> = listOf(
        Challenge(
            id = "challenge_daily_new_uw_zone",
            title = "Discover a New UW Spot",
            description = "Visit a building or field you haven't captured yet on campus.",
            challengeType = ChallengeType.VISIT,
            timeWindow = ChallengeTimeWindow.DAILY,
            ruleConfigJson = """{"uniqueZones":1,"cityId":"city_waterloo"}""",
            rewardXP = 100
        ),
        Challenge(
            id = "challenge_weekly_ring_road",
            title = "Ring Road Explorer",
            description = "Walk at least 3 km along Ring Road this week.",
            challengeType = ChallengeType.EXPLORE,
            timeWindow = ChallengeTimeWindow.WEEKLY,
            ruleConfigJson = """{"distanceKm":3.0,"zoneId":"zone_uw_main_campus"}""",
            rewardXP = 350
        ),
        Challenge(
            id = "challenge_weekly_park_reset",
            title = "Park Reset",
            description = "Spend 2 hours recharging in Waterloo Park.",
            challengeType = ChallengeType.TIME,
            timeWindow = ChallengeTimeWindow.WEEKLY,
            ruleConfigJson = """{"timeMinutes":120,"zoneId":"zone_waterloo_park"}""",
            rewardXP = 250
        )
    )

    val challengeProgress: List<ChallengeProgress> = listOf(
        ChallengeProgress(
            userId = "user_you",
            challengeId = "challenge_daily_new_uw_zone",
            periodStartIso = "2025-10-02T00:00:00Z",
            progressValue = 0.0,
            completedAtIso = null
        ),
        ChallengeProgress(
            userId = "user_you",
            challengeId = "challenge_weekly_ring_road",
            periodStartIso = "2025-09-29T00:00:00Z",
            progressValue = 1.8,
            completedAtIso = null
        ),
        ChallengeProgress(
            userId = "user_you",
            challengeId = "challenge_weekly_park_reset",
            periodStartIso = "2025-09-29T00:00:00Z",
            progressValue = 1.0,
            completedAtIso = null
        )
    )

    val routes: List<Route> = listOf(
        Route(
            id = "route_uw_coffee_trail",
            cityId = "city_waterloo",
            title = "UW Coffee Trail",
            theme = RouteTheme.FOOD,
            difficulty = RouteDifficulty.EASY,
            estimatedDurationMinutes = 90,
            createdByUserId = "user_you",
            isOfficial = false
        ),
        Route(
            id = "route_uw_sunset_walk",
            cityId = "city_waterloo",
            title = "Sunset Walk to Waterloo Park",
            theme = RouteTheme.PARKS,
            difficulty = RouteDifficulty.MEDIUM,
            estimatedDurationMinutes = 120,
            createdByUserId = null,
            isOfficial = true
        )
    )

    val routeStops: List<RouteStop> = listOf(
        RouteStop(
            id = "rs_coffee_dc",
            routeId = "route_uw_coffee_trail",
            orderIndex = 1,
            zoneId = "zone_dc_library",
            landmarkId = "lm_dc_silent_study",
            hintText = "Start with a focus sprint in DC, then grab coffee nearby."
        ),
        RouteStop(
            id = "rs_coffee_uptown",
            routeId = "route_uw_coffee_trail",
            orderIndex = 2,
            zoneId = "zone_uptown_waterloo",
            landmarkId = "lm_uptown_public_square",
            hintText = "Walk the LRT path to Uptown and reward yourself."
        ),
        RouteStop(
            id = "rs_sunset_slc",
            routeId = "route_uw_sunset_walk",
            orderIndex = 1,
            zoneId = "zone_slc",
            landmarkId = "lm_slc_foosball",
            hintText = "Meet friends at SLC before heading out."
        ),
        RouteStop(
            id = "rs_sunset_park",
            routeId = "route_uw_sunset_walk",
            orderIndex = 2,
            zoneId = "zone_waterloo_park",
            landmarkId = "lm_waterloo_park_boathouse",
            hintText = "Catch golden hour by the water."
        )
    )

    val userRouteProgress: List<UserRouteProgress> = listOf(
        UserRouteProgress(
            userId = "user_you",
            routeId = "route_uw_coffee_trail",
            startedAtIso = "2025-10-02T18:00:00Z",
            completedAtIso = null,
            stopsCompleted = 1
        )
    )

    // --- Social & leaderboards ---

    val friendships: List<Friendship> = listOf(
        Friendship(
            id = "fr_uw_lab_partner",
            requesterId = "user_you",
            addresseeId = "user_world_explorer",
            status = FriendshipStatus.ACCEPTED,
            createdAtIso = "2025-09-15T11:00:00Z"
        )
    )

    val leaderboards: List<Leaderboard> = listOf(
        Leaderboard(
            id = "lb_uw_weekly_xp",
            scope = LeaderboardScope.CAMPUS,
            metric = LeaderboardMetric.XP_WEEK,
            period = LeaderboardPeriod.WEEKLY
        ),
        Leaderboard(
            id = "lb_waterloo_park_visits",
            scope = LeaderboardScope.CITY,
            metric = LeaderboardMetric.ZONES_CAPTURED,
            period = LeaderboardPeriod.WEEKLY
        )
    )

    val leaderboardEntries: List<LeaderboardEntry> = listOf(
        LeaderboardEntry(
            leaderboardId = "lb_uw_weekly_xp",
            userId = "user_world_explorer",
            rank = 1,
            value = 125000.0,
            computedAtIso = "2025-10-02T23:00:00Z"
        ),
        LeaderboardEntry(
            leaderboardId = "lb_uw_weekly_xp",
            userId = "user_you",
            rank = 142,
            value = 12450.0,
            computedAtIso = "2025-10-02T23:00:00Z"
        )
    )

    val zoneActivity: List<ZoneActivity> = listOf(
        ZoneActivity(
            id = "za_dc_capture",
            zoneId = "zone_dc_library",
            type = ZoneActivityType.CAPTURE,
            userId = "user_you",
            createdAtIso = "2025-10-01T14:10:00Z",
            summaryText = "You captured DC Silent Study Tiles"
        ),
        ZoneActivity(
            id = "za_park_walk",
            zoneId = "zone_waterloo_park",
            type = ZoneActivityType.VISIT,
            userId = "user_you",
            createdAtIso = "2025-10-02T22:30:00Z",
            summaryText = "Evening walk logged in Waterloo Park"
        )
    )

    // --- Media ---

    val zonePlaylists: List<ZonePlaylist> = listOf(
        ZonePlaylist(
            id = "zp_dc_focus",
            zoneId = "zone_dc_library",
            provider = PlaylistProvider.SPOTIFY,
            playlistUrl = "https://open.spotify.com/playlist/uw-dc-focus",
            title = "UW DC Focus",
            createdByUserId = "user_you"
        ),
        ZonePlaylist(
            id = "zp_park_chill",
            zoneId = "zone_waterloo_park",
            provider = PlaylistProvider.SPOTIFY,
            playlistUrl = "https://open.spotify.com/playlist/waterloo-park-chill",
            title = "Waterloo Park Chill",
            createdByUserId = "user_world_explorer"
        )
    )

    // --- Summaries & privacy ---

    val explorationSummaries: List<ExplorationSummary> = listOf(
        ExplorationSummary(
            id = "summary_week_uw_40",
            userId = "user_you",
            periodStartIso = "2025-09-29T00:00:00Z",
            periodEndIso = "2025-10-05T23:59:59Z",
            topZonesJson = """[{"zoneId":"zone_dc_library","minutes":180},{"zoneId":"zone_waterloo_park","minutes":60}]""",
            newZonesVisited = 3,
            timeByZoneJson = """{"zone_dc_library":180,"zone_slc":45,"zone_waterloo_park":60}""",
            insightsText = "Most of your week was split between DC grinds and sunset resets in Waterloo Park."
        )
    )

    val approxPresence: List<ApproxPresence> = listOf(
        ApproxPresence(
            userId = "user_you",
            zoneId = "zone_uw_main_campus",
            hashCell = GeoCell("dpz83uw"),
            lastUpdatedAtIso = "2025-10-02T22:45:00Z"
        ),
        ApproxPresence(
            userId = "user_world_explorer",
            zoneId = "zone_waterloo_park",
            hashCell = GeoCell("dpz83wp"),
            lastUpdatedAtIso = "2025-10-02T22:40:00Z"
        )
    )
}
