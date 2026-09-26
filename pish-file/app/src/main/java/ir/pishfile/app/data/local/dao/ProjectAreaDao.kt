package ir.pishfile.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import ir.pishfile.app.data.local.entity.ProjectAreaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectAreaDao {

    @Query("SELECT * FROM project_areas WHERE projectId = :projectId ORDER BY sortIndex, id")
    fun observeByProject(projectId: String): Flow<List<ProjectAreaEntity>>

    @Query("SELECT * FROM project_areas WHERE projectId = :projectId ORDER BY sortIndex, id")
    suspend fun getByProject(projectId: String): List<ProjectAreaEntity>

    @Query("SELECT * FROM project_areas WHERE id = :id")
    suspend fun getById(id: String): ProjectAreaEntity?

    @Query("SELECT * FROM project_areas")
    suspend fun getAll(): List<ProjectAreaEntity>

    @Query("SELECT COUNT(*) FROM project_areas WHERE projectId = :projectId")
    fun observeCountByProject(projectId: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(area: ProjectAreaEntity): Long

    @Update
    suspend fun update(area: ProjectAreaEntity)

    @Delete
    suspend fun delete(area: ProjectAreaEntity)

    @Query("DELETE FROM project_areas WHERE projectId = :projectId")
    suspend fun deleteByProject(projectId: String)
}
