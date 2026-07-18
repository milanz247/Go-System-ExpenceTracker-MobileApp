package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val MonochromaticColorScheme = darkColorScheme(
  primary = PureWhite,
  onPrimary = PitchBlack,
  secondary = Zinc400,
  onSecondary = PureWhite,
  tertiary = Zinc600,
  onTertiary = PureWhite,
  background = PitchBlack,
  onBackground = PureWhite,
  surface = Zinc900,
  onSurface = PureWhite,
  surfaceVariant = Zinc950,
  onSurfaceVariant = Zinc300,
  outline = Zinc700,
  outlineVariant = Zinc800
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force premium dark mode by default for Apple/Linear matte black aesthetic
  dynamicColor: Boolean = false, // Disable dynamic colors to keep monochromatic brand styling
  content: @Composable () -> Unit,
) {
  val colorScheme = MonochromaticColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
