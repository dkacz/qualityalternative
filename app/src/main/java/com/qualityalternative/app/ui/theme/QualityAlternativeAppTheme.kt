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
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qualityalternative.app.R
import com.qualityalternative.app.domain.model.AppThemeMode

@Immutable
data class QualityAlternativeColors(
    val background: Color,
    val elevatedSurface: Color,
    val primaryText: Color,
    val mutedText: Color,
    val faintText: Color,
    val line: Color,
    val lineStrong: Color,
    val accent: Color,
    val accentSoft: Color,
    val success: Color,
    val successSoft: Color,
)

private val LightColors = QualityAlternativeColors(
    background = Color(0xFFF2EADE),
    elevatedSurface = Color(0xFFFBF4EA),
    primaryText = Color(0xFF271D17),
    mutedText = Color(0xFF64554B),
    faintText = Color(0xFF9C9086),
    line = Color(0xFFDACFC3),
    lineStrong = Color(0xFFBDAEA1),
    accent = Color(0xFF965630),
    accentSoft = Color(0xFFF4D8C5),
    success = Color(0xFF647A4E),
    successSoft = Color(0xFFDAE2C5),
)

private val DarkColors = QualityAlternativeColors(
    background = Color(0xFF463B33),
    elevatedSurface = Color(0xFF54473F),
    primaryText = Color(0xFFEFE6DB),
    mutedText = Color(0xFFBAAFA4),
    faintText = Color(0xFF8A7E73),
    line = Color(0xFF685B50),
    lineStrong = Color(0xFF807165),
    accent = Color(0xFFE9A679),
    accentSoft = Color(0xFF6C4932),
    success = Color(0xFFA9C192),
    successSoft = Color(0xFF445335),
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

val QualityDisplayFontFamily = FontFamily(
    Font(resId = R.font.newsreader, weight = FontWeight.Normal),
    Font(resId = R.font.newsreader, weight = FontWeight.Medium),
    Font(resId = R.font.newsreader, weight = FontWeight.SemiBold),
    Font(resId = R.font.newsreader_italic, weight = FontWeight.Normal, style = FontStyle.Italic),
)

val QualityBodyFontFamily = FontFamily(
    Font(resId = R.font.work_sans, weight = FontWeight.Normal),
    Font(resId = R.font.work_sans, weight = FontWeight.Medium),
    Font(resId = R.font.work_sans, weight = FontWeight.SemiBold),
    Font(resId = R.font.work_sans, weight = FontWeight.Bold),
)

val QualityMonoFontFamily = FontFamily(
    Font(resId = R.font.jetbrains_mono, weight = FontWeight.Normal),
    Font(resId = R.font.jetbrains_mono, weight = FontWeight.Medium),
)

private val QualityTypography = Typography(
    headlineMedium = TextStyle(
        fontFamily = QualityDisplayFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 30.sp,
        lineHeight = 34.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = QualityDisplayFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        lineHeight = 25.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = QualityDisplayFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 22.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = QualityBodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 17.sp,
        lineHeight = 25.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = QualityBodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = QualityBodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = QualityBodyFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 18.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = QualityBodyFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
)

private val QualityShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(18.dp),
)
