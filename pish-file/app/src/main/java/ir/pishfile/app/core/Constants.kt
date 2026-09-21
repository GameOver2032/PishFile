package ir.pishfile.app.core

/**
 * ثابت‌ها و فهرست‌های مشترک برنامه (وضعیت‌ها، انواع، برچسب‌های فارسی).
 */
object Constants {

    // ---------- مدل‌های قیمت‌گذاری پروژه ----------
    const val PRICING_METER = "METER"           // قیمت هر متر مربع
    const val PRICING_DEPOSIT_BONUS = "DEPOSIT_BONUS" // واریزی و امتیاز
    const val PRICING_SHARE = "SHARE"           // سهامی

    val projectPricingModels = listOf(PRICING_METER, PRICING_DEPOSIT_BONUS, PRICING_SHARE)

    fun projectPricingModelLabel(model: String?): String = when (model) {
        PRICING_METER -> "قیمت متری"
        PRICING_DEPOSIT_BONUS -> "واریزی و امتیاز"
        PRICING_SHARE -> "سهامی"
        else -> "قیمت متری"
    }

    // ---------- وضعیت پیش‌فروش (فایل) ----------
    const val PREFILE_DRAFT = "DRAFT"               // پیش‌نویس
    const val PREFILE_URGENT = "URGENT"             // فروش فوری
    const val PREFILE_NORMAL = "NORMAL"             // غیر فوری
    const val PREFILE_WITHDRAWN = "WITHDRAWN"       // منصرف از فروش

    val preFileStatuses = listOf(
        PREFILE_DRAFT, PREFILE_URGENT, PREFILE_NORMAL, PREFILE_WITHDRAWN
    )

    fun preFileStatusLabel(status: String?): String = when (status) {
        PREFILE_DRAFT -> "پیش‌نویس"
        PREFILE_URGENT -> "فروش فوری"
        PREFILE_NORMAL -> "غیر فوری"
        PREFILE_WITHDRAWN -> "منصرف از فروش"
        else -> "پیش‌نویس"
    }

    // ---------- وضعیت واحد (آپارتمان) ----------
    const val UNIT_AVAILABLE = "AVAILABLE"     // آزاد
    const val UNIT_RESERVED = "RESERVED"       // رزرو شده
    const val UNIT_SOLD = "SOLD"               // فروخته شده
    const val UNIT_DELIVERED = "DELIVERED"     // تحویل شده

    val unitStatuses = listOf(UNIT_AVAILABLE, UNIT_RESERVED, UNIT_SOLD, UNIT_DELIVERED)

    fun unitStatusLabel(status: String?): String = when (status) {
        UNIT_AVAILABLE -> "آزاد"
        UNIT_RESERVED -> "رزرو شده"
        UNIT_SOLD -> "فروخته شده"
        UNIT_DELIVERED -> "تحویل شده"
        else -> "نامشخص"
    }

    // ---------- وضعیت پروژه ----------
    const val PROJECT_PLANNING = "PLANNING"     // در حال طراحی/مجوز
    const val PROJECT_EXCAVATION = "EXCAVATION" // گودبرداری
    const val PROJECT_STRUCTURE = "STRUCTURE"   // اسکلت
    const val PROJECT_FINISHING = "FINISHING"   // نازک‌کاری
    const val PROJECT_DELIVERED = "DELIVERED"   // تحویل

    val projectPhases = listOf(
        PROJECT_PLANNING, PROJECT_EXCAVATION, PROJECT_STRUCTURE,
        PROJECT_FINISHING, PROJECT_DELIVERED
    )

    fun projectPhaseLabel(phase: String?): String = when (phase) {
        PROJECT_PLANNING -> "طراحی و مجوز"
        PROJECT_EXCAVATION -> "گودبرداری"
        PROJECT_STRUCTURE -> "اسکلت"
        PROJECT_FINISHING -> "نازک‌کاری"
        PROJECT_DELIVERED -> "تحویل شده"
        else -> "نامشخص"
    }

    // ---------- نوع واحد ----------
    const val UNIT_TYPE_APARTMENT = "APARTMENT"
    const val UNIT_TYPE_SHOP = "SHOP"
    const val UNIT_TYPE_OFFICE = "OFFICE"
    const val UNIT_TYPE_PARKING = "PARKING"
    const val UNIT_TYPE_STORAGE = "STORAGE"
    const val UNIT_TYPE_VILLA = "VILLA"
    const val UNIT_TYPE_LAND = "LAND"

    val unitTypes = listOf(
        UNIT_TYPE_APARTMENT, UNIT_TYPE_SHOP, UNIT_TYPE_OFFICE, UNIT_TYPE_PARKING,
        UNIT_TYPE_STORAGE, UNIT_TYPE_VILLA, UNIT_TYPE_LAND
    )

    fun unitTypeLabel(type: String?): String = when (type) {
        UNIT_TYPE_APARTMENT -> "آپارتمان"
        UNIT_TYPE_SHOP -> "مغازه"
        UNIT_TYPE_OFFICE -> "اداری"
        UNIT_TYPE_PARKING -> "پارکینگ"
        UNIT_TYPE_STORAGE -> "انباری"
        UNIT_TYPE_VILLA -> "ویلا"
        UNIT_TYPE_LAND -> "زمین"
        else -> "نامشخص"
    }

    // ---------- جهت واحد ----------
    val unitDirections = listOf("شمالی", "جنوبی", "شرقی", "غربی", "شمالی-جنوبی", "شرقی-غربی")

    // ---------- وضعیت پیگیری ----------
    const val FOLLOWUP_PENDING = "PENDING"
    const val FOLLOWUP_DONE = "DONE"
    const val FOLLOWUP_CANCELED = "CANCELED"

    fun followUpStatusLabel(status: String?): String = when (status) {
        FOLLOWUP_PENDING -> "در انتظار"
        FOLLOWUP_DONE -> "انجام شده"
        FOLLOWUP_CANCELED -> "لغو شده"
        else -> "نامشخص"
    }

    // ---------- واحدهای اندازه‌گیری ----------
    const val CURRENCY_TOMAN = "تومان"

    val facilities = listOf(
        "آسانسور", "پارکینگ", "انباری", "استخر", "سالن ورزشی", "لابی",
        "نگهبانی ۲۴ ساعته", "سیستم گرمایش از کف", "دوربین مداربسته", "روف‌گاردن",
        "آنتن مرکزی", "شوتینگ زباله", "تابلو برق", "گاز شهری"
    )
}
