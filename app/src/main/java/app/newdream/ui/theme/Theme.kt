package app.newdream.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6C63FF),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE8E5FF),
    onPrimaryContainer = Color(0xFF1A0D5E),
    secondary = Color(0xFF03DAC6),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCEF8F0),
    onSecondaryContainer = Color(0xFF00201C),
    tertiary = Color(0xFFFF6B9D),
    background = Color(0xFFF8F9FA),
    onBackground = Color(0xFF1A1A2E),
    surface = Color.White,
    onSurface = Color(0xFF1A1A2E),
    surfaceVariant = Color(0xFFF0F0F5),
    onSurfaceVariant = Color(0xFF6B7280),
    outline = Color(0xFFE5E7EB),
    error = Color(0xFFEF4444),
    onError = Color.White,
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF8B83FF),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF2A1D7E),
    onPrimaryContainer = Color(0xFFE8E5FF),
    secondary = Color(0xFF03DAC6),
    onSecondary = Color(0xFF00332E),
    secondaryContainer = Color(0xFF005048),
    onSecondaryContainer = Color(0xFFCEF8F0),
    tertiary = Color(0xFFFF8DAF),
    background = Color(0xFF121212),
    onBackground = Color(0xFFE8E8F0),
    surface = Color(0xFF1E1E2E),
    onSurface = Color(0xFFE8E8F0),
    surfaceVariant = Color(0xFF2A2A3E),
    onSurfaceVariant = Color(0xFFA0A0B0),
    outline = Color(0xFF3A3A4E),
    error = Color(0xFFEF4444),
    onError = Color.White,
)

@Composable
fun NewDreamTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
