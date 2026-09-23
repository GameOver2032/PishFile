package ir.pishfile.app.core

import ir.pishfile.app.data.repository.ProjectRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * پرداخت خودکار قسط سررسیدشده.
 *
 * وقتی به تاریخ «سررسید قسط پیش‌رو» یک پروژه رسیدیم، برنامه به‌صورت خودکار
 * قسط را از بخش اقساط پاک می‌کند (تاریخ سررسید خالی می‌شود و تعداد اقساط
 * باقی‌مانده یکی کم می‌شود) و مبلغ آن را به واریزی پروژه اضافه می‌کند.
 */
class InstallmentAutoPayer(private val projectRepository: ProjectRepository) {

    /**
     * همه‌ی پروژه‌های دارای قسط سررسیدشده را پردازش می‌کند.
     * تعداد پروژه‌هایی که قسطشان پرداخت شد را برمی‌گرداند.
     */
    suspend fun processDueInstallments(): Int = withContext(Dispatchers.IO) {
        var paidCount = 0
        for (project in projectRepository.getAll()) {
            val dueDate = project.nextInstallmentDueDate ?: continue
            // فقط تاریخ‌های امروز یا گذشته (بازگشتی null = تاریخ نامعتبر)
            val days = Formatters.daysFromToday(dueDate) ?: continue
            if (days > 0) continue
            val amount = project.installmentAmount ?: continue
            val remaining = project.remainingInstallmentsCount
            if (remaining == null || remaining <= 0) continue

            projectRepository.save(
                project.copy(
                    // مبلغ قسط به واریزی پروژه اضافه می‌شود
                    defaultDepositAmount = (project.defaultDepositAmount ?: 0L) + amount,
                    // قسط از بخش اقساط پاک می‌شود
                    remainingInstallmentsCount = remaining - 1,
                    nextInstallmentDueDate = null,
                )
            )
            paidCount++
        }
        paidCount
    }
}
