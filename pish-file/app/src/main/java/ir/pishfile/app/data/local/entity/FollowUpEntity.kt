package ir.pishfile.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * پیگیری — تماس، بازدید، جلسه یا یادآوری مربوط به مشتری/پیش‌فایل.
 */
@Entity(
    tableName = "follow_ups",
    foreignKeys = [
        ForeignKey(
            entity = CustomerEntity::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PreFileEntity::class,
            parentColumns = ["id"],
            childColumns = ["preFileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("customerId"), Index("preFileId"), Index("dueDate"),
        Index("status"), Index("remoteId"), Index("syncState")
    ]
)
data class FollowUpEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    /** نوع: CALL تماس، VISIT بازدید، MEETING جلسه، MESSAGE پیام، REMINDER یادآوری */
    val type: String = "CALL",

    /** اولویت: LOW / NORMAL / HIGH / URGENT */
    val priority: String = "NORMAL",

    /** عنوان کوتاه */
    val title: String,

    /** شرح */
    val description: String? = null,

    /** نتیجه‌ی پیگیری برای موارد انجام‌شده */
    @ColumnInfo(name = "outcome")
    val outcome: String? = null,

    /** نتیجه مثبت/منفی/بی‌نتیجه */
    @ColumnInfo(name = "result")
    val result: String? = null,

    @ColumnInfo(name = "customerId")
    val customerId: String? = null,

    @ColumnInfo(name = "preFileId")
    val preFileId: String? = null,

    @ColumnInfo(name = "projectId")
    val projectId: String? = null,

    /** تاریخ انجام (شمسی) */
    @ColumnInfo(name = "dueDate")
    val dueDate: String? = null,

    /** ساعت */
    @ColumnInfo(name = "dueTime")
    val dueTime: String? = null,

    /** مدت زمان صرف‌شده (دقیقه) */
    @ColumnInfo(name = "durationMinutes")
    val durationMinutes: Int? = null,

    /** PENDING / DONE / CANCELED */
    val status: String = "PENDING",

    /** تاریخ انجام واقعی */
    @ColumnInfo(name = "completedDate")
    val completedDate: String? = null,

    /** مسئول پیگیری */
    @ColumnInfo(name = "assignee")
    val assignee: String? = null,

    /** شماره تماس هر دو طرف برای گزارش */
    @ColumnInfo(name = "contactPhone")
    val contactPhone: String? = null,

    /** یادآوری چند روز قبل */
    @ColumnInfo(name = "remindDaysBefore")
    val remindDaysBefore: Int = 0,

    // --- میدان‌های آماده برای همگام‌سازی ---
    @ColumnInfo(name = "remoteId")
    val remoteId: String? = null,

    @ColumnInfo(name = "syncState")
    val syncState: String = "PENDING_UPLOAD",

    @ColumnInfo(name = "createdAt")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updatedAt")
    val updatedAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "serverUpdatedAt")
    val serverUpdatedAt: Long? = null,

    @ColumnInfo(name = "deletedAt")
    val deletedAt: Long? = null,
)
