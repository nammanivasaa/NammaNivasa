package com.example.nammanivasa.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Central spacing / dimension tokens for Namma Nivasa.
 *
 * A single 4.dp-based scale keeps vertical rhythm and gutters consistent across
 * screens. Screens and shared components should reach for these instead of
 * scattering ad-hoc [dp] literals, so padding stays aligned as the app grows.
 */
object Spacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 20.dp
    val xxl = 24.dp
    val xxxl = 32.dp

    /** Standard horizontal screen gutter used by full-width content. */
    val screenHorizontal = 24.dp

    /** Default inner padding for cards. */
    val cardPadding = 16.dp

    /** Minimum interactive touch target (accessibility). */
    val touchTarget = 48.dp

    /** Fixed height of the primary/secondary auth action buttons. */
    val buttonHeight = 56.dp
}
