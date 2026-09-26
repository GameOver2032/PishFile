package ir.pishfile.app.data.repository

import ir.pishfile.app.data.local.dao.ProjectAreaDao
import ir.pishfile.app.data.local.entity.ProjectAreaEntity
import kotlinx.coroutines.flow.Flow

class ProjectAreaRepository(private val dao: ProjectAreaDao) {

    fun observeByProject(projectId: String): Flow<List<ProjectAreaEntity>> = dao.observeByProject(projectId)

    fun observeCountByProject(projectId: String): Flow<Int> = dao.observeCountByProject(projectId)

    suspend fun getByProject(projectId: String): List<ProjectAreaEntity> = dao.getByProject(projectId)

    suspend fun getById(id: String): ProjectAreaEntity? = dao.getById(id)

    suspend fun save(area: ProjectAreaEntity) {
        if (dao.getById(area.id) == null) dao.insert(area) else dao.update(area)
    }

    suspend fun delete(area: ProjectAreaEntity) = dao.delete(area)

    suspend fun deleteByProject(projectId: String) = dao.deleteByProject(projectId)
}
