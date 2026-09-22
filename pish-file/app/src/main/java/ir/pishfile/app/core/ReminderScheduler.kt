package ir.pishfile.app.core

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import ir.pishfile.app.data.local.entity.FollowUpEntity
import java.util.Calendar

/**
 * تنظیم آلارم (یادآوری) برای پیگیری‌ها.
 *
 * برای هر پیگیری در وضعیت «در انتظار» که تاریخ (و در صورت نیاز ساعت) دارد،
 * یک آلارم دقیق یک‌بار در آینده ثبت می‌شود؛ در لحظه‌ی سررسید
 * [ReminderReceiver] اعلان را نشان می‌دهد.
 */
class ReminderScheduler(private val context: Context) {

    private val alarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /** ثبت (یا بازآرایی) آلارم برای یک پیگیری؛ اگر تاریخ نامعتبر باشد آلارم لغو می‌شود. */
    fun schedule(followUp: FollowUpEntity) {
        val triggerAt = reminderTimeMillis(followUp.dueDate, followUp.dueTime)
        if (triggerAt == null || triggerAt <= System.currentTimeMillis()) {
            cancel(followUp.id)
            return
        }
        val pendingIntent = pendingIntentFor(followUp.id)
        // در اندروید ۱۲+ اگر کاربر مجوز آلارم دقیق را داده باشد دقیق، وگرنه تقریبی
        val canUseExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            alarmManager.canScheduleExactAlarms()
        if (canUseExact) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        }
    }

    /** لغو آلارم یک پیگیری (انجام شد / لغو شد / حذف). */
    fun cancel(followUpId: String) {
        alarmManager.cancel(pendingIntentFor(followUpId))
    }

    private fun pendingIntentFor(followUpId: String): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ACTION_REMINDER
            putExtra(EXTRA_FOLLOWUP_ID, followUpId)
        }
        return PendingIntent.getBroadcast(
            context,
            followUpId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        const val ACTION_REMINDER = "ir.pishfile.app.ACTION_FOLLOWUP_REMINDER"
        const val EXTRA_FOLLOWUP_ID = "followup_id"
        const val CHANNEL_ID = "follow_up_reminders"

        /**
         * محاسبه‌ی زمان زنگ خوردن آلارم برای یک پیگیری:
         * ساعت دلخواه روی تاریخ شمسی سررسید؛ اگر ساعتی داده نشده باشد، ساعت ۰۹:۰۰.
         * برگشتی null = تاریخ معتبر ندارد (آلارم معنایی ندارد).
         */
        fun reminderTimeMillis(dueDate: String?, dueTime: String?): Long? {
            val base = Formatters.jalaliStringToEpoch(dueDate) ?: return null
            var hour = 9
            var minute = 0
            if (!dueTime.isNullOrBlank()) {
                val parts = Formatters.toLatinDigits(dueTime).trim().split(':')
                val h = parts.getOrNull(0)?.toIntOrNull()
                val m = parts.getOrNull(1)?.toIntOrNull()
                if (h != null && h in 0..23 && (m == null || m in 0..59)) {
                    hour = h
                    minute = m ?: 0
                }
            }
            val calendar = Calendar.getInstance().apply {
                timeInMillis = base
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            return calendar.timeInMillis
        }
    }
}
