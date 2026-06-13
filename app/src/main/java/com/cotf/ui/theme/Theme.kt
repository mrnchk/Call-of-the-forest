package com.cotf.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ForestColorScheme = darkColorScheme(
    primary = ForestGreen,
    onPrimary = Parchment,
    primaryContainer = ForestGreenDark,
    onPrimaryContainer = ForestGreenLight,
    secondary = EarthyBrown,
    onSecondary = Parchment,
    secondaryContainer = EarthyBrownLight,
    tertiary = OliveGreen,
    onTertiary = Parchment,
    background = DarkSurface,
    onBackground = Parchment,
    surface = DarkSurface,
    onSurface = Parchment,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = ParchmentDim,
    error = Color(0xFFCF6679),
    onError = Parchment,
)

@Composable
fun CallOfTheForestTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ForestColorScheme,
        typography = ForestTypography,
        content = content
    )
}
