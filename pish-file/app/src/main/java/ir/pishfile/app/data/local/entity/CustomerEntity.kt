package ir.pishfile.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * مشتری / طرف‌مذاکره‌ی فایل پیش‌فروش (خریدار، فروشنده یا سایر).
 *
 * یک مشتری می‌تواند به یک فایل پیش‌فروش و/یا یک واحد وصل شود؛
 * با حذف فایل یا واحد، پیوند از بین می‌رود ولی خود مشتری حفظ می‌ماند.
 */
@Entity(
    tableName = "customers",
    foreignKeys = [
        ForeignKey(
            entity = PreFileEntity::class,
            parentColumns = ["id"],
            childColumns = ["preFileId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = UnitEntity::class,
            parentColumns = ["id"],
            childColumns = ["unitId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("preFileId"), Index("unitId"), Index("status"), Index("syncState")
    ]
)
data class CustomerEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    /** نام و نام خانوادگی مشتری */
    val name: String,

    /** شماره تماس */
    val phone: String? = null,

    /** نقش: BUYER (خریدار) / SELLER (فروشنده) / OTHER (سایر) */
    val role: String = "BUYER",

    /** فایل پیش‌فروش مربوطه (در صورت وجود) */
    @ColumnInfo(name = "preFileId")
    val preFileId: String? = null,

    /** واحد مربوطه (در صورت وجود) */
    @ColumnInfo(name = "unitId")
    val unitId: String? = null,

    /** یادداشت کلی درباره‌ی مشتری */
    val notes: String? = null,

    /** وضعیت: ACTIVE (در مذاکره) / DONE (نهایی شد) / LOST (رد شد) */
    val status: String = "ACTIVE",

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
