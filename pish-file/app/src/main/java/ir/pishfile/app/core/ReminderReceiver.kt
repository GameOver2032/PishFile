package ir.pishfile.app.core

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import ir.pishfile.app.MainActivity
import ir.pishfile.app.data.local.PishFileDatabase
import kotlinx.coroutines.runBlocking

/**
 * دریافت‌کننده‌ی آلارم پیگیری — در زمان سررسید، اعلان نشان می‌دهد.
 * اگر پیگیری قبلاً انجام شده/لغو/حذف شده باشد، اعلان داده نمی‌شود.
 */
class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val followUpId = intent.getStringExtra(ReminderScheduler.EXTRA_FOLLOWUP_ID) ?: return

        // بررسی تازه‌گی: فقط پیگیری‌های «در انتظار» اعلان می‌گیرند
        val followUp = runBlocking {
            PishFileDatabase.getInstance(context).followUpDao().getById(followUpId)
        }
        if (followUp == null || followUp.deletedAt != null) return
        if (followUp.status != FollowUpStatus.PENDING) return

        ensureChannel(context)

        val openApp = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                action = Intent.ACTION_MAIN
                addCategory(Intent.CATEGORY_LAUNCHER)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val details = listOfNotNull(
            followUp.dueTime?.let { "ساعت: $it" },
            followUp.contactPhone?.let { "تماس: $it" },
            followUp.description
        ).joinToString("\n")

        val notification = Notification.Builder(context, ReminderScheduler.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("یادآوری پیگیری: ${followUp.title}")
            .setContentText(followUp.description ?: "نوبت پیگیری با مشتری است")
            .setStyle(Notification.BigTextStyle().bigText(details.ifBlank { followUp.title }))
            .setAutoCancel(true)
            .setContentIntent(openApp)
            .setPriority(Notification.PRIORITY_HIGH)
            .build()

        val manager = ContextCompat.getSystemService(context, NotificationManager::class.java)
        manager?.notify(followUpId.hashCode(), notification)
    }

    private fun ensureChannel(context: Context) {
        val manager = ContextCompat.getSystemService(context, NotificationManager::class.java) ?: return
        if (manager.getNotificationChannel(ReminderScheduler.CHANNEL_ID) == null) {
            manager.createNotificationChannel(
                NotificationChannel(
                    ReminderScheduler.CHANNEL_ID,
                    "یادآوری پیگیری‌ها",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "اعلان برای پیگیری‌های زمان‌بندی‌شده با مشتری"
                }
            )
        }
    }

    /** کد وضعیت «در انتظار» — جدا نگه داشته تا این فایل به بسته‌ی UI وابسته نماند. */
    private object FollowUpStatus {
        const val PENDING = "PENDING"
    }
}
