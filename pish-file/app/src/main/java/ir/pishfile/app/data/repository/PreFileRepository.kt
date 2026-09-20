package ir.pishfile.app.data.repository

import ir.pishfile.app.core.Constants
import ir.pishfile.app.core.Formatters
import ir.pishfile.app.data.local.dao.FinanceSummary
import ir.pishfile.app.data.local.dao.InstallmentDao
import ir.pishfile.app.data.local.dao.PreFileDao
import ir.pishfile.app.data.local.dao.PreFileRow
import ir.pishfile.app.data.local.dao.ProjectDao
import ir.pishfile.app.data.local.dao.UnitDao
import ir.pishfile.app.data.local.dao.CustomerDao
import ir.pishfile.app.data.local.dao.FollowUpDao
import ir.pishfile.app.data.local.entity.InstallmentEntity
import ir.pishfile.app.data.local.entity.PreFileEntity
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

/**
 * منطق کاری پیش‌فایل — مهم‌ترین کلاس دامنه‌ی برنامه.
 *
 * مسئولیت‌ها:
 *  - شماره‌گذاری خودکار پیش‌فایل (PF-سال-شماره)
 *  - تولید خودکار جدول اقساط بر اساس شرایط توافق
 *  - ثبت پرداخت و محاسبه‌ی مانده و درصد پیشرفت
 *  - به‌روزرسانی خودکار وضعیت واحد (آزاد/رزرو/فروخته‌شده)
 */
