package ir.pishfile.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp
import ir.pishfile.app.R

/**
 * تایپوگرافی فارسی — فونت وزیرمتن (Vazirmatn) با مجوز OFL.
 * ارتفاع خط کمی بیشتر گرفته شده چون حروف فارسی دنباله‌دار (ج، ی، ...) دارند.
 */
val VazirmatnFamily = FontFamily(
    fonts = listOf(
        Font(R.font.vazirmatn_light, FontWeight.Light),
        Font(R.font.vazirmatn_regular, FontWeight.Normal),
        Font(R.font.vazirmatn_medium, FontWeight.Medium),
        Font(R.font.vazirmatn_bold, FontWeight.Bold),
    )
)

private val lineHeightStyle = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.None,
)

private fun style(
    size: Int,
    lineHeight: Int,
    weight: FontWeight,
    letterSpacing: Double = 0.0,
) = TextStyle(
    fontFamily = VazirmatnFamily,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = letterSpacing.sp,
    lineHeightStyle = lineHeightStyle,
)

val PishFileTypography = Typography(
    displaySmall = style(34, 48, FontWeight.Bold),
    headlineLarge = style(30, 42, FontWeight.Bold),
    headlineMedium = style(26, 38, FontWeight.Bold),
    headlineSmall = style(23, 34, FontWeight.Medium),
    titleLarge = style(20, 30, FontWeight.Medium),
    titleMedium = style(17, 26, FontWeight.Medium),
    titleSmall = style(15, 24, FontWeight.Medium),
    bodyLarge = style(16, 28, FontWeight.Normal),
    bodyMedium = style(14.5f.toInt(), 26, FontWeight.Normal),
    bodySmall = style(13, 22, FontWeight.Normal),
    labelLarge = style(15, 22, FontWeight.Medium),
    labelMedium = style(13, 20, FontWeight.Medium),
    labelSmall = style(11.5f.toInt(), 18, FontWeight.Medium),
)
