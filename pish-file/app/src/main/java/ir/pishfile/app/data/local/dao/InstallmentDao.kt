package ir.pishfile.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import ir.pishfile.app.data.local.entity.InstallmentEntity
import kotlinx.coroutines.flow.Flow

/** قسط همراه با نام مشتری/پروژه — برای صفحه‌ی سررسیدها */
data class InstallmentRow(
    val installment: InstallmentEntity,
    val customerName: String?,
    val customerPhone: String?,
    val projectName: String?,
    val draftNumber: String?,
)

@Dao
interface InstallmentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(installment: InstallmentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(installments: List<InstallmentEntity>)

    @Update
    suspend fun update(installment: InstallmentEntity)

    @Delete
    suspend fun delete(installment: InstallmentEntity)

    @Query("SELECT * FROM installments WHERE preFileId = :preFileId AND deletedAt IS NULL ORDER BY installmentNumber")
    fun observeByPreFile(preFileId: String): Flow<List<InstallmentEntity>>

    @Query("SELECT * FROM installments WHERE preFileId = :preFileId AND deletedAt IS NULL ORDER BY installmentNumber")
    suspend fun getByPreFile(preFileId: String): List<InstallmentEntity>

    @Query("SELECT * FROM installments WHERE id = :id")
    suspend fun getById(id: String): InstallmentEntity?

    @Query("SELECT * FROM installments WHERE id = :id")
    fun observeById(id: String): Flow<InstallmentEntity?>

    @Query("SELECT * FROM installments WHERE deletedAt IS NULL ORDER BY dueDate")
    fun observeAll(): Flow<List<InstallmentEntity>>

    @Query(
        """
        SELECT i.* FROM installments i
        INNER JOIN pre_files p ON p.id = i.preFileId
        LEFT JOIN customers c ON c.id = p.customerId
        LEFT JOIN projects pr ON pr.id = p.projectId
        WHERE i.deletedAt IS NULL AND p.deletedAt IS NULL
        ORDER BY i.dueDate
        """
    )
    fun observeAllWithContext(): Flow<List<InstallmentEntity>>

    /** سررسیدهای پیش‌رو (تا تاریخ مشخص) که هنوز پرداخت نشده‌اند */
    @Query(
        """
        SELECT * FROM installments
        WHERE deletedAt IS NULL AND status IN ('UNPAID','PARTIAL','OVERDUE')
          AND dueDate <= :untilDate
        ORDER BY dueDate
        """
    )
    fun observeDue(untilDate: String): Flow<List<InstallmentEntity>>

    @Query(
        """
        SELECT * FROM installments
        WHERE deletedAt IS NULL AND status IN ('UNPAID','PARTIAL','OVERDUE')
          AND dueDate < :today
        ORDER BY dueDate
        """
    )
    fun observeOverdue(today: String): Flow<List<InstallmentEntity>>

    @Query(
        """
        SELECT * FROM installments
        WHERE deletedAt IS NULL AND status IN ('UNPAID','PARTIAL','OVERDUE')
          AND dueDate BETWEEN :fromDate AND :toDate
        ORDER BY dueDate
        """
    )
    fun observeDueBetween(fromDate: String, toDate: String): Flow<List<InstallmentEntity>>

    @Query("SELECT SUM(amount) FROM installments WHERE dueDate = :date AND deletedAt IS NULL AND status = 'PAID'")
    fun observePaidOn(date: String): Flow<Long?>

    @Query("SELECT SUM(amount - paidAmount) FROM installments WHERE deletedAt IS NULL AND status IN ('UNPAID','PARTIAL','OVERDUE') AND dueDate < :today")
    fun observeOverdueAmount(today: String): Flow<Long?>

    @Query("SELECT SUM(paidAmount) FROM installments WHERE deletedAt IS NULL AND paidDate = :date")
    fun observeReceiptsOn(date: String): Flow<Long?>

    @Query("SELECT COUNT(*) FROM installments WHERE deletedAt IS NULL AND status IN ('UNPAID','PARTIAL','OVERDUE')")
    fun observeOpenCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM installments WHERE deletedAt IS NULL AND status = 'PAID' AND paidDate = :date")
    fun observePaidCountOn(date: String): Flow<Int>

    @Query("SELECT * FROM installments WHERE preFileId = :preFileId AND deletedAt IS NULL ORDER BY installmentNumber")
    suspend fun getByPreFileRaw(preFileId: String): List<InstallmentEntity>

    @Query("DELETE FROM installments WHERE preFileId = :preFileId")
    suspend fun deleteByPreFile(preFileId: String)

    @Query("UPDATE installments SET status = :status, paidAmount = :paidAmount, paidDate = :paidDate, updatedAt = :timestamp, syncState = 'PENDING_UPLOAD' WHERE id = :id")
    suspend fun markPaid(id: String, status: String, paidAmount: Long, paidDate: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE installments SET status = 'OVERDUE', syncState = 'PENDING_UPLOAD' WHERE deletedAt IS NULL AND status IN ('UNPAID','PARTIAL') AND dueDate < :today")
    suspend fun markOverdue(today: String)

    // --- آماده برای همگام‌سازی ---
    @Query("SELECT * FROM installments WHERE syncState != 'CLEAN'")
    suspend fun getPendingSync(): List<InstallmentEntity>

    @Query("SELECT * FROM installments WHERE updatedAt > :since")
    suspend fun getChangedSince(since: Long): List<InstallmentEntity>

    @Query("UPDATE installments SET syncState = :state, remoteId = COALESCE(:remoteId, remoteId) WHERE id = :id")
    suspend fun updateSyncState(id: String, state: String, remoteId: String?)

    @Query("UPDATE installments SET deletedAt = :timestamp, syncState = 'PENDING_DELETE', updatedAt = :timestamp WHERE id = :id")
    suspend fun softDelete(id: String, timestamp: Long = System.currentTimeMillis())
}
