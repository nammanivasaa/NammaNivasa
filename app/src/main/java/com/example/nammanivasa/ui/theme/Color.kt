package com.example.nammanivasa.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// ============================================================
// Namma Nivasa — Burgundy brand design system
// ============================================================
// Central brand tokens (locked palette):
//   Primary burgundy  #800020   Deep burgundy  #57001B
//   Berry accent      #A32952   Soft rose      #F5E4EA
//   Pale rose bg      #FBF1F4   Main bg        #FAF8F7
//   Surface           #FFFFFF   Primary text   #251D21
//   Secondary text    #786D72   Border         #E9DFE2

// --- Brand Colors (Light Theme) ---
val Primary = Color(0xFF800020)          // Primary burgundy
val PrimaryDark = Color(0xFF57001B)      // Deep burgundy
val PrimaryContainer = Color(0xFFF5E4EA) // Soft rose
val SecondaryBackground = Color(0xFFFBF1F4) // Pale rose
val BerryAccent = Color(0xFFA32952)      // Berry accent
val AppBackground = Color(0xFFFAF8F5)    // Main warm off-white background (#FAF8F5)
val Surface = Color(0xFFFFFFFF)          // White surface
val PrimaryText = Color(0xFF251D21)      // Primary text
val SecondaryText = Color(0xFF786D72)    // Secondary text
val Border = Color(0xFFE9DFE2)           // Hairline border
val Success = Color(0xFF16845B)
val Warning = Color(0xFFB86E00)
val Error = Color(0xFFC62828)

// --- Status indicator accents (used on the Home hero) ---
// Unread-notification dot and the profile "active presence" dot. Kept as named
// tokens so these two status colours live with the rest of the palette rather
// than as inline literals in the UI.
val NotificationDot = Color(0xFFFF4D4F)
val PresenceActive = Color(0xFF34C759)

// --- Brand gradient (used sparingly for hero / premium actions) ---
// Keep this aligned with the core burgundy palette for consistent branding.
val BrandGradientStart = Primary             // Primary burgundy
val BrandGradientEnd = PrimaryDark           // Deep burgundy

// --- Brand Colors (Dark Theme) ---
val DarkPrimary = Color(0xFFF2A9BC)           // Soft rose (readable on dark)
val DarkPrimaryDark = Color(0xFFF5E4EA)       // Soft rose
val DarkPrimaryContainer = Color(0xFF5C1226)  // Deep-mid burgundy container
val DarkSecondaryBackground = Color(0xFF241C20) // Warm dark surface
val DarkBerryAccent = Color(0xFFE89AAE)       // Muted berry-rose accent
val DarkAppBackground = Color(0xFF181215)     // Warm near-black background
val DarkSurface = Color(0xFF241C20)           // Warm dark surface
val DarkPrimaryText = Color(0xFFF7EFF1)       // Warm off-white text
val DarkSuccess = Color(0xFF69DB7C)
val DarkWarning = Color(0xFFFFB74D)
val DarkError = Color(0xFFEF5350)

// --- Semantic color aliases (light) ---
val SurfaceContainer = Color(0xFFF5EFF1)      // Warm, slightly deeper than bg
val SurfaceContainerLow = Color(0xFFFBF7F8)
val SurfaceContainerHigh = Color(0xFFEFE7EA)
val SurfaceVariant = Color(0xFFF2EBEE)        // Soft warm rose-gray
val Outline = Color(0xFF786D72)               // Secondary text tone
val OutlineVariant = Color(0xFFE9DFE2)        // Border
val ErrorContainer = Color(0xFFFFDAD6)

// --- Elevation shadows ---
val ElevationLevel1 = 1.dp
val ElevationLevel2 = 3.dp
val ElevationLevel3 = 6.dp
val ElevationLevel4 = 8.dp
val ElevationLevel5 = 12.dp

// --- Shape tokens ---
val ShapeExtraSmall = 4.dp
val ShapeSmall = 8.dp
val ShapeMedium = 12.dp
val ShapeLarge = 16.dp
val ShapeExtraLarge = 24.dp
val ShapeFull = 999.dp