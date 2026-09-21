package ir.pishfile.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import ir.pishfile.app.core.Formatters
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

    @Query("SELECT * FROM follow_ups WHERE deletedAt IS NULL AND status = 'PENDING' ORDER BY dueDate, dueTime")
    fun observePending(): Flow<List<FollowUpEntity>>

    @Query("SELECT * FROM follow_ups WHERE deletedAt IS NULL AND status = 'PENDING' AND dueDate <= :today ORDER BY dueDate, dueTime")
    fun observeDueToday(today: String = Formatters.todayJalali()): Flow<List<FollowUpEntity>>

    @Query("SELECT * FROM follow_ups WHERE preFileId = :preFileId AND deletedAt IS NULL ORDER BY dueDate")
    fun observeByPreFile(preFileId: String): Flow<List<FollowUpEntity>>

    @Query("SELECT * FROM follow_ups WHERE id = :id")
    suspend fun getById(id: String): FollowUpEntity?

    @Query("SELECT * FROM follow_ups WHERE deletedAt IS NULL")
    suspend fun getAll(): List<FollowUpEntity>

    @Query("UPDATE follow_ups SET status = 'DONE', completedDate = :date, outcome = :outcome, updatedAt = :timestamp, syncState = 'PENDING_UPLOAD' WHERE id = :id")
    suspend fun markDone(id: String, outcome: String? = null, date: String = Formatters.todayJalali(), timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE follow_ups SET deletedAt = :timestamp, syncState = 'PENDING_DELETE', updatedAt = :timestamp WHERE id = :id")
    suspend fun softDelete(id: String, timestamp: Long = System.currentTimeMillis())
}
