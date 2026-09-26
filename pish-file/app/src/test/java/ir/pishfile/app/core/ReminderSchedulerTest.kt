package ir.pishfile.app.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Calendar

class ReminderSchedulerTest {

    @Test
    fun `parses jalali due date with explicit time`() {
        val today = Formatters.todayJalali()
        val millis = ReminderScheduler.reminderTimeMillis(today, "09:30")
        assertNotNull(millis)
        val calendar = Calendar.getInstance().apply { timeInMillis = millis!! }
        assertEquals(9, calendar.get(Calendar.HOUR_OF_DAY))
        assertEquals(30, calendar.get(Calendar.MINUTE))
    }

    @Test
    fun `defaults to 09 00 when no time provided`() {
        val today = Formatters.todayJalali()
        val millis = ReminderScheduler.reminderTimeMillis(today, null)
        assertNotNull(millis)
        val calendar = Calendar.getInstance().apply { timeInMillis = millis!! }
        assertEquals(9, calendar.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, calendar.get(Calendar.MINUTE))
    }

    @Test
    fun `accepts persian digits in time`() {
        val today = Formatters.todayJalali()
        val millis = ReminderScheduler.reminderTimeMillis(today, "۱۰:۴۵")
        assertNotNull(millis)
        val calendar = Calendar.getInstance().apply { timeInMillis = millis!! }
        assertEquals(10, calendar.get(Calendar.HOUR_OF_DAY))
        assertEquals(45, calendar.get(Calendar.MINUTE))
    }

    @Test
    fun `ignores invalid time and keeps default`() {
        val today = Formatters.todayJalali()
        val millis = ReminderScheduler.reminderTimeMillis(today, "25:99")
        assertNotNull(millis)
        val calendar = Calendar.getInstance().apply { timeInMillis = millis!! }
        assertEquals(9, calendar.get(Calendar.HOUR_OF_DAY))
    }

    @Test
    fun `returns null for invalid date`() {
        assertNull(ReminderScheduler.reminderTimeMillis("تاریخ نامعتبر", "10:00"))
        assertNull(ReminderScheduler.reminderTimeMillis(null, "10:00"))
    }

    @Test
    fun `future date is later than today`() {
        val today = Formatters.todayJalali()
        val tomorrow = Formatters.addJalaliDays(today, 1)
        val todayMillis = ReminderScheduler.reminderTimeMillis(today, "23:59")!!
        val tomorrowMillis = ReminderScheduler.reminderTimeMillis(tomorrow, "00:00")!!
        assertTrue(tomorrowMillis > todayMillis)
    }

    private fun assertTrue(condition: Boolean) {
        if (!condition) throw AssertionError("expected true")
    }
}
