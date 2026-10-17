package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class ThemeMode {
    DARK,
    AMOLED,
    LIGHT,
    SYSTEM
}

private val StudioDarkColorScheme = darkColorScheme(
    primary = ElectricCyan,
    onPrimary = Color(0xFF00262C),
    primaryContainer = Color(0xFF00404A),
    onPrimaryContainer = Color(0xFF9CF2FF),
    secondary = NeonEmerald,
    onSecondary = Color(0xFF002C1E),
    secondaryContainer = Color(0xFF044D36),
    onSecondaryContainer = Color(0xFFA7F3D0),
    tertiary = AmberWarning,
    onTertiary = Color(0xFF332000),
    tertiaryContainer = Color(0xFF523500),
    onTertiaryContainer = Color(0xFFFDE68A),
    error = CoralAlert,
    background = VaultObsidian,
    onBackground = Color(0xFFF1F5F9),
    surface = VaultSurface,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = VaultSurfaceElevated,
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF334155)
)

private val AmoledDarkColorScheme = StudioDarkColorScheme.copy(
    background = VaultAmoledBlack,
    surface = VaultAmoledSurface,
    surfaceVariant = Color(0xFF131A24)
)

private val StudioLightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F2FE),
    onPrimaryContainer = Color(0xFF075985),
    secondary = LightSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD1FAE5),
    onSecondaryContainer = Color(0xFF065F46),
    tertiary = LightTertiary,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFEF3C7),
    onTertiaryContainer = Color(0xFF92400E),
    error = CoralAlert,
    background = LightBackground,
    onBackground = Color(0xFF0F172A),
    surface = LightSurface,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1)
)

@Composable
fun StreamVaultTheme(
    themeMode: ThemeMode = ThemeMode.DARK,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val colorScheme = when (themeMode) {
        ThemeMode.DARK -> StudioDarkColorScheme
        ThemeMode.AMOLED -> AmoledDarkColorScheme
        ThemeMode.LIGHT -> StudioLightColorScheme
        ThemeMode.SYSTEM -> if (systemDark) StudioDarkColorScheme else StudioLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
