package ir.pishfile.app.core

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import ir.pishfile.app.PishFileApp

/**
 * کار دوره‌ای WorkManager: هر روز بررسی می‌کند قسط سررسیدشده‌ای
 * وجود دارد و در صورت نیاز آن را به واریزی پروژه اضافه می‌کند.
 */
class InstallmentWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val container = (applicationContext as PishFileApp).container
            container.installmentAutoPayer.processDueInstallments()
            Result.success()
        } catch (e: Exception) {
            // در صورت خطا، دوباره تلاش می‌کنیم (کارهای دوره‌ای به‌طور ذاتی تکرار می‌شوند)
            Result.retry()
        }
    }
}
