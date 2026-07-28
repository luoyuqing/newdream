package app.newdream.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import app.newdream.R

// Font families from Foreverse
private val DisplayFont = FontFamily(
    Font(R.font.geist_variable, FontWeight.Normal)
)

private val BodyFont = FontFamily(
    Font(R.font.noto_serif_sc, FontWeight.Normal),
    Font(R.font.noto_serif_sc_black, FontWeight.Black)
)

private val EnglishFont = FontFamily(
    Font(R.font.inter_variable, FontWeight.Normal)
)

private val MonoFont = FontFamily(
    Font(R.font.geist_mono_variable, FontWeight.Normal)
)

// Foreverse-inspired typography
private val AppTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = DisplayFont,
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily = DisplayFont,
        fontWeight = FontWeight.Normal,
        fontSize = 45.sp,
        lineHeight = 52.sp
    ),
    displaySmall = TextStyle(
        fontFamily = DisplayFont,
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
        lineHeight = 44.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = DisplayFont,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        lineHeight = 40.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = DisplayFont,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 36.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = DisplayFont,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 32.sp
    ),
    titleLarge = TextStyle(
        fontFamily = BodyFont,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontFamily = BodyFont,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = BodyFont,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = BodyFont,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = BodyFont,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = BodyFont,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    labelLarge = TextStyle(
        fontFamily = EnglishFont,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = EnglishFont,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = EnglishFont,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFC97A4D),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF5E6DC),
    onPrimaryContainer = Color(0xFF2A1F1A),
    secondary = Color(0xFF3DA876),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD4F0E4),
    onSecondaryContainer = Color(0xFF003322),
    tertiary = Color(0xFFA89684),
    background = Color(0xFFF8F2E8),
    onBackground = Color(0xFF2A1F1A),
    surface = Color.White,
    onSurface = Color(0xFF2A1F1A),
    surfaceVariant = Color(0xFFF0E9DA),
    onSurfaceVariant = Color(0xFF6B5D52),
    outline = Color(0xFFE8DFCF),
    outlineVariant = Color(0xFFD4C9B8),
    error = Color(0xFFC94D4D),
    onError = Color.White,
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFE8B190),
    onPrimary = Color(0xFF1A1612),
    primaryContainer = Color(0xFF4A3528),
    onPrimaryContainer = Color(0xFFF5E6DC),
    secondary = Color(0xFF3DA876),
    onSecondary = Color(0xFF003322),
    secondaryContainer = Color(0xFF005038),
    onSecondaryContainer = Color(0xFFD4F0E4),
    tertiary = Color(0xFFC4B4A4),
    background = Color(0xFF1A1612),
    onBackground = Color(0xFFE8DFCF),
    surface = Color(0xFF2A241E),
    onSurface = Color(0xFFE8DFCF),
    surfaceVariant = Color(0xFF3A322A),
    onSurfaceVariant = Color(0xFFB4A89C),
    outline = Color(0xFF4A423A),
    outlineVariant = Color(0xFF3A322A),
    error = Color(0xFFE07070),
    onError = Color(0xFF1A1612),
)

@Composable
fun NewDreamTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
