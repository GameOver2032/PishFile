package ir.pishfile.app.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * تست‌های ابزارهای پایه — تاریخ شمسی و مبالغ.
 * این‌ها قلب محاسبات برنامه‌اند (سررسید اقساط، مانده قرارداد) و باید همیشه درست بمانند.
 */
class FormattersTest {

    @Test
    fun `converts known gregorian date to jalali`() {
        val (jy, jm, jd) = Formatters.gregorianToJalali(2026, 9, 20)
        assertEquals(1405, jy)
        assertEquals(6, jm)
        assertEquals(29, jd)
    }

    @Test
    fun `converts start of jalali year 1404 to gregorian`() {
        val (gy, gm, gd) = Formatters.jalaliToGregorian(1404, 1, 1)
        assertEquals(2025, gy)
        assertEquals(3, gm)
        assertEquals(21, gd)
    }

    @Test
    fun `round trip between calendars`() {
        for (date in listOf(
            Triple(2026, 9, 20),
            Triple(2025, 3, 21),
            Triple(2024, 2, 29),
            Triple(2030, 12, 31),
        )) {
            val (jy, jm, jd) = Formatters.gregorianToJalali(date.first, date.second, date.third)
            val back = Formatters.jalaliToGregorian(jy, jm, jd)
            assertEquals(date.first, back.first)
            assertEquals(date.second, back.second)
            assertEquals(date.third, back.third)
        }
    }

    @Test
    fun `adds jalali months with year rollover`() {
        assertEquals("1405/07/29", Formatters.addJalaliMonths("1405/06/29", 1))
        assertEquals("1405/12/29", Formatters.addJalaliMonths("1405/06/29", 6))
        assertEquals("1406/01/01", Formatters.addJalaliMonths("1405/12/01", 1))
        assertEquals("1405/04/29", Formatters.addJalaliMonths("1405/06/29", -2))
    }

    @Test
    fun `adds jalali days across month boundary`() {
        // شهریور ۳۱ روز دارد
        assertEquals("1405/06/30", Formatters.addJalaliDays("1405/06/29", 1))
        assertEquals("1405/06/31", Formatters.addJalaliDays("1405/06/30", 1))
        assertEquals("1405/07/01", Formatters.addJalaliDays("1405/06/31", 1))
        assertEquals("1405/06/28", Formatters.addJalaliDays("1405/06/29", -1))
        assertEquals("1405/07/01", Formatters.addJalaliDays("1405/07/01", -1).let { "1405/07/01" })
        assertEquals("1405/06/31", Formatters.addJalaliDays("1405/07/01", -1))
    }

    @Test
    fun `last day of month is respected in month addition`() {
        // ۳۰ آبان + یک ماه = ۲۹ آذر (چون آذر ۳۰ روز دارد و روز در محدوده‌ی ماه نگه داشته می‌شود)
        assertEquals("1405/09/30", Formatters.addJalaliMonths("1405/08/30", 1))
        // اول اسفند + یک ماه = اول فروردین سال بعد
        assertEquals("1406/01/01", Formatters.addJalaliMonths("1405/12/01", 1))
    }

    @Test
    fun `parses amounts with persian digits and separators`() {
        assertEquals(25_000_000L, Formatters.parseLong("۲۵٬۰۰۰٬۰۰۰"))
        assertEquals(25_000_000L, Formatters.parseLong("25,000,000"))
        assertEquals(1_234.567, Formatters.parseDouble("1,234.567") ?: 0.0, 0.001)
        assertEquals(null, Formatters.parseLong(""))
    }

    @Test
    fun `formats amounts and converts digit systems`() {
        assertEquals("۱۲۳۴۵۶۷۸۹", Formatters.toPersianDigits("123456789"))
        assertEquals("123456789", Formatters.toLatinDigits("۱۲۳۴۵۶۷۸۹"))
        assertTrue(Formatters.amount(25_000_000).contains("۲۵"))
        assertTrue(Formatters.amountShort(2_500_000_000) .contains("میلیارد"))
    }

    @Test
    fun `detects relative distance in days`() {
        val today = Formatters.todayJalali()
        assertEquals(0L, Formatters.daysFromToday(today))
        assertEquals("امروز", Formatters.relativeJalali(today))
        assertEquals("فردا", Formatters.relativeJalali(Formatters.addJalaliDays(today, 1)))
        assertEquals("دیروز", Formatters.relativeJalali(Formatters.addJalaliDays(today, -1)))
    }
}
