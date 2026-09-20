package ir.pishfile.app.core

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Calendar
import java.util.Locale

/**
 * ابزارهای کمکی برای اعداد، مبالغ و تاریخ شمسی.
 * همه‌چیز آفلاین و بدون کتابخانه خارجی محاسبه می‌شود.
 */
object Formatters {

    private val faLocale = Locale("fa", "IR")

    private val amountFormat = DecimalFormat("#,###", DecimalFormatSymbols(faLocale))
    private val latinDigits = DecimalFormat("#,###", DecimalFormatSymbols(Locale.US))

    /** مبلغ با جداکننده هزارگان — «۲۵٬۰۰۰٬۰۰۰» */
    fun amount(value: Long?): String {
        if (value == null) return "—"
        return amountFormat.format(value)
    }

    /** مبلغ به صورت «۲۵ میلیون تومان» برای نمایش خلاصه */
    fun amountShort(value: Long?): String {
        if (value == null) return "—"
        val abs = kotlin.math.abs(value)
        return when {
            abs >= 1_000_000_000L -> formatDecimal(value / 1_000_000_000.0) + " میلیارد"
            abs >= 1_000_000L -> formatDecimal(value / 1_000_000.0) + " میلیون"
            abs >= 1_000L -> formatDecimal(value / 1_000.0) + " هزار"
            else -> amountFormat.format(value)
        }
    }

    fun amountWithUnit(value: Long?): String = if (value == null) "—" else "${amountShort(value)} تومان"

    private fun formatDecimal(d: Double): String {
        val s = String.format(Locale.US, "%.2f", d).trimEnd('0').trimEnd('.')
        return toPersianDigits(s)
    }

    /** تبدیل رقم‌های لاتین به فارسی */
    fun toPersianDigits(input: String): String {
        val sb = StringBuilder(input.length)
        for (c in input) {
            sb.append(
                when (c) {
                    in '0'..'9' -> ('۰' + (c - '0'))
                    else -> c
                }
            )
        }
        return sb.toString()
    }

    /** تبدیل رقم‌های فارسی/عربی به لاتین (برای ورودی کاربر) */
    fun toLatinDigits(input: String): String {
        val sb = StringBuilder(input.length)
        for (c in input) {
            sb.append(
                when (c) {
                    in '۰'..'۹' -> ('0' + (c - '۰'))
                    in '٠'..'٩' -> ('0' + (c - '٠'))
                    else -> c
                }
            )
        }
        return sb.toString()
    }

    /** حذف جداکننده/کاراکترهای غیرعددی و تبدیل به عدد */
    fun parseLong(input: String?): Long? {
        if (input.isNullOrBlank()) return null
        val clean = toLatinDigits(input).filter { it.isDigit() }
        return clean.toLongOrNull()
    }

    fun parseDouble(input: String?): Double? {
        if (input.isNullOrBlank()) return null
        val clean = toLatinDigits(input).replace(",", "").trim()
        return clean.toDoubleOrNull()
    }

    /** نمایش عدد ساده با رقم فارسی */
    fun number(value: Number?): String = if (value == null) "—" else toPersianDigits(value.toString())

    /** درصد */
    fun percent(value: Int?): String = if (value == null) "—" else "${toPersianDigits(value.toString())}٪"

    // ---------- تاریخ شمسی (تبدیل بدون وابستگی خارجی) ----------

