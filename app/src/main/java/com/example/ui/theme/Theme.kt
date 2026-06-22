package com.example.ui.theme

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
  primary = AppleBlueDark,
  onPrimary = Color.White,
  primaryContainer = ApplePrimaryContainerDark,
  onPrimaryContainer = AppleOnPrimaryContainerDark,
  secondary = AppleOnSurfaceVariantDark,
  onSecondary = Color.White,
  secondaryContainer = AppleSurfaceVariantDark,
  onSecondaryContainer = Color.White,
  background = AppleBackgroundDark,
  surface = AppleSurfaceDark,
  onBackground = AppleOnBackgroundDark,
  onSurface = AppleOnSurfaceDark,
  surfaceVariant = AppleSurfaceVariantDark,
  onSurfaceVariant = AppleOnSurfaceVariantDark,
  outline = AppleOutlineDark,
  outlineVariant = AppleSurfaceVariantDark,
  errorContainer = Color(0xFF4A1512),
  onErrorContainer = AppleRed
)

private val LightColorScheme = lightColorScheme(
  primary = AppleBlueLight,
  onPrimary = Color.White,
  primaryContainer = ApplePrimaryContainerLight,
  onPrimaryContainer = AppleOnPrimaryContainerLight,
  secondary = AppleOnSurfaceVariantLight,
  onSecondary = Color.White,
  secondaryContainer = AppleSurfaceVariantLight,
  onSecondaryContainer = AppleOnSurfaceVariantLight,
  background = AppleBackgroundLight,
  surface = AppleSurfaceLight,
  onBackground = AppleOnBackgroundLight,
  onSurface = AppleOnSurfaceLight,
  surfaceVariant = AppleSurfaceVariantLight,
  onSurfaceVariant = AppleOnSurfaceVariantLight,
  outline = AppleOutlineLight,
  outlineVariant = AppleSurfaceVariantLight,
  errorContainer = Color(0xFFFFD6D6),
  onErrorContainer = AppleRed
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
