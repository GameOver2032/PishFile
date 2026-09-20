package ir.pishfile.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import ir.pishfile.app.data.local.entity.UnitEntity
import kotlinx.coroutines.flow.Flow

/** خلاصه وضعیت واحدها — برای نمودار و آمار */
data class StatusCount(
    val status: String,
    val count: Int,
)

@Dao
interface UnitDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(unit: UnitEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(units: List<UnitEntity>)

    @Update
    suspend fun update(unit: UnitEntity)

    @Delete
    suspend fun delete(unit: UnitEntity)

    @Query("SELECT * FROM units WHERE deletedAt IS NULL ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<UnitEntity>>

    @Query("SELECT * FROM units WHERE projectId = :projectId AND deletedAt IS NULL ORDER BY floor, unitNumber")
    fun observeByProject(projectId: String): Flow<List<UnitEntity>>

    @Query(
        """
        SELECT * FROM units WHERE deletedAt IS NULL AND (
            :query = '' OR unitNumber LIKE '%' || :query || '%'
            OR block LIKE '%' || :query || '%' OR parkingNumber LIKE '%' || :query || '%'
        ) ORDER BY updatedAt DESC
        """
    )
    fun search(query: String): Flow<List<UnitEntity>>

    @Query("SELECT * FROM units WHERE id = :id")
    fun observeById(id: String): Flow<UnitEntity?>

    @Query("SELECT * FROM units WHERE id = :id")
    suspend fun getById(id: String): UnitEntity?

    @Query("SELECT * FROM units WHERE projectId = :projectId AND deletedAt IS NULL")
    suspend fun getByProject(projectId: String): List<UnitEntity>

    @Query("SELECT * FROM units WHERE deletedAt IS NULL")
    suspend fun getAll(): List<UnitEntity>

    @Query("SELECT COUNT(*) FROM units WHERE deletedAt IS NULL")
    fun observeCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM units WHERE projectId = :projectId AND deletedAt IS NULL")
    fun observeCountByProject(projectId: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM units WHERE deletedAt IS NULL AND status = :status")
    fun observeCountByStatus(status: String): Flow<Int>

    @Query("SELECT status, COUNT(*) as count FROM units WHERE deletedAt IS NULL AND (:projectId IS NULL OR projectId = :projectId) GROUP BY status")
    fun observeStatusCounts(projectId: String? = null): Flow<List<StatusCount>>

    /** واحدهای آزاد با فیلتر متراژ و بودجه */
    @Query(
        """
        SELECT * FROM units WHERE deletedAt IS NULL AND status = 'AVAILABLE'
        AND (:minArea IS NULL OR grossArea >= :minArea)
        AND (:maxArea IS NULL OR grossArea <= :maxArea)
        AND (:maxPrice IS NULL OR COALESCE(finalPrice, totalPrice, 0) <= :maxPrice)
        ORDER BY totalPrice
        """
    )
    fun findAvailable(minArea: Double?, maxArea: Double?, maxPrice: Long?): Flow<List<UnitEntity>>

    @Query("SELECT AVG(COALESCE(finalPrice, totalPrice)) FROM units WHERE projectId = :projectId AND deletedAt IS NULL")
    fun observeAveragePrice(projectId: String): Flow<Double?>

    @Query("SELECT SUM(grossArea) FROM units WHERE projectId = :projectId AND deletedAt IS NULL")
    fun observeTotalArea(projectId: String): Flow<Double?>

    @Query("UPDATE units SET status = :status, updatedAt = :timestamp, syncState = 'PENDING_UPLOAD' WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE units SET syncState = :state, remoteId = COALESCE(:remoteId, remoteId) WHERE id = :id")
    suspend fun updateSyncState(id: String, state: String, remoteId: String?)

    @Query("SELECT * FROM units WHERE syncState != 'CLEAN'")
    suspend fun getPendingSync(): List<UnitEntity>

    @Query("SELECT * FROM units WHERE updatedAt > :since")
    suspend fun getChangedSince(since: Long): List<UnitEntity>

    @Query("UPDATE units SET deletedAt = :timestamp, syncState = 'PENDING_DELETE', updatedAt = :timestamp WHERE id = :id")
    suspend fun softDelete(id: String, timestamp: Long = System.currentTimeMillis())
}
