package ir.pishfile.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import ir.pishfile.app.data.local.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: NoteEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notes: List<NoteEntity>)

    @Update
    suspend fun update(note: NoteEntity)

    @Delete
    suspend fun delete(note: NoteEntity)

    @Query("SELECT * FROM notes WHERE deletedAt IS NULL ORDER BY noteDate DESC, createdAt DESC")
    fun observeAll(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE preFileId = :preFileId AND deletedAt IS NULL ORDER BY noteDate DESC, createdAt DESC")
    fun observeByPreFile(preFileId: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE customerId = :customerId AND deletedAt IS NULL ORDER BY noteDate DESC, createdAt DESC")
    fun observeByCustomer(customerId: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getById(id: String): NoteEntity?

    @Query("SELECT * FROM notes WHERE deletedAt IS NULL")
    suspend fun getAll(): List<NoteEntity>

    @Query("SELECT COUNT(*) FROM notes WHERE deletedAt IS NULL")
    fun observeCount(): Flow<Int>

    @Query("UPDATE notes SET deletedAt = :timestamp, syncState = 'PENDING_DELETE', updatedAt = :timestamp WHERE id = :id")
    suspend fun softDelete(id: String, timestamp: Long = System.currentTimeMillis())
}
