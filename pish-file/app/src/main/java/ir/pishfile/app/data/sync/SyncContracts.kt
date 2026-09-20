package ir.pishfile.app.data.sync

/**
 * قراردادهای همگام‌سازی (Sync).
 *
 * معماری فعلی «آفلاین‌اول» است: همه‌ی تغییرات ابتدا در دیتابیس محلی با
 * syncState = PENDING_UPLOAD ذخیره می‌شوند. وقتی سرور اضافه شد، فقط کافی است
 * یک SyncEngine جدید (مثلاً HttpSyncEngine) در AppContainer ثبت شود؛
 * هیچ‌کدام از صفحه‌ها یا ریپازیتوری‌ها نیاز به تغییر ندارند.
 */

/** وضعیت همگام‌سازی هر رکورد */
object SyncState {
    const val CLEAN = "CLEAN"                       // با سرور هم‌خوان است
    const val PENDING_UPLOAD = "PENDING_UPLOAD"     // منتظر ارسال
    const val PENDING_DELETE = "PENDING_DELETE"     // منتظر حذف روی سرور
    const val CONFLICT = "CONFLICT"                 // تعارض نیازمند تصمیم کاربر
}

/** نتیجه‌ی یک دور همگام‌سازی */
data class SyncResult(
    val uploadedCount: Int = 0,
    val downloadedCount: Int = 0,
    val conflictCount: Int = 0,
    val error: String? = null,
) {
    val isSuccess: Boolean get() = error == null
    val isEmpty: Boolean get() = uploadedCount == 0 && downloadedCount == 0 && conflictCount == 0
}

/** شمارش رکوردهای منتظر ارسال — برای نمایش به کاربر */
data class PendingChanges(
    val projects: Int = 0,
    val units: Int = 0,
    val customers: Int = 0,
    val preFiles: Int = 0,
    val installments: Int = 0,
    val followUps: Int = 0,
) {
    val total: Int get() = projects + units + customers + preFiles + installments + followUps
}

/** هر موتور همگام‌سازی این قرارداد را پیاده می‌کند (سرور REST، فایل، P2P، ...) */
interface SyncEngine {
    val name: String
    suspend fun push(pending: PendingChanges): SyncResult
    suspend fun pull(since: Long): SyncResult
    suspend fun isConfigured(): Boolean
}

/**
 * موتور نمونه — فعلاً کاری انجام نمی‌دهد و فقط معماری را نشان می‌دهد.
 * TODO(فاز بعد): این کلاس را با نسخه‌ی HttpSyncEngine (Ktor/Retrofit) جایگزین کنید.
 */
class DemoSyncEngine : SyncEngine {
    override val name: String = "همگام‌سازی محلی (غیرفعال)"

    override suspend fun push(pending: PendingChanges): SyncResult =
        SyncResult(error = "سرور تنظیم نشده است — داده‌ها امن روی همین گوشی ذخیره شده‌اند")

    override suspend fun pull(since: Long): SyncResult =
        SyncResult(error = "سرور تنظیم نشده است")

    override suspend fun isConfigured(): Boolean = false
}
