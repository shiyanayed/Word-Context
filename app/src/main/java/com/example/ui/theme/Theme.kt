package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class AppThemeMode(val displayName: String) {
    MIDNIGHT("Midnight Sanctum"),
    SCHOLAR_DESK("Scholar's Desk")
}

private val MidnightColorScheme =
  darkColorScheme(
    primary = SacredGold,
    secondary = SlateGrey,
    tertiary = AncientGold,
    background = DarkNavyBg,
    surface = DarkNavySurface,
    onPrimary = DarkNavyBg,
    onSecondary = PaleParchment,
    onTertiary = DarkNavyBg,
    onBackground = PaleParchment,
    onSurface = PaleParchment,
    surfaceVariant = Color(0xFF161B27)
  )

private val ScholarDeskColorScheme =
  lightColorScheme(
    primary = ScholarDeskPrimary,
    secondary = ScholarDeskTextSecondary,
    tertiary = ScholarDeskGold,
    background = ScholarDeskBg,
    surface = ScholarDeskSurface,
    onPrimary = Color.White,
    onSecondary = ScholarDeskTextPrimary,
    onTertiary = Color.White,
    onBackground = ScholarDeskTextPrimary,
    onSurface = ScholarDeskTextPrimary,
    surfaceVariant = ScholarDeskSurfaceVariant
  )

@Composable
fun MyApplicationTheme(
  themeMode: AppThemeMode = AppThemeMode.MIDNIGHT,
  content: @Composable () -> Unit,
) {
  val colorScheme = when (themeMode) {
      AppThemeMode.MIDNIGHT -> MidnightColorScheme
      AppThemeMode.SCHOLAR_DESK -> ScholarDeskColorScheme
  }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
