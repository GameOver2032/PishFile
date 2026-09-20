package ir.pishfile.app.data.repository

import ir.pishfile.app.data.local.dao.CustomerDao
import ir.pishfile.app.data.local.entity.CustomerEntity
import kotlinx.coroutines.flow.Flow

class CustomerRepository(private val dao: CustomerDao) {

    fun observeAll(): Flow<List<CustomerEntity>> = dao.observeAll()

    fun search(query: String): Flow<List<CustomerEntity>> = dao.search(query.trim())

    fun observeById(id: String): Flow<CustomerEntity?> = dao.observeById(id)

    fun observeCount(): Flow<Int> = dao.observeCount()

    fun observeCountByStatus(status: String): Flow<Int> = dao.observeCountByStatus(status)

    fun observeWithUpcomingInstallments(untilDate: String): Flow<List<CustomerEntity>> =
        dao.observeWithUpcomingInstallments(untilDate)

    suspend fun getById(id: String): CustomerEntity? = dao.getById(id)

    suspend fun getAll(): List<CustomerEntity> = dao.getAll()

    suspend fun findByPhone(phone: String): CustomerEntity? = dao.findByPhone(phone)

    suspend fun findByNationalId(nationalId: String): CustomerEntity? = dao.findByNationalId(nationalId)

    suspend fun save(customer: CustomerEntity) {
        val existing = dao.getById(customer.id)
        if (existing == null) {
            dao.insert(customer)
        } else {
            dao.update(customer.copy(syncState = "PENDING_UPLOAD", updatedAt = System.currentTimeMillis()))
        }
    }

    suspend fun delete(id: String) = dao.softDelete(id)
}
