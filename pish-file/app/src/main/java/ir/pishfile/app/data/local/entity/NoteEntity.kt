package ir.pishfile.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import ir.pishfile.app.core.Formatters
import java.util.UUID

/**
 * نوت/مکالمه‌ی تاریخ‌دار: ثبت پیگیری و نتیجه‌ی مکالمات با مشتری یا فروشنده.
 *
 * هر نوت می‌تواند به یک فایل پیش‌فروش و/یا یک مشتری وصل باشد
 * (تایم‌لاین مکالمات روی صفحه‌ی همان فایل یا مشتری نمایش داده می‌شود).
 */
@Entity(
    tableName = "notes",
    foreignKeys = [
        ForeignKey(
            entity = PreFileEntity::class,
            parentColumns = ["id"],
            childColumns = ["preFileId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CustomerEntity::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("preFileId"), Index("customerId"), Index("noteDate"), Index("syncState")
    ]
)
data class NoteEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    /** فایل پیش‌فروش مربوطه (در صورت وجود) */
    @ColumnInfo(name = "preFileId")
    val preFileId: String? = null,

    /** مشتری مربوطه (در صورت وجود) */
    @ColumnInfo(name = "customerId")
    val customerId: String? = null,

    /** نوع مکالمه: CALL (تلفنی) / VISIT (حضوری) / MESSAGE (پیامکی) / OTHER */
    val type: String = "CALL",

    /** متن نوت / شرح مکالمه */
    val text: String,

    /** نتیجه مکالمه (مثلاً: قرار بازدید، اعلام قیمت، انصراف و…) */
    val outcome: String? = null,

    /** تاریخ نوت (شمسی، پیش‌فرض امروز) */
    @ColumnInfo(name = "noteDate")
    val noteDate: String = Formatters.todayJalali(),

    @ColumnInfo(name = "createdAt")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updatedAt")
    val updatedAt: Long = System.currentTimeMillis(),

    // --- فیلدهای همگام‌سازی ---
    @ColumnInfo(name = "remoteId")
    val remoteId: String? = null,

    @ColumnInfo(name = "syncState")
    val syncState: String = "PENDING_UPLOAD",

    @ColumnInfo(name = "serverUpdatedAt")
    val serverUpdatedAt: Long? = null,

    @ColumnInfo(name = "deletedAt")
    val deletedAt: Long? = null,
)
