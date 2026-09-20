package ir.pishfile.app.data.repository

import ir.pishfile.app.core.Formatters
import ir.pishfile.app.data.local.dao.FollowUpDao
import ir.pishfile.app.data.local.entity.FollowUpEntity
import kotlinx.coroutines.flow.Flow

class FollowUpRepository(private val dao: FollowUpDao) {

    fun observeAll(): Flow<List<FollowUpEntity>> = dao.observeAll()

    fun observePending(): Flow<List<FollowUpEntity>> = dao.observePending()

    fun observeByCustomer(customerId: String): Flow<List<FollowUpEntity>> = dao.observeByCustomer(customerId)

    fun observeByPreFile(preFileId: String): Flow<List<FollowUpEntity>> = dao.observeByPreFile(preFileId)

    /** کارهای امروز و عقب‌افتاده */
    fun observeDueToday(): Flow<List<FollowUpEntity>> = dao.observeDue(Formatters.todayJalali())

    fun observeUpcoming(daysAhead: Int = 7): Flow<List<FollowUpEntity>> =
        dao.observeDue(Formatters.addJalaliDays(Formatters.todayJalali(), daysAhead))

    fun observePendingCount(): Flow<Int> = dao.observePendingCount()

    fun observeDueCount(): Flow<Int> = dao.observeDueCount(Formatters.todayJalali())

    suspend fun getById(id: String): FollowUpEntity? = dao.getById(id)

    suspend fun save(followUp: FollowUpEntity) {
        val existing = dao.getById(followUp.id)
        if (existing == null) dao.insert(followUp)
        else dao.update(followUp.copy(updatedAt = System.currentTimeMillis(), syncState = "PENDING_UPLOAD"))
    }

    suspend fun markDone(id: String, outcome: String? = null) {
        dao.markDone(id, Formatters.todayJalali(), outcome)
    }

    suspend fun delete(id: String) = dao.softDelete(id)
}
