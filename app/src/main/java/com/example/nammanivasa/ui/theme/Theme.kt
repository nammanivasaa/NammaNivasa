package com.example.nammanivasa.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    primaryContainer = DarkPrimaryContainer,
    secondary = DarkBerryAccent,
    secondaryContainer = DarkPrimaryDark,
    tertiary = DarkSuccess,
    background = DarkAppBackground,
    surface = DarkSurface,
    surfaceContainer = DarkSecondaryBackground,
    surfaceContainerLow = DarkSurface,
    surfaceContainerHigh = Color(0xFF2C2429),
    surfaceContainerHighest = Color(0xFF362C31),
    onPrimary = Color(0xFF3A0A16),
    onPrimaryContainer = Color(0xFFF5E4EA),
    onSecondary = Color(0xFF3A0A16),
    onSecondaryContainer = Color(0xFFF5E4EA),
    onTertiary = Color(0xFF3A0A16),
    onBackground = DarkPrimaryText,
    onSurface = DarkPrimaryText,
    onSurfaceVariant = Color(0xFFD8C7CC),
    outline = Color(0xFFA19099),
    outlineVariant = Color(0xFF52454A),
    error = DarkError,
    errorContainer = Color(0xFF93000A),
    onError = Color(0xFFFAF9FF),
    onErrorContainer = Color(0xFFFFDAD6),
    scrim = Color.Black,
    inverseSurface = Color(0xFFF7EFF1),
    inverseOnSurface = Color(0xFF241C20),
    inversePrimary = Primary
)

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    primaryContainer = PrimaryContainer,
    secondary = BerryAccent,
    secondaryContainer = SecondaryBackground,
    tertiary = Success,
    background = AppBackground,
    surface = Surface,
    surfaceContainer = SurfaceContainer,
    surfaceContainerLow = SurfaceContainerLow,
    surfaceContainerHigh = SurfaceContainerHigh,
    surfaceContainerHighest = Color(0xFFEFE7EA),
    onPrimary = Color.White,
    onPrimaryContainer = PrimaryText,
    onSecondary = Color.White,
    onSecondaryContainer = PrimaryText,
    onTertiary = Color.White,
    onBackground = PrimaryText,
    onSurface = PrimaryText,
    onSurfaceVariant = SecondaryText,
    outline = Outline,
    outlineVariant = OutlineVariant,
    error = Error,
    errorContainer = ErrorContainer,
    onError = Color.White,
    onErrorContainer = PrimaryText,
    scrim = Color.Black,
    inverseSurface = DarkSurface,
    inverseOnSurface = DarkPrimaryText,
    inversePrimary = DarkPrimary
)

@Composable
fun NammaNivasaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}