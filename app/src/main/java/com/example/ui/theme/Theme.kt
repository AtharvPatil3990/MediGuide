package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val MediLightColorScheme = lightColorScheme(
    primary = DeepTeal,
    onPrimary = WarmSurface,
    primaryContainer = SoftMint,
    onPrimaryContainer = DeepTealDark,
    secondary = TerracottaCoral,
    onSecondary = WarmSurface,
    secondaryContainer = SoftCoralContainer,
    onSecondaryContainer = CharcoalInk,
    tertiary = WarmAmber,
    onTertiary = WarmSurface,
    tertiaryContainer = SoftAmberContainer,
    onTertiaryContainer = CharcoalInk,
    background = WarmIvory,
    onBackground = CharcoalInk,
    surface = WarmSurface,
    onSurface = CharcoalInk,
    surfaceVariant = WarmSurfaceVariant,
    onSurfaceVariant = SlateMuted,
    outline = OutlineWarm,
    error = TerracottaCoral,
    errorContainer = SoftCoralContainer
)

private val MediDarkColorScheme = darkColorScheme(
    primary = BrightMint,
    onPrimary = DeepTealDark,
    primaryContainer = DeepTealDark,
    onPrimaryContainer = SoftMint,
    secondary = TerracottaCoral,
    onSecondary = CharcoalInk,
    secondaryContainer = NightTealSurfaceVariant,
    onSecondaryContainer = SoftCoralContainer,
    tertiary = WarmAmber,
    onTertiary = CharcoalInk,
    tertiaryContainer = NightTealSurfaceVariant,
    onTertiaryContainer = SoftAmberContainer,
    background = NightTealBg,
    onBackground = NightTextPrimary,
    surface = NightTealSurface,
    onSurface = NightTextPrimary,
    surfaceVariant = NightTealSurfaceVariant,
    onSurfaceVariant = NightTextMuted,
    outline = NightTealSurfaceVariant
)

val MediShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(30.dp)
)

@Composable
fun MediGuideTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) MediDarkColorScheme else MediLightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = MediTypography,
        shapes = MediShapes,
        content = content
    )
}