class PreFileRepository(
    private val preFileDao: PreFileDao,
    private val installmentDao: InstallmentDao,
    private val unitDao: UnitDao,
    private val projectDao: ProjectDao,
    private val customerDao: CustomerDao,
    private val followUpDao: FollowUpDao,
) {

    // ---------- خواندن ----------
    fun observeAllRows(): Flow<List<PreFileRow>> = preFileDao.observeAllRows()
    fun observeRowsByStatus(status: String): Flow<List<PreFileRow>> = preFileDao.observeRowsByStatus(status)
    fun observeRowsByProject(projectId: String): Flow<List<PreFileRow>> = preFileDao.observeRowsByProject(projectId)
    fun observeRowsByCustomer(customerId: String): Flow<List<PreFileRow>> = preFileDao.observeRowsByCustomer(customerId)
    fun search(query: String): Flow<List<PreFileRow>> = preFileDao.search(query.trim())
    fun observeById(id: String): Flow<PreFileEntity?> = preFileDao.observeById(id)
    fun observeRowById(id: String): Flow<PreFileRow?> = preFileDao.observeRowById(id)
    fun observeCount(): Flow<Int> = preFileDao.observeCount()
    fun observeStatusCounts() = preFileDao.observeStatusCounts()
    fun observeFinanceSummary(): Flow<FinanceSummary?> = preFileDao.observeFinanceSummary()
    fun observeTotalReceivables(projectId: String? = null) = preFileDao.observeTotalReceivables(projectId)
    fun observeReceivedFrom(fromDate: String, projectId: String? = null) =
        preFileDao.observeReceivedFrom(fromDate, projectId)
    fun observeProjectSales(projectId: String) = preFileDao.observeProjectSales(projectId)
    fun observeIncomplete(): Flow<List<PreFileEntity>> = preFileDao.observeIncomplete()

    suspend fun getById(id: String): PreFileEntity? = preFileDao.getById(id)
    suspend fun getAll(): List<PreFileEntity> = preFileDao.getAll()

    fun observeInstallments(preFileId: String): Flow<List<InstallmentEntity>> =
        installmentDao.observeByPreFile(preFileId)

    /** سررسیدهای پیش‌رو تا n روز آینده — برای داشبورد */
    fun observeUpcomingInstallments(daysAhead: Int = 30): Flow<List<InstallmentEntity>> {
        val until = Formatters.addJalaliDays(Formatters.todayJalali(), daysAhead)
        return installmentDao.observeDue(until)
    }

    fun observeOverdueInstallments(): Flow<List<InstallmentEntity>> =
        installmentDao.observeOverdue(Formatters.todayJalali())

    fun observeOverdueAmount(): Flow<Long?> = installmentDao.observeOverdueAmount(Formatters.todayJalali())

    fun observePendingFollowUps() = followUpDao.observePending()

    // ---------- شماره‌گذاری خودکار ----------
    /**
     * شماره‌ی پیش‌فایل بعدی بر اساس سال شمسی جاری: PF-1405-0007
     */
    suspend fun nextDraftNumber(): String {
        val year = Formatters.todayJalali().substringBefore('/')
        val count = preFileDao.countForYear(year)
        return "PF-$year-${(count + 1).toString().padStart(4, '0')}"
    }

    // ---------- ذخیره ----------
    /**
     * ذخیره‌ی پیش‌فایل + به‌روزرسانی وضعیت واحد.
     * اگر شرایط اقساط تغییر کرده باشد، جدول اقساط بازسازی می‌شود.
     */
    suspend fun save(preFile: PreFileEntity, regenerateInstallments: Boolean = false) {
        val existing = preFileDao.getById(preFile.id)
        val toSave = preFile.copy(
            updatedAt = System.currentTimeMillis(),
            syncState = "PENDING_UPLOAD",
            finalPrice = preFile.finalPrice ?: (preFile.totalPrice - (preFile.discount ?: 0L)),
            remainingAmount = preFile.effectivePrice - preFile.paidAmount,
        )

        if (existing == null) preFileDao.insert(toSave) else preFileDao.update(toSave)

        // وضعیت واحد بر اساس وضعیت پیش‌فایل
        toSave.unitId?.let { unitId ->
            val newUnitStatus = when (toSave.status) {
                Constants.PREFILE_DRAFT -> Constants.UNIT_AVAILABLE
                Constants.PREFILE_RESERVED, Constants.PREFILE_PENDING_PAYMENT -> Constants.UNIT_RESERVED
                Constants.PREFILE_CONFIRMED -> Constants.UNIT_SOLD
                Constants.PREFILE_COMPLETED -> Constants.UNIT_DELIVERED
                Constants.PREFILE_CANCELED -> Constants.UNIT_AVAILABLE
                else -> Constants.UNIT_AVAILABLE
            }
            unitDao.updateStatus(unitId, newUnitStatus)
        }

        if (regenerateInstallments) {
            generateInstallments(toSave)
        }
    }

    /**
     * تولید خودکار جدول اقساط.
     * پیش‌پرداخت به‌عنوان قسط شماره ۱ و مابقی بر اساس دوره‌ی پرداخت تقسیم می‌شود.
     */
    suspend fun generateInstallments(preFile: PreFileEntity): Int {
        val count = preFile.installmentCount ?: 0
        val startDate = preFile.installmentStartDate
        val remaining = preFile.effectivePrice - (preFile.prepayment ?: 0L)
        if (count <= 0 || startDate.isNullOrBlank() || remaining <= 0) return 0

        val periodMonths = when (preFile.installmentPeriod) {
            "دو ماهه" -> 2
            "فصلی" -> 3
            "شش‌ماهه" -> 6
            "سالانه" -> 12
            else -> 1
        }

        val existing = installmentDao.getByPreFile(preFile.id).filter { it.status == Constants.INSTALLMENT_PAID }
        installmentDao.deleteByPreFile(preFile.id)

        val perInstallment = preFile.installmentAmount ?: (remaining / count)
        val created = mutableListOf<InstallmentEntity>()

        // قسط اول = پیش‌پرداخت (اگر ثبت نشده باشد)
        if ((preFile.prepayment ?: 0L) > 0 && preFile.paidAmount < (preFile.prepayment ?: 0L)) {
            created += InstallmentEntity(
                preFileId = preFile.id,
                installmentNumber = 0,
                title = "پیش‌پرداخت",
                amount = preFile.prepayment ?: 0L,
                dueDate = preFile.draftDate,
                status = Constants.INSTALLMENT_UNPAID,
            )
        }

        for (i in 1..count) {
            created += InstallmentEntity(
                preFileId = preFile.id,
                installmentNumber = i,
                title = "قسط $i",
                amount = perInstallment,
                dueDate = Formatters.addJalaliMonths(startDate, periodMonths * (i - 1)),
                status = Constants.INSTALLMENT_UNPAID,
                latePenalty = 0,
            )
        }

        // پرداخت‌های قبلی حفظ می‌شوند
        val unchanged = existing.map { it.copy(preFileId = preFile.id) }
        installmentDao.insertAll(created + unchanged)
        return created.size
    }

    /** ثبت پرداخت یک قسط */
    suspend fun markInstallmentPaid(
        installmentId: String,
        amountPaid: Long,
        paidDate: String = Formatters.todayJalali(),
        paymentMethod: String? = null,
        referenceNumber: String? = null,
        bankName: String? = null,
    ) {
        val installment = installmentDao.getById(installmentId) ?: return
        val totalPaid = installment.paidAmount + amountPaid
        val status = when {
            totalPaid >= installment.amount -> Constants.INSTALLMENT_PAID
            totalPaid > 0 -> Constants.INSTALLMENT_PARTIAL
            else -> Constants.INSTALLMENT_UNPAID
        }
        installmentDao.update(
            installment.copy(
                paidAmount = totalPaid,
                status = status,
                paidDate = paidDate,
                paymentMethod = paymentMethod ?: installment.paymentMethod,
                referenceNumber = referenceNumber ?: installment.referenceNumber,
                bankName = bankName ?: installment.bankName,
                updatedAt = System.currentTimeMillis(),
                syncState = "PENDING_UPLOAD",
            )
        )
        refreshPreFilePaidAmount(installment.preFileId)
    }

    /** لغو پرداخت قسط */
    suspend fun markInstallmentUnpaid(installmentId: String) {
        val installment = installmentDao.getById(installmentId) ?: return
        installmentDao.update(
            installment.copy(
                paidAmount = 0,
                status = Constants.INSTALLMENT_UNPAID,
                paidDate = null,
                updatedAt = System.currentTimeMillis(),
                syncState = "PENDING_UPLOAD",
            )
        )
        refreshPreFilePaidAmount(installment.preFileId)
    }

    /** جمع پرداخت‌های اقساط را روی پیش‌فایل می‌نویسد */
    private suspend fun refreshPreFilePaidAmount(preFileId: String) {
        val preFile = preFileDao.getById(preFileId) ?: return
        val installments = installmentDao.getByPreFile(preFileId)
        val totalPaid = installments.sumOf { it.paidAmount }
        preFileDao.update(
            preFile.copy(
                paidAmount = totalPaid,
                remainingAmount = preFile.effectivePrice - totalPaid,
                updatedAt = System.currentTimeMillis(),
                syncState = "PENDING_UPLOAD",
            )
        )
        if (preFile.remainingAmount != null) return
    }

    /** پرداخت مستقیم روی قرارداد (بدون اتصال به قسط مشخص) */
    suspend fun addPayment(preFileId: String, amount: Long) {
        preFileDao.addPayment(preFileId, amount)
        val preFile = preFileDao.getById(preFileId) ?: return
        preFileDao.update(
            preFile.copy(remainingAmount = preFile.effectivePrice - preFile.paidAmount)
        )
    }

    /** علامت‌گذاری اقساط سررسیدگذشته به‌عنوان معوق */
    suspend fun markOverdueInstallments() {
        installmentDao.markOverdue(Formatters.todayJalali())
    }

    suspend fun deleteInstallment(id: String) = installmentDao.softDelete(id)

    suspend fun delete(preFileId: String) {
        val preFile = preFileDao.getById(preFileId)
        preFileDao.softDelete(preFileId)
        // واحد آزاد می‌شود
        preFile?.unitId?.let { unitDao.updateStatus(it, Constants.UNIT_AVAILABLE) }
    }

    /** کپی از یک پیش‌فایل برای مشتری/واحد دیگر (سرعت کار فروش) */
    suspend fun duplicate(source: PreFileEntity): PreFileEntity {
        val newId = java.util.UUID.randomUUID().toString()
        val copy = source.copy(
            id = newId,
            draftNumber = nextDraftNumber(),
            draftDate = Formatters.todayJalali(),
            status = Constants.PREFILE_DRAFT,
            paidAmount = 0,
            unitId = null,
            trackingCode = null,
            remoteId = null,
            syncState = "PENDING_UPLOAD",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
        )
        preFileDao.insert(copy)
        return copy
    }

    /** تاریخ امروز برای فیلدهای پیش‌فرض */
    fun todayJalali(): String = Formatters.todayJalali()

    companion object {
        /** تعداد روزهای باقی‌مانده تا سررسید */
        fun daysUntil(dueDate: String): Long? = Formatters.daysFromToday(dueDate)

        fun epochOf(jalali: String): Long? = Formatters.jalaliStringToEpoch(jalali)
    }
}
