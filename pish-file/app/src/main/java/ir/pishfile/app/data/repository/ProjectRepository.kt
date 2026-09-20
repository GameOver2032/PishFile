package ir.pishfile.app.data.repository

import ir.pishfile.app.data.local.dao.ProjectDao
import ir.pishfile.app.data.local.entity.ProjectEntity
import kotlinx.coroutines.flow.Flow

class ProjectRepository(private val dao: ProjectDao) {

    fun observeAll(): Flow<List<ProjectEntity>> = dao.observeAll()

    fun search(query: String): Flow<List<ProjectEntity>> = dao.search(query.trim())

    fun observeById(id: String): Flow<ProjectEntity?> = dao.observeById(id)

    fun observeCount(): Flow<Int> = dao.observeCount()

    suspend fun getById(id: String): ProjectEntity? = dao.getById(id)

    suspend fun getAll(): List<ProjectEntity> = dao.getAll()

    suspend fun save(project: ProjectEntity) {
        val existing = dao.getById(project.id)
        if (existing == null) {
            dao.insert(project.copy(syncState = SYNC_PENDING_UPLOAD))
        } else {
            dao.update(project.copy(syncState = SYNC_PENDING_UPLOAD, updatedAt = System.currentTimeMillis()))
        }
    }

    suspend fun delete(id: String) = dao.softDelete(id)

    companion object {
        const val SYNC_PENDING_UPLOAD = "PENDING_UPLOAD"
    }
}
