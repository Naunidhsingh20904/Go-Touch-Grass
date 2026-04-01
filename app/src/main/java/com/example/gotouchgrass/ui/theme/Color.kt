package com.example.gotouchgrass.ui.theme

import androidx.compose.ui.graphics.Color

// ===========================================
// GO TOUCH GRASS - COLOR SYSTEM
// ===========================================
// Design Philosophy:
// - Primary: Forest Green - Nature, growth, outdoor exploration
// - Accent: Golden Yellow - Achievement, rewards, gamification (XP)
// - Neutrals: Warm sand/cream tones - Organic, inviting, earth-like
// Target Audience: Students & Young Adults (20-32)

// ============ PRIMARY COLORS ============
// Forest Green - Main brand color, represents nature & outdoors
val ForestGreen = Color(0xFF2D5A3D)           // Primary actions, headers, navigation
val ForestGreenLight = Color(0xFF3D7A52)      // Primary in dark mode (more visible)
val ForestGreenDark = Color(0xFF1E3D29)       // Pressed/active states

// ============ ACCENT COLORS ============
// Golden Yellow - Gamification elements (XP, badges, achievements)
val GoldenYellow = Color(0xFFE8B931)          // XP bars, achievement highlights
val GoldenYellowLight = Color(0xFFF5D060)     // Hover/light accent states
val GoldenYellowDark = Color(0xFFBF9520)      // Pressed accent states

// ============ LIGHT MODE NEUTRALS ============
val WarmCream = Color(0xFFF5F0E8)             // Main background - warm, organic feel
val WarmWhite = Color(0xFFFDFBF7)             // Card/surface background
val SandLight = Color(0xFFEDE8DC)             // Secondary backgrounds
val SandMuted = Color(0xFFE5E0D4)             // Muted backgrounds, disabled states
val SandBorder = Color(0xFFD9D4C8)            // Borders, dividers
val TextPrimary = Color(0xFF1A2F1F)           // Primary text - dark forest green
val TextSecondary = Color(0xFF4A5D4E)         // Secondary/muted text
val TextMuted = Color(0xFF7A8A7D)             // Placeholder text, hints

// ============ DARK MODE NEUTRALS ============
val DarkForest = Color(0xFF1A2F1F)            // Dark mode background
val DarkForestCard = Color(0xFF243529)        // Dark mode card/surface
val DarkForestSecondary = Color(0xFF2E4035)   // Dark mode secondary
val DarkForestMuted = Color(0xFF3A4D40)       // Dark mode muted
val DarkForestBorder = Color(0xFF4A5D4E)      // Dark mode borders
val TextOnDark = Color(0xFFF0EDE6)            // Primary text on dark
val TextOnDarkSecondary = Color(0xFFB8C4BB)   // Secondary text on dark
val TextOnDarkMuted = Color(0xFF8A9A8D)       // Muted text on dark

// ============ SEMANTIC COLORS ============
val Success = Color(0xFF4CAF50)               // Success states, completed zones
val Error = Color(0xFFD32F2F)                 // Error states, destructive actions
val Warning = Color(0xFFF9A825)               // Warning states
val Info = Color(0xFF1976D2)                  // Info states

// ============ ZONE RARITY COLORS ============
// Used for zone cards and badges based on rarity tier
val RarityCommon = Color(0xFF78909C)          // Common zones - Blue Grey
val RarityUncommon = Color(0xFF4CAF50)        // Uncommon zones - Green
val RarityRare = Color(0xFF2196F3)            // Rare zones - Blue
val RarityEpic = Color(0xFF9C27B0)            // Epic zones - Purple
val RarityLegendary = Color(0xFFFFB300)       // Legendary zones - Gold/Amber

// ============ GRADIENT HELPERS ============
// For XP bars and progress indicators
val XpBarStart = ForestGreen
val XpBarEnd = Color(0xFF4CAF50)              // Lighter green for gradient end