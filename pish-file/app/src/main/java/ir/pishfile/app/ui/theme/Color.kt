package ir.pishfile.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * پالت رنگ «پیش‌فایل» — سبز ساختمانی عمیق + طلایی ملایم.
 * رنگ‌ها به‌گونه‌ای انتخاب شده‌اند که متن فارسی روی آن‌ها خوانا باشد.
 */

// روشن
val EmeraldDeep = Color(0xFF0E6F5C)
val EmeraldLight = Color(0xFF16A085)
val EmeraldContainer = Color(0xFFB8E9DA)
val GoldAccent = Color(0xFFB87E1E)
val GoldContainer = Color(0xFFF5E3C1)
val SlateBlue = Color(0xFF2E5E8C)
val SlateBlueContainer = Color(0xFFD6E4F0)

val BackgroundLight = Color(0xFFF6F8F6)
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceVariantLight = Color(0xFFE7ECE9)
val OnSurfaceLight = Color(0xFF1A2420)
val OnSurfaceVariantLight = Color(0xFF4A5852)
val OutlineLight = Color(0xFFC7CFCA)
val ErrorLight = Color(0xFFB3261E)
val ErrorContainerLight = Color(0xFFF9DEDC)

// تاریک
val EmeraldDark = Color(0xFF6FD3B4)
val EmeraldContainerDark = Color(0xFF0A4A3D)
val GoldDark = Color(0xFFE7C173)
val SlateBlueDark = Color(0xFFA8C8E8)
val BackgroundDark = Color(0xFF0F1512)
val SurfaceDark = Color(0xFF16201C)
val SurfaceVariantDark = Color(0xFF27332E)
val OnSurfaceDark = Color(0xFFE2E8E4)
val OnSurfaceVariantDark = Color(0xFFB6C2BC)
val OutlineDark = Color(0xFF4C5A54)
val ErrorDark = Color(0xFFF2B8B5)
val ErrorContainerDark = Color(0xFF8C1D18)

/** رنگ‌های وضعیت‌ها — در همه‌ی صفحه‌ها یکسان استفاده می‌شوند */
object StatusColors {
    val available = Color(0xFF2E7D32)       // آزاد — سبز
    val reserved = Color(0xFFE08A00)        // رزرو — نارنجی
    val sold = Color(0xFF1565C0)            // پیش‌فروش — آبی
    val delivered = Color(0xFF00695C)       // تحویل — سبزآبی تیره
    val draft = Color(0xFF6B7280)           // پیش‌نویس — خاکستری
    val pending = Color(0xFFE08A00)         // در انتظار — نارنجی
    val confirmed = Color(0xFF2E7D32)       // قطعی — سبز
    val canceled = Color(0xFFC62828)        // لغو — قرمز
    val overdue = Color(0xFFC62828)         // معوق — قرمز
    val paid = Color(0xFF2E7D32)            // پرداخت‌شده — سبز
    val partial = Color(0xFF8E24AA)         // پرداخت جزئی — بنفش
}
