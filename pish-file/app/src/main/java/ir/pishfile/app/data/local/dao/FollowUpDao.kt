package ir.pishfile.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import ir.pishfile.app.data.local.entity.FollowUpEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FollowUpDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(followUp: FollowUpEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(followUps: List<FollowUpEntity>)

    @Update
    suspend fun update(followUp: FollowUpEntity)

    @Delete
    suspend fun delete(followUp: FollowUpEntity)

    @Query("SELECT * FROM follow_ups WHERE deletedAt IS NULL ORDER BY dueDate, dueTime")
    fun observeAll(): Flow<List<FollowUpEntity>>

    @Query("SELECT * FROM follow_ups WHERE status = 'PENDING' AND deletedAt IS NULL ORDER BY dueDate, priority DESC")
    fun observePending(): Flow<List<FollowUpEntity>>

    @Query("SELECT * FROM follow_ups WHERE customerId = :customerId AND deletedAt IS NULL ORDER BY dueDate DESC")
    fun observeByCustomer(customerId: String): Flow<List<FollowUpEntity>>

    @Query("SELECT * FROM follow_ups WHERE preFileId = :preFileId AND deletedAt IS NULL ORDER BY dueDate DESC")
    fun observeByPreFile(preFileId: String): Flow<List<FollowUpEntity>>

    @Query(
        """
        SELECT * FROM follow_ups
        WHERE deletedAt IS NULL AND status = 'PENDING'
          AND dueDate IS NOT NULL AND dueDate <= :untilDate
        ORDER BY dueDate, priority DESC
        """
    )
    fun observeDue(untilDate: String): Flow<List<FollowUpEntity>>

    @Query("SELECT * FROM follow_ups WHERE id = :id")
    suspend fun getById(id: String): FollowUpEntity?

    @Query("SELECT * FROM follow_ups WHERE deletedAt IS NULL")
    suspend fun getAll(): List<FollowUpEntity>

    @Query("SELECT COUNT(*) FROM follow_ups WHERE deletedAt IS NULL AND status = 'PENDING'")
    fun observePendingCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM follow_ups WHERE deletedAt IS NULL AND status = 'PENDING' AND dueDate IS NOT NULL AND dueDate <= :untilDate")
    fun observeDueCount(untilDate: String): Flow<Int>

    @Query("UPDATE follow_ups SET status = 'DONE', completedDate = :completedDate, outcome = :outcome, updatedAt = :timestamp, syncState = 'PENDING_UPLOAD' WHERE id = :id")
    suspend fun markDone(id: String, completedDate: String, outcome: String?, timestamp: Long = System.currentTimeMillis())

    // --- آماده برای همگام‌سازی ---
    @Query("SELECT * FROM follow_ups WHERE syncState != 'CLEAN'")
    suspend fun getPendingSync(): List<FollowUpEntity>

    @Query("SELECT * FROM follow_ups WHERE updatedAt > :since")
    suspend fun getChangedSince(since: Long): List<FollowUpEntity>

    @Query("UPDATE follow_ups SET syncState = :state, remoteId = COALESCE(:remoteId, remoteId) WHERE id = :id")
    suspend fun updateSyncState(id: String, state: String, remoteId: String?)

    @Query("UPDATE follow_ups SET deletedAt = :timestamp, syncState = 'PENDING_DELETE', updatedAt = :timestamp WHERE id = :id")
    suspend fun softDelete(id: String, timestamp: Long = System.currentTimeMillis())
}
