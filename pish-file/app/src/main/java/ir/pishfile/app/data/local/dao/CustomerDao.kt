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

    @Query("SELECT * FROM customers WHERE deletedAt IS NULL ORDER BY name COLLATE LOCALIZED")
    fun observeAll(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE deletedAt IS NULL AND status = :status ORDER BY name COLLATE LOCALIZED")
    fun observeByStatus(status: String): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE preFileId = :preFileId AND deletedAt IS NULL ORDER BY createdAt DESC")
    fun observeByPreFile(preFileId: String): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE unitId = :unitId AND deletedAt IS NULL ORDER BY createdAt DESC")
    fun observeByUnit(unitId: String): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE id = :id")
    fun observeById(id: String): Flow<CustomerEntity?>

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getById(id: String): CustomerEntity?

    @Query("SELECT * FROM customers WHERE deletedAt IS NULL")
    suspend fun getAll(): List<CustomerEntity>

    @Query("SELECT COUNT(*) FROM customers WHERE deletedAt IS NULL")
    fun observeCount(): Flow<Int>

    @Query("UPDATE customers SET status = :status, updatedAt = :timestamp, syncState = 'PENDING_UPLOAD' WHERE id = :id")
    suspend fun markStatus(id: String, status: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE customers SET deletedAt = :timestamp, syncState = 'PENDING_DELETE', updatedAt = :timestamp WHERE id = :id")
    suspend fun softDelete(id: String, timestamp: Long = System.currentTimeMillis())
}
