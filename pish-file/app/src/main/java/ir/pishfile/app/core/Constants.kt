package ir.pishfile.app.core

/**
 * ثابت‌ها و فهرست‌های مشترک برنامه (وضعیت‌ها، انواع، برچسب‌های فارسی).
 * این فایل نقش «واژه‌نامه‌ی دامنه» را دارد؛ هر جا متن فارسی لازم باشد از این‌جا خوانده می‌شود.
 */
object Constants {

    // ---------- وضعیت پیش‌فایل ----------
    const val PREFILE_DRAFT = "DRAFT"                 // پیش‌نویس
    const val PREFILE_RESERVED = "RESERVED"           // رزرو اولیه
    const val PREFILE_PENDING_PAYMENT = "PENDING_PAYMENT" // در انتظار پرداخت
    const val PREFILE_CONFIRMED = "CONFIRMED"         // قطعی‌شده
    const val PREFILE_CANCELED = "CANCELED"           // لغو شده
    const val PREFILE_COMPLETED = "COMPLETED"         // تسویه/تحویل شده

    val preFileStatuses = listOf(
        PREFILE_DRAFT, PREFILE_RESERVED, PREFILE_PENDING_PAYMENT,
        PREFILE_CONFIRMED, PREFILE_COMPLETED, PREFILE_CANCELED
    )

    fun preFileStatusLabel(status: String?): String = when (status) {
        PREFILE_DRAFT -> "پیش‌نویس"
        PREFILE_RESERVED -> "رزرو اولیه"
        PREFILE_PENDING_PAYMENT -> "در انتظار پرداخت"
        PREFILE_CONFIRMED -> "قطعی‌شده"
        PREFILE_CANCELED -> "لغو شده"
        PREFILE_COMPLETED -> "تحویل/تسویه شده"
        else -> "نامشخص"
    }

    // ---------- وضعیت واحد (آپارتمان) ----------
    const val UNIT_AVAILABLE = "AVAILABLE"     // آزاد
    const val UNIT_RESERVED = "RESERVED"       // رزرو شده
    const val UNIT_SOLD = "SOLD"               // پیش‌فروش شده
    const val UNIT_DELIVERED = "DELIVERED"     // تحویل شده

    val unitStatuses = listOf(UNIT_AVAILABLE, UNIT_RESERVED, UNIT_SOLD, UNIT_DELIVERED)

    fun unitStatusLabel(status: String?): String = when (status) {
        UNIT_AVAILABLE -> "آزاد"
        UNIT_RESERVED -> "رزرو شده"
        UNIT_SOLD -> "پیش‌فروش شده"
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

    // ---------- وضعیت مشتری ----------
    const val CUSTOMER_LEAD = "LEAD"           // سرنخ
    const val CUSTOMER_ACTIVE = "ACTIVE"       // مشتری فعال
    const val CUSTOMER_INSTALLMENT = "INSTALLMENT" // در حال پرداخت اقساط
    const val CUSTOMER_SETTLED = "SETTLED"     // تسویه شده
    const val CUSTOMER_INACTIVE = "INACTIVE"   // غیرفعال

    val customerStatuses = listOf(
        CUSTOMER_LEAD, CUSTOMER_ACTIVE, CUSTOMER_INSTALLMENT, CUSTOMER_SETTLED, CUSTOMER_INACTIVE
    )

    fun customerStatusLabel(status: String?): String = when (status) {
        CUSTOMER_LEAD -> "سرنخ / بازدیدکننده"
        CUSTOMER_ACTIVE -> "مشتری فعال"
        CUSTOMER_INSTALLMENT -> "در حال پرداخت اقساط"
        CUSTOMER_SETTLED -> "تسویه شده"
        CUSTOMER_INACTIVE -> "غیرفعال"
        else -> "نامشخص"
    }

    // ---------- وضعیت قسط ----------
    const val INSTALLMENT_UNPAID = "UNPAID"     // پرداخت‌نشده
    const val INSTALLMENT_PAID = "PAID"         // پرداخت‌شده
    const val INSTALLMENT_OVERDUE = "OVERDUE"   // معوق
    const val INSTALLMENT_PARTIAL = "PARTIAL"   // پرداخت جزئی

    fun installmentStatusLabel(status: String?): String = when (status) {
        INSTALLMENT_UNPAID -> "پرداخت‌نشده"
        INSTALLMENT_PAID -> "پرداخت‌شده"
        INSTALLMENT_OVERDUE -> "معوق"
        INSTALLMENT_PARTIAL -> "پرداخت جزئی"
        else -> "نامشخص"
    }

    // ---------- نوع پرداخت ----------
    const val PAYMENT_CASH = "CASH"                 // نقدی
    const val PAYMENT_INSTALLMENT = "INSTALLMENT"   // اقساطی
    const val PAYMENT_EXCHANGE = "EXCHANGE"         // تهاتر
    const val PAYMENT_FACILITY = "FACILITY"         // تسهیلات بانکی
    const val PAYMENT_MIXED = "MIXED"               // ترکیبی

    val paymentTypes = listOf(PAYMENT_CASH, PAYMENT_INSTALLMENT, PAYMENT_EXCHANGE, PAYMENT_FACILITY, PAYMENT_MIXED)

    fun paymentTypeLabel(type: String?): String = when (type) {
        PAYMENT_CASH -> "نقدی"
        PAYMENT_INSTALLMENT -> "اقساطی"
        PAYMENT_EXCHANGE -> "تهاتر"
        PAYMENT_FACILITY -> "تسهیلات بانکی"
        PAYMENT_MIXED -> "ترکیبی"
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

    // ---------- نوع سرنخ / منبع مشتری ----------
    const val SOURCE_REFERRAL = "REFERRAL"       // معرفی
    const val SOURCE_INSTAGRAM = "INSTAGRAM"     // اینستاگرام
    const val SOURCE_SITE = "SITE"               // وب‌سایت
    const val SOURCE_BANNER = "BANNER"           // بنر/تابلو
    const val SOURCE_EXHIBITION = "EXHIBITION"   // نمایشگاه
    const val SOURCE_WALK_IN = "WALK_IN"         // مراجعه حضوری
    const val SOURCE_CALL = "CALL"               // تماس تلفنی
    const val SOURCE_OTHER = "OTHER"

    val customerSources = listOf(
        SOURCE_REFERRAL, SOURCE_INSTAGRAM, SOURCE_SITE, SOURCE_BANNER,
        SOURCE_EXHIBITION, SOURCE_WALK_IN, SOURCE_CALL, SOURCE_OTHER
    )

    fun customerSourceLabel(source: String?): String = when (source) {
        SOURCE_REFERRAL -> "معرفی"
        SOURCE_INSTAGRAM -> "اینستاگرام"
        SOURCE_SITE -> "وب‌سایت"
        SOURCE_BANNER -> "بنر و تابلو"
        SOURCE_EXHIBITION -> "نمایشگاه"
        SOURCE_WALK_IN -> "مراجعه حضوری"
        SOURCE_CALL -> "تماس تلفنی"
        SOURCE_OTHER -> "سایر"
        else -> "نامشخص"
    }

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

    val documentTypes = listOf(
        "قرارداد پیش‌فروش", "مبایعه‌نامه", "رسید پرداخت", "چک", "سفته",
        "کارت ملی", "شناسنامه", "سند مالکیت", "پروانه ساخت", "سایر"
    )
}
