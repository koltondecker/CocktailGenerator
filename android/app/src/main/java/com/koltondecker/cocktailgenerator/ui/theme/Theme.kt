package com.koltondecker.cocktailgenerator.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val DarkColors = darkColorScheme(
    primary = Amber,
    onPrimary = Color(0xFF241300),
    primaryContainer = AmberDeep,
    onPrimaryContainer = Color(0xFFFFE3BC),
    secondary = Coral,
    onSecondary = Color(0xFF2B0710),
    secondaryContainer = CoralDeep,
    onSecondaryContainer = Color(0xFFFFD9DF),
    tertiary = Aqua,
    onTertiary = Color(0xFF00201B),
    tertiaryContainer = AquaDeep,
    onTertiaryContainer = Color(0xFFC8F5EC),
    background = InkBackground,
    onBackground = Cream,
    surface = InkSurface,
    onSurface = Cream,
    surfaceVariant = InkSurfaceHigh,
    onSurfaceVariant = CreamMuted,
    outline = InkOutline,
    error = Color(0xFFFF8A80),
    errorContainer = Color(0xFF5C1A1A),
    onErrorContainer = Color(0xFFFFDAD6),
)

private val LightColors = lightColorScheme(
    primary = AmberDeep,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE3BC),
    onPrimaryContainer = Color(0xFF3D2500),
    secondary = CoralDeep,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFD9DF),
    onSecondaryContainer = Color(0xFF400A18),
    tertiary = AquaDeep,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFC8F5EC),
    onTertiaryContainer = Color(0xFF002C25),
    background = DayBackground,
    onBackground = DayInk,
    surface = DaySurface,
    onSurface = DayInk,
    surfaceVariant = DaySurfaceHigh,
    onSurfaceVariant = DayInkMuted,
    outline = Color(0xFFB9AECB),
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp),
)

@Composable
fun CocktailGeneratorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    // Brand palette always — dynamic color is intentionally off so the
    // lounge look survives on every device.
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}