    /** تبدیل میلادی به شمسی (الگوریتم استاندارد با حساب صحیح) */
    fun gregorianToJalali(gy: Int, gm: Int, gd: Int): Triple<Int, Int, Int> {
        val gDaysInMonth = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)
        var jy: Int
        var gyBase = gy
        if (gyBase > 1600) {
            jy = 979
            gyBase -= 1600
        } else {
            jy = 0
            gyBase -= 621
        }
        val gy2 = if (gm > 2) gyBase + 1 else gyBase
        var days = 365 * gyBase + (gy2 + 3) / 4 - (gy2 + 99) / 100 + (gy2 + 399) / 400 -
            80 + gd + gDaysInMonth[gm - 1]
        jy += 33 * (days / 12053)
        days %= 12053
        jy += 4 * (days / 1461)
        days %= 1461
        if (days > 365) {
            jy += (days - 1) / 365
            days = (days - 1) % 365
        }
        val jm = if (days < 186) 1 + days / 31 else 7 + (days - 186) / 30
        val jd = 1 + (if (days < 186) days % 31 else (days - 186) % 30)
        return Triple(jy, jm, jd)
    }

    private val jalaliMonths = listOf(
        "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
    )

    fun jalaliMonthName(month: Int): String = jalaliMonths.getOrElse(month - 1) { "" }

    /** تاریخ امروز به شمسی: «۱۴۰۵/۰۶/۲۹» */
    fun todayJalali(): String = epochToJalali(System.currentTimeMillis())

    fun epochToJalali(epochMillis: Long): String {
        val cal = Calendar.getInstance().apply { timeInMillis = epochMillis }
        val (jy, jm, jd) = gregorianToJalali(
            cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH)
        )
        return toPersianDigits(String.format(Locale.US, "%04d/%02d/%02d", jy, jm, jd))
    }

    /** «۲۹ شهریور ۱۴۰۵» */
    fun epochToJalaliLong(epochMillis: Long): String {
        val cal = Calendar.getInstance().apply { timeInMillis = epochMillis }
        val (jy, jm, jd) = gregorianToJalali(
            cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH)
        )
        return "${toPersianDigits(jd.toString())} ${jalaliMonthName(jm)} ${toPersianDigits(jy.toString())}"
    }

    /** تبدیل تاریخ شمسی به میلادی */
    fun jalaliToGregorian(jy0: Int, jm: Int, jd: Int): Triple<Int, Int, Int> {
        val gyStart: Int
        var jy = jy0
        if (jy > 979) {
            gyStart = 1600
            jy -= 979
        } else {
            gyStart = 621
        }
        var gy = gyStart
        var days = 365 * jy + (jy / 33) * 8 + ((jy % 33) + 3) / 4 + 78 + jd +
            (if (jm < 7) (jm - 1) * 31 else ((jm - 7) * 30) + 186)
        gy += 400 * (days / 146097)
        days %= 146097
        if (days > 36524) {
            days -= 1
            gy += 100 * (days / 36524)
            days %= 36524
            if (days >= 365) days += 1
        }
        gy += 4 * (days / 1461)
        days %= 1461
        if (days > 365) {
            gy += (days - 1) / 365
            days = (days - 1) % 365
        }
        var gd = days + 1
        val monthLengths = intArrayOf(
            0, 31,
            if ((gy % 4 == 0 && gy % 100 != 0) || (gy % 400 == 0)) 29 else 28,
            31, 30, 31, 30, 31, 31, 30, 31, 30, 31
        )
        var gm = 0
        while (gm < 13 && gd > monthLengths[gm]) {
            gd -= monthLengths[gm]
            gm++
        }
        return Triple(gy, gm, gd)
    }

    /** تبدیل «۱۴۰۵/۰۶/۲۹» به زمان (میلی‌ثانیه) — برای مرتب‌سازی و یادآوری */
    fun jalaliStringToEpoch(text: String?): Long? {
        if (text.isNullOrBlank()) return null
        val parts = toLatinDigits(text).trim().split('/', '-', '.')
        if (parts.size < 3) return null
        val jy = parts[0].toIntOrNull() ?: return null
        val jm = parts[1].toIntOrNull() ?: return null
        val jd = parts[2].toIntOrNull() ?: return null
        if (jm !in 1..12 || jd !in 1..31) return null
        val (gy, gm, gd) = jalaliToGregorian(jy, jm, jd)
        return Calendar.getInstance().apply {
            clear()
            set(gy, gm - 1, gd, 12, 0, 0)
        }.timeInMillis
    }

    /** فاصله روز از امروز (منفی = گذشته) */
    fun daysFromToday(jalaliDate: String?): Long? {
        val target = jalaliStringToEpoch(jalaliDate) ?: return null
        val dayMs = 24L * 60 * 60 * 1000
        return Math.floorDiv(target - System.currentTimeMillis() + dayMs / 2, dayMs)
    }

    /** فاصله زمانی خوانا: «۳ روز پیش»، «امروز»، «۵ روز دیگر» */
    fun relativeJalali(jalaliDate: String?): String {
        val d = daysFromToday(jalaliDate) ?: return "—"
        return when {
            d == 0L -> "امروز"
            d == 1L -> "فردا"
            d == -1L -> "دیروز"
            d < 0 -> "${toPersianDigits((-d).toString())} روز پیش"
            else -> "${toPersianDigits(d.toString())} روز دیگر"
        }
    }

    /** تاریخ کامل امروز برای فیلدهای پیش‌فرض */
    fun nowJalaliFull(): String = todayJalali()

    /** تعداد روزهای یک ماه شمسی (برای محاسبات سررسید) */
    fun jalaliMonthLength(jy: Int, jm: Int): Int = when (jm) {
        in 1..6 -> 31
        in 7..11 -> 30
        else -> if (isJalaliLeapYear(jy)) 30 else 29
    }

    /** سال کبیسه شمسی (الگوریتم چرخه ۳۳ ساله) */
    fun isJalaliLeapYear(jy: Int): Boolean {
        val remainder = jy % 33
        return remainder in intArrayOf(1, 5, 9, 13, 17, 22, 26, 30)
    }

    /** تبدیل رشته‌ی شمسی به سه جزء عددی */
    fun parseJalali(text: String?): Triple<Int, Int, Int>? {
        if (text.isNullOrBlank()) return null
        val parts = toLatinDigits(text).trim().split('/', '-', '.')
        if (parts.size < 3) return null
        val jy = parts[0].toIntOrNull() ?: return null
        val jm = parts[1].toIntOrNull() ?: return null
        val jd = parts[2].toIntOrNull() ?: return null
        if (jm !in 1..12 || jd !in 1..31) return null
        return Triple(jy, jm, jd)
    }

    /** قالب‌بندی شمسی استاندارد: 1405/06/29 */
    fun formatJalali(jy: Int, jm: Int, jd: Int): String =
        String.format(Locale.US, "%04d/%02d/%02d", jy, jm, jd)

    /** افزودن/کاهش ماه به تاریخ شمسی — برای ساخت خودکار سررسید اقساط */
    fun addJalaliMonths(date: String, months: Int): String {
        val (jy, jm, jd) = parseJalali(date) ?: return date
        var totalMonths = (jy * 12 + (jm - 1)) + months
        val newJy = Math.floorDiv(totalMonths, 12)
        val newJm = Math.floorMod(totalMonths, 12) + 1
        val maxDay = jalaliMonthLength(newJy, newJm)
        return formatJalali(newJy, newJm, minOf(jd, maxDay))
    }

    /** افزودن/کاهش روز به تاریخ شمسی */
    fun addJalaliDays(date: String, days: Int): String {
        val (jy, jm, jd) = parseJalali(date) ?: return date
        var y = jy
        var m = jm
        var d = jd + days
        var guard = 0
        while (d < 1 && guard++ < 1000) {
            m -= 1
            if (m < 1) { m = 12; y -= 1 }
            d += jalaliMonthLength(y, m)
        }
        guard = 0
        while (d > jalaliMonthLength(y, m) && guard++ < 1000) {
            d -= jalaliMonthLength(y, m)
            m += 1
            if (m > 12) { m = 1; y += 1 }
        }
        return formatJalali(y, m, d)
    }

    /** تاریخ امروز به‌صورت اجزا */
    fun todayJalaliParts(): Triple<Int, Int, Int> {
        val cal = Calendar.getInstance()
        return gregorianToJalali(
            cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH)
        )
    }
}
