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

/** ردیف پیش‌فروش همراه با نام پروژه و واحد برای نمایش در لیست */
data class PreFileRow(
    @Embedded val preFile: PreFileEntity,
    val projectName: String?,
    val unitTitle: String?,
    val projectPricingModel: String?,
    val projectDefaultDeposit: Long?,
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
                   pr.pricingModel AS projectPricingModel,
                   pr.default_deposit_amount AS projectDefaultDeposit,
                   CASE WHEN u.id IS NULL THEN NULL
                        ELSE (COALESCE(u.block || ' - ', '') || 'واحد ' || u.unitNumber ||
                              CASE WHEN u.floor IS NOT NULL THEN ' (طبقه ' || u.floor || ')' ELSE '' END)
                   END AS unitTitle
            FROM pre_files p
            LEFT JOIN projects pr ON pr.id = p.projectId
            LEFT JOIN units u ON u.id = p.unitId
            WHERE p.deletedAt IS NULL
        """
    }

    @Query("$ROW_SELECT ORDER BY p.updatedAt DESC")
    fun observeAllRows(): Flow<List<PreFileRow>>

    @Query("$ROW_SELECT AND p.status = :status ORDER BY p.updatedAt DESC")
    fun observeRowsByStatus(status: String): Flow<List<PreFileRow>>

    @Query("$ROW_SELECT AND p.projectId = :projectId ORDER BY p.updatedAt DESC")
    fun observeRowsByProject(projectId: String): Flow<List<PreFileRow>>

    @Query(
        """
        $ROW_SELECT AND (
            :query = '' OR p.draftNumber LIKE '%' || :query || '%'
            OR p.ownerName LIKE '%' || :query || '%'
            OR p.ownerPhone LIKE '%' || :query || '%'
            OR pr.name LIKE '%' || :query || '%'
            OR u.unitNumber LIKE '%' || :query || '%'
            OR p.notes LIKE '%' || :query || '%'
            OR p.ranking LIKE '%' || :query || '%'
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

    @Query("SELECT COUNT(*) FROM pre_files WHERE draftNumber LIKE 'PF-' || :year || '%'")
    suspend fun countForYear(year: String): Int

    @Query("SELECT COUNT(*) FROM pre_files WHERE deletedAt IS NULL")
    fun observeCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM pre_files WHERE deletedAt IS NULL AND status = :status")
    fun observeCountByStatus(status: String): Flow<Int>

    @Query("SELECT status, COUNT(*) as count FROM pre_files WHERE deletedAt IS NULL GROUP BY status")
    fun observeStatusCounts(): Flow<List<StatusCount>>

    @Query("$ROW_SELECT ORDER BY p.createdAt DESC LIMIT 1")
    fun observeLatest(): Flow<PreFileRow?>

    @Query("SELECT * FROM pre_files WHERE syncState != 'CLEAN'")
    suspend fun getPendingSync(): List<PreFileEntity>

    @Query("SELECT * FROM pre_files WHERE updatedAt > :since")
    suspend fun getChangedSince(since: Long): List<PreFileEntity>

    @Query("UPDATE pre_files SET syncState = :state, remoteId = COALESCE(:remoteId, remoteId) WHERE id = :id")
    suspend fun updateSyncState(id: String, state: String, remoteId: String?)

    @Query("UPDATE pre_files SET deletedAt = :timestamp, syncState = 'PENDING_DELETE', updatedAt = :timestamp WHERE id = :id")
    suspend fun softDelete(id: String, timestamp: Long = System.currentTimeMillis())
}
