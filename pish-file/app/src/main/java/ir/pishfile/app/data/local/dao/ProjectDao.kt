package ir.pishfile.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import ir.pishfile.app.data.local.entity.ProjectEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(project: ProjectEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(projects: List<ProjectEntity>)

    @Update
    suspend fun update(project: ProjectEntity)

    @Delete
    suspend fun delete(project: ProjectEntity)

    @Query("SELECT * FROM projects WHERE deletedAt IS NULL ORDER BY isFavorite DESC, updatedAt DESC")
    fun observeAll(): Flow<List<ProjectEntity>>

    @Query(
        """
        SELECT * FROM projects
        WHERE deletedAt IS NULL AND (
            :query = '' OR name LIKE '%' || :query || '%'
            OR code LIKE '%' || :query || '%'
            OR city LIKE '%' || :query || '%'
            OR district LIKE '%' || :query || '%'
        )
        ORDER BY isFavorite DESC, updatedAt DESC
        """
    )
    fun search(query: String): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :id AND deletedAt IS NULL")
    fun observeById(id: String): Flow<ProjectEntity?>

    @Query("SELECT * FROM projects WHERE id = :id")
    suspend fun getById(id: String): ProjectEntity?

    @Query("SELECT * FROM projects WHERE deletedAt IS NULL ORDER BY name")
    suspend fun getAll(): List<ProjectEntity>

    @Query("SELECT COUNT(*) FROM projects WHERE deletedAt IS NULL")
    fun observeCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM projects WHERE deletedAt IS NULL AND isArchived = 0")
    fun observeActiveCount(): Flow<Int>

    @Query("SELECT * FROM projects WHERE isFavorite = 1 AND deletedAt IS NULL ORDER BY updatedAt DESC LIMIT :limit")
    fun observeFavorites(limit: Int = 5): Flow<List<ProjectEntity>>

    // --- آماده برای همگام‌سازی ---
    @Query("SELECT * FROM projects WHERE syncState != 'CLEAN'")
    suspend fun getPendingSync(): List<ProjectEntity>

    @Query("SELECT * FROM projects WHERE updatedAt > :since")
    suspend fun getChangedSince(since: Long): List<ProjectEntity>

    @Query("UPDATE projects SET syncState = :state, remoteId = COALESCE(:remoteId, remoteId) WHERE id = :id")
    suspend fun updateSyncState(id: String, state: String, remoteId: String?)

    @Query("UPDATE projects SET deletedAt = :timestamp, syncState = 'PENDING_DELETE', updatedAt = :timestamp WHERE id = :id")
    suspend fun softDelete(id: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE projects SET isFavorite = :favorite, updatedAt = :timestamp, syncState = 'PENDING_UPLOAD' WHERE id = :id")
    suspend fun setFavorite(id: String, favorite: Boolean, timestamp: Long = System.currentTimeMillis())
    
    @Query("DELETE FROM projects WHERE remoteId IS NOT NULL AND deletedAt IS NOT NULL")
    suspend fun purgeSyncedDeleted()
}
