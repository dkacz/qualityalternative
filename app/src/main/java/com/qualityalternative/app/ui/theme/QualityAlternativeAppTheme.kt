package com.qualityalternative.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qualityalternative.app.domain.model.AppThemeMode

@Immutable
data class QualityAlternativeColors(
    val background: Color,
    val elevatedSurface: Color,
    val primaryText: Color,
    val mutedText: Color,
    val faintText: Color,
    val line: Color,
    val accent: Color,
    val accentSoft: Color,
    val success: Color,
    val successSoft: Color,
)

private val LightColors = QualityAlternativeColors(
    background = Color(0xFFF6F0E6),
    elevatedSurface = Color(0xFFFFFAF0),
    primaryText = Color(0xFF28231C),
    mutedText = Color(0xFF6F6558),
    faintText = Color(0xFF9B8F7D),
    line = Color(0xFFE1D5C2),
    accent = Color(0xFF8B4A2F),
    accentSoft = Color(0xFFEED7C4),
    success = Color(0xFF527A54),
    successSoft = Color(0xFFDDE9D6),
)

private val DarkColors = QualityAlternativeColors(
    background = Color(0xFF151514),
    elevatedSurface = Color(0xFF211F1B),
    primaryText = Color(0xFFEFE9DD),
    mutedText = Color(0xFFB7AA98),
    faintText = Color(0xFF7E7467),
    line = Color(0xFF3A352E),
    accent = Color(0xFFD89B72),
    accentSoft = Color(0xFF3A2A22),
    success = Color(0xFF9DBB8B),
    successSoft = Color(0xFF253323),
)

private val LocalQualityAlternativeColors = staticCompositionLocalOf { LightColors }

object QualityAlternativeThemeTokens {
    val colors: QualityAlternativeColors
        @Composable
        @ReadOnlyComposable
        get() = LocalQualityAlternativeColors.current
}

@Composable
fun QualityAlternativeAppTheme(
    themeMode: AppThemeMode,
    content: @Composable () -> Unit,
) {
    val tokens = when (themeMode) {
        AppThemeMode.LIGHT -> LightColors
        AppThemeMode.DARK -> DarkColors
    }
    val colorScheme = when (themeMode) {
        AppThemeMode.LIGHT -> lightColorScheme(
            primary = tokens.accent,
            onPrimary = Color.White,
            primaryContainer = tokens.accentSoft,
            onPrimaryContainer = tokens.primaryText,
            secondary = tokens.success,
            onSecondary = Color.White,
            secondaryContainer = tokens.successSoft,
            onSecondaryContainer = tokens.primaryText,
            background = tokens.background,
            onBackground = tokens.primaryText,
            surface = tokens.elevatedSurface,
            onSurface = tokens.primaryText,
            surfaceVariant = tokens.elevatedSurface,
            onSurfaceVariant = tokens.mutedText,
            outline = tokens.line,
            error = Color(0xFF9B3528),
        )

        AppThemeMode.DARK -> darkColorScheme(
            primary = tokens.accent,
            onPrimary = Color(0xFF21140E),
            primaryContainer = tokens.accentSoft,
            onPrimaryContainer = tokens.primaryText,
            secondary = tokens.success,
            onSecondary = Color(0xFF10170E),
            secondaryContainer = tokens.successSoft,
            onSecondaryContainer = tokens.primaryText,
            background = tokens.background,
            onBackground = tokens.primaryText,
            surface = tokens.elevatedSurface,
            onSurface = tokens.primaryText,
            surfaceVariant = tokens.elevatedSurface,
            onSurfaceVariant = tokens.mutedText,
            outline = tokens.line,
            error = Color(0xFFFFB4A9),
        )
    }

    CompositionLocalProvider(LocalQualityAlternativeColors provides tokens) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = QualityTypography,
            shapes = QualityShapes,
            content = content,
        )
    }
}

private val QualityTypography = Typography(
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 30.sp,
        lineHeight = 36.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 17.sp,
        lineHeight = 25.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 18.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
)

private val QualityShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(32.dp),
)
