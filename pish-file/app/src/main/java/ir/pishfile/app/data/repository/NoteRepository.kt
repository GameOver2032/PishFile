package ir.pishfile.app.data.repository

import ir.pishfile.app.data.local.dao.NoteDao
import ir.pishfile.app.data.local.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

class NoteRepository(private val dao: NoteDao) {

    fun observeAll(): Flow<List<NoteEntity>> = dao.observeAll()
    fun observeByPreFile(preFileId: String): Flow<List<NoteEntity>> = dao.observeByPreFile(preFileId)
    fun observeByCustomer(customerId: String): Flow<List<NoteEntity>> = dao.observeByCustomer(customerId)
    fun observeCount(): Flow<Int> = dao.observeCount()

    suspend fun getById(id: String): NoteEntity? = dao.getById(id)
    suspend fun getAll(): List<NoteEntity> = dao.getAll()

    suspend fun save(note: NoteEntity) {
        dao.insert(note.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun delete(id: String) = dao.softDelete(id)
}
