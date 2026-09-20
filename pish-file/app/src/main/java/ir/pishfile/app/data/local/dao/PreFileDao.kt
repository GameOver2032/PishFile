package ir.pishfile.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import ir.pishfile.app.data.local.entity.PreFileEntity
import kotlinx.coroutines.flow.Flow

/** ردیف پیش‌فایل همراه با نام پروژه/واحد/مشتری — برای نمایش در فهرست */
data class PreFileRow(
    @Embedded val preFile: PreFileEntity,
    val projectName: String?,
    val unitTitle: String?,
    val customerName: String?,
    val customerPhone: String?,
)

/** آمار مالی برای داشبورد */
data class FinanceSummary(
    val totalContractValue: Long?,
    val totalReceived: Long?,
    val totalRemaining: Long?,
    val contractCount: Int,
)

@Dao
interface PreFileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(preFile: PreFileEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(preFiles: List<PreFileEntity>)

    @Update
    suspend fun update(preFile: PreFileEntity)

    @Delete
    suspend fun delete(preFile: PreFileEntity)

    private companion object {
        const val ROW_SELECT = """
            SELECT p.*,
                   pr.name AS projectName,
                   CASE WHEN u.id IS NULL THEN NULL
                        ELSE (COALESCE(u.block || ' - ', '') || 'واحد ' || u.unitNumber ||
                              CASE WHEN u.floor IS NOT NULL THEN ' (طبقه ' || u.floor || ')' ELSE '' END)
                   END AS unitTitle,
                   CASE WHEN c.id IS NULL THEN NULL
                        ELSE (COALESCE(c.title || ' ', '') || c.firstName || ' ' || c.lastName)
                   END AS customerName,
                   c.phonePrimary AS customerPhone
            FROM pre_files p
            LEFT JOIN projects pr ON pr.id = p.projectId
            LEFT JOIN units u ON u.id = p.unitId
            LEFT JOIN customers c ON c.id = p.customerId
            WHERE p.deletedAt IS NULL
        """
    }

    @Query("$ROW_SELECT ORDER BY p.updatedAt DESC")
    fun observeAllRows(): Flow<List<PreFileRow>>

    @Query("$ROW_SELECT AND p.status = :status ORDER BY p.updatedAt DESC")
    fun observeRowsByStatus(status: String): Flow<List<PreFileRow>>

    @Query("$ROW_SELECT AND p.projectId = :projectId ORDER BY p.updatedAt DESC")
    fun observeRowsByProject(projectId: String): Flow<List<PreFileRow>>

    @Query("$ROW_SELECT AND p.customerId = :customerId ORDER BY p.updatedAt DESC")
    fun observeRowsByCustomer(customerId: String): Flow<List<PreFileRow>>

    @Query(
        """
        $ROW_SELECT AND (
            :query = '' OR p.draftNumber LIKE '%' || :query || '%'
            OR c.firstName LIKE '%' || :query || '%'
            OR c.lastName LIKE '%' || :query || '%'
            OR c.phonePrimary LIKE '%' || :query || '%'
            OR pr.name LIKE '%' || :query || '%'
            OR u.unitNumber LIKE '%' || :query || '%'
            OR p.trackingCode LIKE '%' || :query || '%'
        ) ORDER BY p.updatedAt DESC
        """
    )
    fun search(query: String): Flow<List<PreFileRow>>

    @Query("SELECT * FROM pre_files WHERE id = :id AND deletedAt IS NULL")
    fun observeById(id: String): Flow<PreFileEntity?>

    @Query("SELECT * FROM pre_files WHERE id = :id")
    suspend fun getById(id: String): PreFileEntity?

    @Query("$ROW_SELECT AND p.id = :id")
    fun observeRowById(id: String): Flow<PreFileRow?>

    @Query("SELECT * FROM pre_files WHERE deletedAt IS NULL")
    suspend fun getAll(): List<PreFileEntity>

    @Query("SELECT * FROM pre_files WHERE projectId = :projectId AND deletedAt IS NULL")
    suspend fun getByProject(projectId: String): List<PreFileEntity>

    @Query("SELECT * FROM pre_files WHERE customerId = :customerId AND deletedAt IS NULL")
    suspend fun getByCustomer(customerId: String): List<PreFileEntity>

    /**
     * تشخیص خودکار شماره پیش‌نویس بعدی: از آخرین شماره‌ی همان سال استفاده می‌کند.
     */
    @Query("SELECT COUNT(*) FROM pre_files WHERE draftNumber LIKE 'PF-' || :year || '%'")
    suspend fun countForYear(year: String): Int

    @Query(
        """
        SELECT COUNT(*) FROM pre_files WHERE draftNumber LIKE 'PF-' || :year || '%'
        """
    )
    fun observeCountForYear(year: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM pre_files WHERE deletedAt IS NULL")
    fun observeCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM pre_files WHERE deletedAt IS NULL AND status = :status")
    fun observeCountByStatus(status: String): Flow<Int>

    @Query("SELECT status, COUNT(*) as count FROM pre_files WHERE deletedAt IS NULL GROUP BY status")
    fun observeStatusCounts(): Flow<List<StatusCount>>

    /** ارجاع‌نشده‌ها (بدون واحد یا مشتری) — برای یادآوری تکمیل اطلاعات */
    @Query("SELECT * FROM pre_files WHERE deletedAt IS NULL AND (unitId IS NULL OR customerId IS NULL)")
    fun observeIncomplete(): Flow<List<PreFileEntity>>

    /** --- جمع‌ها و آمار مالی --- */
    @Query(
        """
        SELECT SUM(COALESCE(p.finalPrice, p.totalPrice - COALESCE(p.discount, 0))) AS totalContractValue,
               SUM(p.paidAmount) AS totalReceived,
               SUM(COALESCE(p.finalPrice, p.totalPrice - COALESCE(p.discount, 0)) - p.paidAmount) AS totalRemaining,
               COUNT(*) AS contractCount
        FROM pre_files p
        WHERE p.deletedAt IS NULL AND p.status IN ('CONFIRMED','PENDING_PAYMENT','COMPLETED','RESERVED')
        """
    )
    fun observeFinanceSummary(): Flow<FinanceSummary?>

    @Query(
        """
        SELECT SUM(COALESCE(p.finalPrice, p.totalPrice - COALESCE(p.discount, 0)) - p.paidAmount)
        FROM pre_files p
        WHERE p.deletedAt IS NULL AND p.status IN ('CONFIRMED','PENDING_PAYMENT','COMPLETED')
          AND (:projectId IS NULL OR p.projectId = :projectId)
        """
    )
    fun observeTotalReceivables(projectId: String? = null): Flow<Long?>

    @Query(
        """
        SELECT SUM(p.paidAmount) FROM pre_files p
        WHERE p.deletedAt IS NULL AND (:projectId IS NULL OR p.projectId = :projectId)
          AND p.draftDate >= :fromDate
        """
    )
    fun observeReceivedFrom(fromDate: String, projectId: String? = null): Flow<Long?>

    @Query(
        """
        SELECT SUM(COALESCE(p.finalPrice, p.totalPrice - COALESCE(p.discount, 0)))
        FROM pre_files p
        WHERE p.deletedAt IS NULL AND p.projectId = :projectId
          AND p.status IN ('CONFIRMED','COMPLETED','PENDING_PAYMENT')
        """
    )
    fun observeProjectSales(projectId: String): Flow<Long?>

    @Query(
        """
        SELECT pr.name AS status, COUNT(p.id) AS count
        FROM projects pr LEFT JOIN pre_files p ON p.projectId = pr.id AND p.deletedAt IS NULL
        WHERE pr.deletedAt IS NULL GROUP BY pr.id
        """
    )
    fun observeContractCountByProject(): Flow<List<StatusCount>>

    // --- آماده برای همگام‌سازی ---
    @Query("SELECT * FROM pre_files WHERE syncState != 'CLEAN'")
    suspend fun getPendingSync(): List<PreFileEntity>

    @Query("SELECT * FROM pre_files WHERE updatedAt > :since")
    suspend fun getChangedSince(since: Long): List<PreFileEntity>

    @Query("UPDATE pre_files SET syncState = :state, remoteId = COALESCE(:remoteId, remoteId) WHERE id = :id")
    suspend fun updateSyncState(id: String, state: String, remoteId: String?)

    @Query("UPDATE pre_files SET paidAmount = paidAmount + :amount, updatedAt = :timestamp, syncState = 'PENDING_UPLOAD' WHERE id = :id")
    suspend fun addPayment(id: String, amount: Long, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE pre_files SET deletedAt = :timestamp, syncState = 'PENDING_DELETE', updatedAt = :timestamp WHERE id = :id")
    suspend fun softDelete(id: String, timestamp: Long = System.currentTimeMillis())
}
