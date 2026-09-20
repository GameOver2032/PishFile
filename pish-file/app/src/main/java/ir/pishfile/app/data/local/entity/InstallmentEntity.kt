package ir.pishfile.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * قسط / سررسید پرداخت مربوط به یک پیش‌فایل.
 */
@Entity(
    tableName = "installments",
    foreignKeys = [
        ForeignKey(
            entity = PreFileEntity::class,
            parentColumns = ["id"],
            childColumns = ["preFileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("preFileId"), Index("status"), Index("dueDate"), Index("remoteId"), Index("syncState")
    ]
)
data class InstallmentEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "preFileId")
    val preFileId: String,

    /** شماره قسط (۱ تا n) */
    @ColumnInfo(name = "installmentNumber")
    val installmentNumber: Int,

    /** عنوان قسط: «قسط ۳» یا «پیش‌پرداخت دوم» */
    val title: String? = null,

    /** مبلغ قسط */
    val amount: Long,

    /** تاریخ سررسید (شمسی) */
    @ColumnInfo(name = "dueDate")
    val dueDate: String,

    /** UNPAID / PAID / OVERDUE / PARTIAL */
    val status: String = "UNPAID",

    /** مبلغ پرداخت‌شده (برای پرداخت جزئی) */
    @ColumnInfo(name = "paidAmount")
    val paidAmount: Long = 0,

    /** تاریخ پرداخت واقعی (شمسی) */
    @ColumnInfo(name = "paidDate")
    val paidDate: String? = null,

    /** روش پرداخت: نقد، چک، کارت‌به‌کارت، حواله */
    @ColumnInfo(name = "paymentMethod")
    val paymentMethod: String? = null,

    /** شماره چک / پیگیری تراکنش */
    @ColumnInfo(name = "referenceNumber")
    val referenceNumber: String? = null,

    /** نام بانک */
    @ColumnInfo(name = "bankName")
    val bankName: String? = null,

    /** نام صادرکننده چک */
    @ColumnInfo(name = "chequeOwner")
    val chequeOwner: String? = null,

    /** تاریخ چک (ممکن است با سررسید متفاوت باشد) */
    @ColumnInfo(name = "chequeDate")
    val chequeDate: String? = null,

    /** مبلغ جریمه دیرکرد */
    @ColumnInfo(name = "latePenalty")
    val latePenalty: Long? = null,

    val notes: String? = null,

    /** اگر یادآوری پرداخت لازم است */
    @ColumnInfo(name = "reminderEnabled")
    val reminderEnabled: Boolean = true,

    @ColumnInfo(name = "reminderDate")
    val reminderDate: String? = null,

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
