package ir.pishfile.app.data.repository

import ir.pishfile.app.core.Formatters
import ir.pishfile.app.data.local.dao.FollowUpDao
import ir.pishfile.app.data.local.entity.FollowUpEntity
import kotlinx.coroutines.flow.Flow

class FollowUpRepository(private val dao: FollowUpDao) {

    fun observeAll(): Flow<List<FollowUpEntity>> = dao.observeAll()
    fun observePending(): Flow<List<FollowUpEntity>> = dao.observePending()
    fun observeDueToday(): Flow<List<FollowUpEntity>> = dao.observeDueToday(Formatters.todayJalali())
    fun observeByPreFile(preFileId: String): Flow<List<FollowUpEntity>> = dao.observeByPreFile(preFileId)

    suspend fun getById(id: String): FollowUpEntity? = dao.getById(id)
    suspend fun getAll(): List<FollowUpEntity> = dao.getAll()

    suspend fun save(followUp: FollowUpEntity) {
        val toSave = followUp.copy(
            updatedAt = System.currentTimeMillis(),
            syncState = "PENDING_UPLOAD",
        )
        if (dao.getById(followUp.id) == null) dao.insert(toSave) else dao.update(toSave)
    }

    suspend fun markDone(id: String, outcome: String? = null) {
        dao.markDone(id, outcome)
    }

    suspend fun delete(id: String) = dao.softDelete(id)
}
