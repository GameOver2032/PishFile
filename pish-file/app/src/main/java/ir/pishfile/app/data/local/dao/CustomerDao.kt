package ir.pishfile.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import ir.pishfile.app.data.local.entity.CustomerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(customer: CustomerEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(customers: List<CustomerEntity>)

    @Update
    suspend fun update(customer: CustomerEntity)

    @Delete
    suspend fun delete(customer: CustomerEntity)

    @Query("SELECT * FROM customers WHERE deletedAt IS NULL ORDER BY isFavorite DESC, updatedAt DESC")
    fun observeAll(): Flow<List<CustomerEntity>>

    @Query(
        """
        SELECT * FROM customers WHERE deletedAt IS NULL AND (
            :query = '' OR firstName LIKE '%' || :query || '%'
            OR lastName LIKE '%' || :query || '%'
            OR nationalId LIKE '%' || :query || '%'
            OR phonePrimary LIKE '%' || :query || '%'
            OR phoneSecondary LIKE '%' || :query || '%'
            OR companyName LIKE '%' || :query || '%'
        ) ORDER BY isFavorite DESC, updatedAt DESC
        """
    )
    fun search(query: String): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE status = :status AND deletedAt IS NULL ORDER BY updatedAt DESC")
    fun observeByStatus(status: String): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE id = :id")
    fun observeById(id: String): Flow<CustomerEntity?>

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getById(id: String): CustomerEntity?

    @Query("SELECT * FROM customers WHERE deletedAt IS NULL ORDER BY lastName")
    suspend fun getAll(): List<CustomerEntity>

    @Query("SELECT DISTINCT * FROM customers WHERE deletedAt IS NULL ORDER BY firstName")
    suspend fun getAllDistinct(): List<CustomerEntity>

    @Query("SELECT COUNT(*) FROM customers WHERE deletedAt IS NULL")
    fun observeCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM customers WHERE deletedAt IS NULL AND status = :status")
    fun observeCountByStatus(status: String): Flow<Int>

    @Query("SELECT * FROM customers WHERE phonePrimary = :phone AND deletedAt IS NULL LIMIT 1")
    suspend fun findByPhone(phone: String): CustomerEntity?

    @Query("SELECT * FROM customers WHERE nationalId = :nationalId AND deletedAt IS NULL LIMIT 1")
    suspend fun findByNationalId(nationalId: String): CustomerEntity?

    /** مشتریانی که قسط سررسید نزدیک دارند */
    @Query(
        """
        SELECT DISTINCT c.* FROM customers c
        INNER JOIN pre_files p ON p.customerId = c.id
        INNER JOIN installments i ON i.preFileId = p.id
        WHERE i.status IN ('UNPAID','PARTIAL','OVERDUE') AND i.dueDate <= :untilDate AND i.deletedAt IS NULL
        ORDER BY c.lastName
        """
    )
    fun observeWithUpcomingInstallments(untilDate: String): Flow<List<CustomerEntity>>

    @Query("UPDATE customers SET syncState = :state, remoteId = COALESCE(:remoteId, remoteId) WHERE id = :id")
    suspend fun updateSyncState(id: String, state: String, remoteId: String?)

    @Query("SELECT * FROM customers WHERE syncState != 'CLEAN'")
    suspend fun getPendingSync(): List<CustomerEntity>

    @Query("SELECT * FROM customers WHERE updatedAt > :since")
    suspend fun getChangedSince(since: Long): List<CustomerEntity>

    @Query("UPDATE customers SET deletedAt = :timestamp, syncState = 'PENDING_DELETE', updatedAt = :timestamp WHERE id = :id")
    suspend fun softDelete(id: String, timestamp: Long = System.currentTimeMillis())
}
