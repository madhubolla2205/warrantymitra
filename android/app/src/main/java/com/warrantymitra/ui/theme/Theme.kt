package com.warrantymitra.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

private val Light = lightColorScheme(
    primary = Color(0xFF1F6F63), onPrimary = Color.White,
    primaryContainer = Color(0xFFDCEFE9), onPrimaryContainer = Color(0xFF15302A),
    secondary = Color(0xFFD98A0B), onSecondary = Color.White,
    secondaryContainer = Color(0xFFFBECD0), onSecondaryContainer = Color(0xFF5A3600),
    background = Color(0xFFF2F4EF), onBackground = Color(0xFF15302A),
    surface = Color(0xFFFFFFFF), onSurface = Color(0xFF15302A),
    surfaceVariant = Color(0xFFE7EBE3), onSurfaceVariant = Color(0xFF5B6D66),
    outline = Color(0xFFCFD8D0), error = Color(0xFFB3372B),
)

private val Dark = darkColorScheme(
    primary = Color(0xFF5CC2B0), onPrimary = Color(0xFF0F1916),
    primaryContainer = Color(0xFF183A34), onPrimaryContainer = Color(0xFFE4EEE9),
    secondary = Color(0xFFF0A830), onSecondary = Color(0xFF0F1916),
    secondaryContainer = Color(0xFF3A2B10), onSecondaryContainer = Color(0xFFFFE2AD),
    background = Color(0xFF0F1916), onBackground = Color(0xFFE4EEE9),
    surface = Color(0xFF16231F), onSurface = Color(0xFFE4EEE9),
    surfaceVariant = Color(0xFF1D2D28), onSurfaceVariant = Color(0xFF9DB0A8),
    outline = Color(0xFF2C3F39), error = Color(0xFFF08A7E),
)

/** Good / warning / critical colours, kept apart from the brand accent. */
@Immutable
data class StatusColors(
    val ok: Color, val okBg: Color,
    val warn: Color, val warnBg: Color,
    val bad: Color, val badBg: Color,
)

private val LightStatus = StatusColors(Color(0xFF1F7A4D), Color(0xFFDCF1E5), Color(0xFFA86200), Color(0xFFFDEBC8), Color(0xFFB3372B), Color(0xFFF8DEDB))
private val DarkStatus = StatusColors(Color(0xFF5FD194), Color(0xFF173326), Color(0xFFF2B955), Color(0xFF3A2C12), Color(0xFFF08A7E), Color(0xFF3D1C19))

val LocalStatusColors = staticCompositionLocalOf { LightStatus }

@Composable
fun WarrantyMitraTheme(dark: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    androidx.compose.runtime.CompositionLocalProvider(LocalStatusColors provides if (dark) DarkStatus else LightStatus) {
        MaterialTheme(colorScheme = if (dark) Dark else Light, content = content)
    }
}
