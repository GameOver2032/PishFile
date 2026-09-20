package ir.pishfile.app.data.sync

import ir.pishfile.app.data.local.PishFileDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * مدیریت همگام‌سازی. امروز فقط وضعیت «تغییرات معلق» را نگه می‌دارد و
 * موتور فعلی (DemoSyncEngine) هیچ ارتباط شبکه‌ای برقرار نمی‌کند.
 *
 * برای اتصال به سرور در آینده:
 *  ۱) یک کلاس HttpSyncEngine : SyncEngine بسازید (Retrofit/Ktor + احراز هویت)
 *  ۲) آن را در DefaultAppContainer به لیست engines اضافه کنید
 *  ۳) آدرس سرور را از تنظیمات (SettingsRepository.serverUrl) بخوانید
 */
class SyncRepository(
    private val database: PishFileDatabase,
    private val engines: List<SyncEngine>,
) {

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _lastResult = MutableStateFlow<SyncResult?>(null)
    val lastResult: StateFlow<SyncResult?> = _lastResult.asStateFlow()

    /** تعداد تغییرات منتظر ارسال (همه‌ی جداول) */
    suspend fun countPending(): PendingChanges = PendingChanges(
        projects = database.projectDao().getPendingSync().size,
        units = database.unitDao().getPendingSync().size,
        customers = database.customerDao().getPendingSync().size,
        preFiles = database.preFileDao().getPendingSync().size,
        installments = database.installmentDao().getPendingSync().size,
        followUps = database.followUpDao().getPendingSync().size,
    )

    /** یک دور کامل همگام‌سازی — آفلاین‌اول: اول ارسال، بعد دریافت */
    suspend fun syncNow(): SyncResult {
        _isSyncing.value = true
        return try {
            val pending = countPending()
            val configured = engines.firstOrNull { it.isConfigured() }
            val result = if (configured == null) {
                SyncResult(
                    uploadedCount = 0,
                    downloadedCount = 0,
                    error = "همگام‌سازی فعال نیست. داده‌ها روی همین دستگاه ذخیره شده‌اند."
                )
            } else {
                val pushResult = configured.push(pending)
                if (!pushResult.isSuccess) pushResult
                else {
                    val pullResult = configured.pull(0L)
                    SyncResult(
                        uploadedCount = pushResult.uploadedCount,
                        downloadedCount = pullResult.downloadedCount,
                        conflictCount = pullResult.conflictCount,
                        error = pullResult.error,
                    )
                }
            }
            markPushedIfSynced(result)
            _lastResult.value = result
            result
        } finally {
            _isSyncing.value = false
        }
    }

    /**
     * اگر ارسال موفق بود، وضعیت رکوردها «پاک» می‌شود.
     * (فعلاً موتور پیش‌فرض غیرفعال است، پس این تابع بی‌اثر می‌ماند.)
     */
    private fun markPushedIfSynced(result: SyncResult) {
        if (!result.isSuccess || result.uploadedCount == 0) return
        // TODO(فاز سرور): وضعیت رکوردهای ارسال‌شده را با updateSyncState به CLEAN تغییر دهید
    }

    /** پاک‌سازی رکوردهای حذف‌شده‌ای که قبلاً روی سرور ثبت شده‌اند */
    suspend fun purgeDeleted() {
        database.projectDao().purgeSyncedDeleted()
    }
}
