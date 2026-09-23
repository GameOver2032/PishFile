package ir.pishfile.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * فایل پیش‌فروش.
 * شامل مشخصات مالی بر اساس ۳ مدل:
 *  1) واریزی و امتیاز (واریزی تا امروز + مبلغ امتیاز = کل پرداختی خریدار)
 *  2) متری (قیمت هر متر × متراژ = قیمت کل)
 *  3) سهامی (متراژ هر سهم × تعداد سهم، قیمت هر سهم × تعداد سهم)
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
            entity = ProjectAreaEntity::class,
            parentColumns = ["id"],
            childColumns = ["areaId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("projectId"), Index("unitId"), Index("areaId"),
        Index("status"), Index("draftNumber"), Index("remoteId"), Index("syncState")
    ]
)
data class PreFileEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    /** شماره فایل پیش‌فروش (PF-1405-0001) */
    @ColumnInfo(name = "draftNumber")
    val draftNumber: String,

    /** تاریخ ثبت فایل (شمسی) */
    @ColumnInfo(name = "draftDate")
    val draftDate: String,

    // --- ارتباط پروژه و واحد ---
    @ColumnInfo(name = "projectId")
    val projectId: String,

    @ColumnInfo(name = "unitId")
    val unitId: String? = null,

    /**
     * نوع فایل: READY (فایل واحد آماده) یا PRESALE (فایل پیش‌فروش).
     * مقادیر قدیمی (null) به‌عنوان PRESALE در نظر گرفته می‌شوند.
     */
    @ColumnInfo(name = "fileType")
    val fileType: String? = null,

    /** متراژ انتخاب‌شده از میان متراژهای پروژه (با شرایط مالی مخصوص به خودش) */
    @ColumnInfo(name = "areaId")
    val areaId: String? = null,

    /** مشخصات مالک / سپارنده‌ی فایل */
    @ColumnInfo(name = "ownerName")
    val ownerName: String? = null,

    @ColumnInfo(name = "ownerPhone")
    val ownerPhone: String? = null,

    // --- مدل قیمت‌گذاری: METER / DEPOSIT_BONUS / SHARE ---
    @ColumnInfo(name = "pricingModel")
    val pricingModel: String = "METER",

    // --- ۱) مدل واریزی و امتیاز ---
    /** مبلغ واریزی پروژه تا امروز */
    @ColumnInfo(name = "depositAmount")
    val depositAmount: Long? = null,

    /** مبلغ امتیاز (سود پروژه) */
    @ColumnInfo(name = "bonusAmount")
    val bonusAmount: Long? = null,

    // --- ۲) مدل متری ---
    /** قیمت هر مترمربع */
    @ColumnInfo(name = "pricePerMeter")
    val pricePerMeter: Long? = null,

    /** متراژ واحد (مترمربع) */
    @ColumnInfo(name = "meterArea")
    val meterArea: Double? = null,

    // --- ۳) مدل سهامی ---
    /** متراژ هر سهم */
    @ColumnInfo(name = "shareMeterArea")
    val shareMeterArea: Double? = null,

    /** تعداد سهم */
    @ColumnInfo(name = "shareCount")
    val shareCount: Int? = null,

    /** قیمت هر سهم */
    @ColumnInfo(name = "sharePrice")
    val sharePrice: Long? = null,

    // --- مبلغ کل (پرداختی کل برای خرید فایل) ---
    @ColumnInfo(name = "totalPrice")
    val totalPrice: Long = 0L,

    // --- رتبه‌بندی پروژه/فایل ---
    @ColumnInfo(name = "hasRanking")
    val hasRanking: Boolean = false,

    @ColumnInfo(name = "ranking")
    val ranking: String? = null,

    // --- شرایط فروش (امکان تیک‌گذاری نقد، شرایطی، تهاتر) ---
    @ColumnInfo(name = "saleConditionCash")
    val saleConditionCash: Boolean = true,

    @ColumnInfo(name = "saleConditionInstallment")
    val saleConditionInstallment: Boolean = false,

    @ColumnInfo(name = "saleConditionExchange")
    val saleConditionExchange: Boolean = false,

    /** توضیحات شرایط فروش (مثلاً در صورت تهاتر با چه چیزهایی تهاتر می‌کند) */
    @ColumnInfo(name = "saleConditionNotes")
    val saleConditionNotes: String? = null,

    // --- اطلاعات اقساط پرونده ---
    /** تعداد کل اقساط */
    @ColumnInfo(name = "installmentCount")
    val installmentCount: Int? = null,

    /** تعداد اقساط مانده */
    @ColumnInfo(name = "remainingInstallmentsCount")
    val remainingInstallmentsCount: Int? = null,

    /** مبلغ هر قسط */
    @ColumnInfo(name = "installmentAmount")
    val installmentAmount: Long? = null,

    /** دوره‌ی پرداخت اقساط (مثلاً ماهانه، سه ماهه) */
    @ColumnInfo(name = "installmentPeriod")
    val installmentPeriod: String? = null,

    /** تاریخ سررسید قسط پیش‌رو */
    @ColumnInfo(name = "nextInstallmentDueDate")
    val nextInstallmentDueDate: String? = null,

    // --- اسنپ‌شات واحد ---
    @ColumnInfo(name = "unitSnapshotBlock")
    val unitBlock: String? = null,

    @ColumnInfo(name = "unitSnapshotNumber")
    val unitNumber: String? = null,

    @ColumnInfo(name = "unitSnapshotFloor")
    val unitFloor: Int? = null,

    // --- وضعیت فایل ---
    /** DRAFT (پیش‌نویس) / URGENT (فروش فوری) / NORMAL (غیر فوری) / WITHDRAWN (منصرف از فروش) */
    val status: String = "DRAFT",

    /** تاریخ تحویل تقریبی */
    @ColumnInfo(name = "deliveryDate")
    val deliveryDate: String? = null,

    /** توضیحات و یادداشت کلی */
    @ColumnInfo(name = "notes")
    val notes: String? = null,

    val isFavorite: Boolean = false,

    // --- فیلدهای سیستمی و همگام‌سازی ---
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
    /** محاسبه مبلغ کل پرداختی بر اساس مدل انتخابی */
    val computedTotal: Long
        get() = when (pricingModel) {
            "DEPOSIT_BONUS" -> (depositAmount ?: 0L) + (bonusAmount ?: 0L)
            "SHARE" -> {
                val count = shareCount ?: 1
                val price = sharePrice ?: 0L
                count * price
            }
            else -> { // METER
                if (totalPrice > 0) totalPrice
                else {
                    val m = meterArea ?: 0.0
                    val p = pricePerMeter ?: 0L
                    (m * p).toLong()
                }
            }
        }

    val displayPrice: Long
        get() = if (totalPrice > 0) totalPrice else computedTotal

    /** شرایط فروش متنی */
    val saleConditionsSummary: String
        get() = buildList {
            if (saleConditionCash) add("نقدی")
            if (saleConditionInstallment) add("شرایطی")
            if (saleConditionExchange) add("تهاتر")
        }.joinToString("، ").ifBlank { "تعیین‌نشده" }
}
