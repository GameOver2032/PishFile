package ir.pishfile.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

// رنگ‌های تکمیلی طرح روشن
private val OnPrimaryContainerLight = Color(0xFF0A3D31)
private val OnSecondaryContainerLight = Color(0xFF4A3208)
private val OnTertiaryContainerLight = Color(0xFF16324B)
private val OutlineVariantLight = Color(0xFFDCE3DF)
private val OnErrorContainerLight = Color(0xFF410E0B)
private val SurfaceContainerLowestLight = Color(0xFFFFFFFF)
private val SurfaceContainerLowLight = Color(0xFFF1F4F2)
private val SurfaceContainerLight = Color(0xFFEDF1EF)
private val SurfaceContainerHighLight = Color(0xFFE7ECE9)
private val SurfaceContainerHighestLight = Color(0xFFE1E7E3)

// رنگ‌های تکمیلی طرح تاریک
private val OnPrimaryDark = Color(0xFF00382C)
private val OnSecondaryDark = Color(0xFF3E2C00)
private val SecondaryContainerDark = Color(0xFF5C4400)
private val OnTertiaryDark = Color(0xFF11334F)
private val TertiaryContainerDark = Color(0xFF1E3F5C)
private val OutlineVariantDark = Color(0xFF3A4640)
private val OnErrorDark = Color(0xFF601410)
private val SurfaceContainerLowestDark = Color(0xFF0B100D)
private val SurfaceContainerLowDark = Color(0xFF131B17)
private val SurfaceContainerDark = Color(0xFF17211D)
private val SurfaceContainerHighDark = Color(0xFF1E2A25)
private val SurfaceContainerHighestDark = Color(0xFF25332D)

private val LightColors = lightColorScheme(
    primary = EmeraldDeep,
    onPrimary = SurfaceLight,
    primaryContainer = EmeraldContainer,
    onPrimaryContainer = OnPrimaryContainerLight,
    secondary = GoldAccent,
    onSecondary = SurfaceLight,
    secondaryContainer = GoldContainer,
    onSecondaryContainer = OnSecondaryContainerLight,
    tertiary = SlateBlue,
    onTertiary = SurfaceLight,
    tertiaryContainer = SlateBlueContainer,
    onTertiaryContainer = OnTertiaryContainerLight,
    background = BackgroundLight,
    onBackground = OnSurfaceLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight,
    error = ErrorLight,
    onError = SurfaceLight,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight,
    surfaceContainerLowest = SurfaceContainerLowestLight,
    surfaceContainerLow = SurfaceContainerLowLight,
    surfaceContainer = SurfaceContainerLight,
    surfaceContainerHigh = SurfaceContainerHighLight,
    surfaceContainerHighest = SurfaceContainerHighestLight,
)

private val DarkColors = darkColorScheme(
    primary = EmeraldDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = EmeraldContainerDark,
    onPrimaryContainer = EmeraldContainer,
    secondary = GoldDark,
    onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryContainerDark,
    onSecondaryContainer = GoldContainer,
    tertiary = SlateBlueDark,
    onTertiary = OnTertiaryDark,
    tertiaryContainer = TertiaryContainerDark,
    onTertiaryContainer = SlateBlueContainer,
    background = BackgroundDark,
    onBackground = OnSurfaceDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark,
    error = ErrorDark,
    onError = OnErrorDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = ErrorContainerLight,
    surfaceContainerLowest = SurfaceContainerLowestDark,
    surfaceContainerLow = SurfaceContainerLowDark,
    surfaceContainer = SurfaceContainerDark,
    surfaceContainerHigh = SurfaceContainerHighDark,
    surfaceContainerHighest = SurfaceContainerHighestDark,
)

/**
 * تم برنامه. حالت روشن/تاریک از تنظیمات کاربر خوانده می‌شود و
 * کل رابط کاربری راست‌چین (RTL) است، مستقل از زبان دستگاه.
 */
@Composable
fun PishFileTheme(
    themeMode: String = "system",
    content: @Composable () -> Unit,
) {
    val darkTheme = when (themeMode) {
        "dark" -> true
        "light" -> false
        else -> isSystemInDarkTheme()
    }

    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = PishFileTypography,
    ) {
        CompositionLocalProvider(
            LocalLayoutDirection provides LayoutDirection.Rtl,
            content = content
        )
    }
}
