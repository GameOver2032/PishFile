package ir.pishfile.app.data.repository

import ir.pishfile.app.data.local.dao.CustomerDao
import ir.pishfile.app.data.local.entity.CustomerEntity
import kotlinx.coroutines.flow.Flow

class CustomerRepository(private val dao: CustomerDao) {

    fun observeAll(): Flow<List<CustomerEntity>> = dao.observeAll()
    fun observeByStatus(status: String): Flow<List<CustomerEntity>> = dao.observeByStatus(status)
    fun observeByPreFile(preFileId: String): Flow<List<CustomerEntity>> = dao.observeByPreFile(preFileId)
    fun observeByUnit(unitId: String): Flow<List<CustomerEntity>> = dao.observeByUnit(unitId)
    fun observeById(id: String): Flow<CustomerEntity?> = dao.observeById(id)
    fun observeCount(): Flow<Int> = dao.observeCount()

    suspend fun getById(id: String): CustomerEntity? = dao.getById(id)
    suspend fun getAll(): List<CustomerEntity> = dao.getAll()

    suspend fun save(customer: CustomerEntity) {
        dao.insert(customer.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun markStatus(id: String, status: String) = dao.markStatus(id, status)

    suspend fun delete(id: String) = dao.softDelete(id)
}
