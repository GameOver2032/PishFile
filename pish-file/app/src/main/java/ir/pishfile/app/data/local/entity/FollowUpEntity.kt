package ir.pishfile.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * یادداشت یا رویداد پیگیری (تماس، جلسه، یادآوری).
 */
@Entity(
    tableName = "follow_ups",
    foreignKeys = [
        ForeignKey(
            entity = PreFileEntity::class,
            parentColumns = ["id"],
            childColumns = ["preFileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("preFileId"), Index("dueDate"), Index("status"), Index("syncState")
    ]
)
data class FollowUpEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    /** نوع: CALL / VISIT / MEETING / OTHER */
    val type: String = "CALL",

    /** اولویت: LOW / NORMAL / HIGH */
    val priority: String = "NORMAL",

    /** عنوان/موضوع */
    val title: String,

    /** شرح */
    val description: String? = null,

    /** نتیجه پیگیری */
    val outcome: String? = null,

    val result: String? = null,

    /** شناسه فایل پیش‌فروش مربوطه */
    @ColumnInfo(name = "preFileId")
    val preFileId: String? = null,

    /** شناسه پروژه */
    @ColumnInfo(name = "projectId")
    val projectId: String? = null,

    /** تاریخ سررسید (شمسی) */
    @ColumnInfo(name = "dueDate")
    val dueDate: String? = null,

    /** ساعت سررسید */
    @ColumnInfo(name = "dueTime")
    val dueTime: String? = null,

    @ColumnInfo(name = "durationMinutes")
    val durationMinutes: Int? = null,

    /** PENDING / DONE / CANCELED */
    val status: String = "PENDING",

    @ColumnInfo(name = "completedDate")
    val completedDate: String? = null,

    /** مسئول پیگیری */
    val assignee: String? = null,

    /** شماره تماس */
    @ColumnInfo(name = "contactPhone")
    val contactPhone: String? = null,

    @ColumnInfo(name = "remindDaysBefore")
    val remindDaysBefore: Int = 0,

    // --- فیلدهای همگام‌سازی ---
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
