package ir.pishfile.app.core

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * زمان‌بندی کار پرداخت خودکار اقساط:
 *  - یک کار دوره‌ای ۲۴ ساعته (همیشه فعال)
 *  - یک کار فوری هنگام استارت برنامه (تا اگر گوشی خاموش بوده، همین حالا جبران شود)
 */
object InstallmentAutoPayScheduler {

    const val PERIODIC_WORK = "pishfile_installment_autopay"
    const val IMMEDIATE_WORK = "pishfile_installment_autopay_now"

    fun schedule(context: Context) {
        val workManager = WorkManager.getInstance(context)

        val periodic = PeriodicWorkRequestBuilder<InstallmentWorker>(24, TimeUnit.HOURS).build()
        workManager.enqueueUniquePeriodicWork(
            PERIODIC_WORK,
            ExistingPeriodicWorkPolicy.KEEP,
            periodic,
        )

        val immediate = OneTimeWorkRequestBuilder<InstallmentWorker>().build()
        workManager.enqueueUniqueWork(
            IMMEDIATE_WORK,
            ExistingWorkPolicy.REPLACE,
            immediate,
        )
    }
}
