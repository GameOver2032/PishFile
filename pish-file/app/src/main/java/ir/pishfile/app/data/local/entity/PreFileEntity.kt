package ir.pishfile.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * پیش‌فایل — برگه‌ی اصلی توافق پیش‌فروش بین مشتری و واحد.
 * همه‌ی شرایط مالی (پیش‌پرداخت، تخفیف، قسط‌بندی، تنظیم سند) این‌جا جمع می‌شود.
 */
@Entity(
    tableName = "pre_files",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UnitEntity::class,
            parentColumns = ["id"],
            childColumns = ["unitId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = CustomerEntity::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("projectId"), Index("unitId"), Index("customerId"),
        Index("status"), Index("draftNumber"), Index("remoteId"), Index("syncState")
    ]
)
data class PreFileEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    /** شماره پیش‌نویس — مثل PF-1405-0012 */
    @ColumnInfo(name = "draftNumber")
    val draftNumber: String,

    /** تاریخ تنظیم پیش‌فایل (شمسی: 1405/06/29) */
    @ColumnInfo(name = "draftDate")
    val draftDate: String,

    // --- ارتباط‌ها ---
    @ColumnInfo(name = "projectId")
    val projectId: String,

    @ColumnInfo(name = "unitId")
    val unitId: String? = null,

    @ColumnInfo(name = "customerId")
    val customerId: String? = null,

    /** فروشنده/کارشناس مسئول */
    @ColumnInfo(name = "salesAgentName")
    val salesAgentName: String? = null,

    @ColumnInfo(name = "salesAgentPhone")
    val salesAgentPhone: String? = null,

    /** کد پیگیری/رزرو */
    @ColumnInfo(name = "trackingCode")
    val trackingCode: String? = null,

    // --- اسنپ‌شات مشخصات واحد در لحظه تنظیم (تاریخی، در برابر تغییر واحد) ---
    @ColumnInfo(name = "unitSnapshotBlock")
    val unitBlock: String? = null,

    @ColumnInfo(name = "unitSnapshotNumber")
    val unitNumber: String? = null,

    @ColumnInfo(name = "unitSnapshotFloor")
    val unitFloor: Int? = null,

    @ColumnInfo(name = "unitSnapshotArea")
    val unitArea: Double? = null,

    // --- شرایط مالی ---
    /** مبلغ کل به عدد */
    @ColumnInfo(name = "totalPrice")
    val totalPrice: Long,

    /** قیمت هر مترمربع در لحظه توافق */
    @ColumnInfo(name = "pricePerMeter")
    val pricePerMeter: Long? = null,

    /** تخفیف */
    val discount: Long? = null,

    /** مبلغ بعد از تخفیف (نهایی) */
    @ColumnInfo(name = "finalPrice")
    val finalPrice: Long? = null,

    /** مبلغ پیش‌پرداخت */
    @ColumnInfo(name = "prepayment")
    val prepayment: Long? = null,

    /** مبلغ دریافتی تا این لحظه */
    @ColumnInfo(name = "paidAmount")
    val paidAmount: Long = 0,

    /** مانده */
    @ColumnInfo(name = "remainingAmount")
    val remainingAmount: Long? = null,

    /** تعداد اقساط */
    @ColumnInfo(name = "installmentCount")
    val installmentCount: Int? = null,

    /** مبلغ هر قسط */
    @ColumnInfo(name = "installmentAmount")
    val installmentAmount: Long? = null,

    /** دوره‌ی پرداخت قسط: ماهانه، دو ماهه، فصلی، سالانه */
    @ColumnInfo(name = "installmentPeriod")
    val installmentPeriod: String? = null,

    /** تاریخ شروع اقساط */
    @ColumnInfo(name = "installmentStartDate")
    val installmentStartDate: String? = null,

    /** نوع پرداخت: نقدی، اقساطی، تهاتر، تسهیلات، ترکیبی */
    @ColumnInfo(name = "paymentType")
    val paymentType: String = "INSTALLMENT",

    // --- تعهدات طرفین ---
    /** تعهد فروشنده: زمان تحویل، جریمه تأخیر... */
    @ColumnInfo(name = "sellerCommitment")
    val sellerCommitment: String? = null,

    /** تعهد خریدار */
    @ColumnInfo(name = "buyerCommitment")
    val buyerCommitment: String? = null,

    /** جریمه عدم انجام تعهدات */
    @ColumnInfo(name = "penaltyClause")
    val penaltyClause: String? = null,

    /** شرایط فسخ و انصراف */
    @ColumnInfo(name = "cancellationTerms")
    val cancellationTerms: String? = null,

    /** تنظیم سند رسمی — تاریخ توافقی */
    @ColumnInfo(name = "deedDate")
    val deedDate: String? = null,

    /** محل تنظیم سند: دفترخانه */
    @ColumnInfo(name = "deedOffice")
    val deedOffice: String? = null,

    /** تاریخ تحویل توافقی واحد */
    @ColumnInfo(name = "deliveryDate")
    val deliveryDate: String? = null,

    // --- ضمانت‌ها ---
    /** ملک مورد معامله در رهن/بازداشت است؟ */
    @ColumnInfo(name = "isUnitMortgaged")
    val isUnitMortgaged: Boolean = false,

    /** ضامن / چک / سفته */
    @ColumnInfo(name = "guaranteeType")
    val guaranteeType: String? = null,

    /** تعداد چک دریافتی */
    @ColumnInfo(name = "chequeCount")
    val chequeCount: Int? = null,

    /** مبلغ چک‌ها */
    @ColumnInfo(name = "chequeAmount")
    val chequeAmount: Long? = null,

    // --- وضعیت ---
    /** DRAFT / RESERVED / PENDING_PAYMENT / CONFIRMED / COMPLETED / CANCELED */
    val status: String = "DRAFT",

    /** تاریخ قطعی شدن */
    @ColumnInfo(name = "confirmedDate")
    val confirmedDate: String? = null,

    /** تاریخ لغو */
    @ColumnInfo(name = "cancelDate")
    val cancelDate: String? = null,

    /** دلیل لغو */
    @ColumnInfo(name = "cancelReason")
    val cancelReason: String? = null,

    /** در صورت لغو، مبلغ کسر شده */
    @ColumnInfo(name = "cancelPenaltyAmount")
    val cancelPenaltyAmount: Long? = null,

    // --- پرداخت‌های داخل قرارداد ---
    /** از چه پرداخت‌هایی در این قرارداد استفاده شده (مثلاً تهاتر خودرو) */
    @ColumnInfo(name = "exchangeDetails")
    val exchangeDetails: String? = null,

    @ColumnInfo(name = "notes")
    val notes: String? = null,

    /** اسناد پیوست (مسیر فایل‌ها، جدا شده با کاما) */
    @ColumnInfo(name = "documentPaths")
    val documentPaths: String? = null,

    @ColumnInfo(name = "contractPhotoPath")
    val contractPhotoPath: String? = null,

    val isFavorite: Boolean = false,

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
) {
    /** مبلغ نهایی مورد توافق */
    val effectivePrice: Long
        get() = finalPrice ?: (totalPrice - (discount ?: 0L))

    /** مانده قابل پرداخت */
    val dueAmount: Long
        get() = remainingAmount ?: (effectivePrice - paidAmount)

    val progressPercent: Int
        get() {
            val total = effectivePrice
            if (total <= 0) return 0
            return ((paidAmount.toDouble() / total) * 100).toInt().coerceIn(0, 100)
        }
}
