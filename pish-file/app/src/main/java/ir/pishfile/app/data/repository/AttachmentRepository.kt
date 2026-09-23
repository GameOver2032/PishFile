package ir.pishfile.app.data.repository

import ir.pishfile.app.data.local.dao.AttachmentDao
import ir.pishfile.app.data.local.entity.AttachmentEntity
import kotlinx.coroutines.flow.Flow

class AttachmentRepository(private val dao: AttachmentDao) {

    fun observeByOwner(ownerType: String, ownerId: String): Flow<List<AttachmentEntity>> =
        dao.observeByOwner(ownerType, ownerId)

    suspend fun getById(id: String): AttachmentEntity? = dao.getById(id)

    suspend fun save(attachment: AttachmentEntity) = dao.insert(attachment)

    suspend fun delete(attachment: AttachmentEntity) = dao.delete(attachment)

    suspend fun deleteByOwner(ownerType: String, ownerId: String) = dao.deleteByOwner(ownerType, ownerId)
}
