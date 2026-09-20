package ir.pishfile.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * واحد ساختمانی — قلب فیلدهای تخصصی املاک.
 * شامل بلوک، طبقه، متراژ، جهت، امکانات و اطلاعات مالی.
 */
@Entity(
    tableName = "units",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("projectId"), Index("status"), Index("remoteId"), Index("syncState"),
        Index(value = ["projectId", "block", "floor", "unitNumber"], unique = false)
    ]
)
data class UnitEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "projectId")
    val projectId: String,

    // --- شناسه واحد ---
    /** نام بلوک/فاز مثل «A» یا «فاز ۲» */
    val block: String? = null,

    /** شماره واحد مثل «۱۰۲» */
    @ColumnInfo(name = "unitNumber")
    val unitNumber: String,

    /** طبقه (منفی برای زیرزمین) */
    val floor: Int? = null,

    /** نوع واحد: آپارتمان، مغازه، اداری، پارکینگ، انباری... */
    @ColumnInfo(name = "unitType")
    val unitType: String = "APARTMENT",

    /** واحد چند خوابه */
    val bedrooms: Int? = null,

    /** تعداد سرویس بهداشتی */
    val bathrooms: Int? = null,

    /** تعداد آشپزخانه (برای واحدهای تجاری) */
    val kitchens: Int? = null,

    // --- متراژ ---
    /** متراژ ناخالص (فروش) */
    @ColumnInfo(name = "grossArea")
    val grossArea: Double? = null,

    /** متراژ مفید/خالص */
    @ColumnInfo(name = "netArea")
    val netArea: Double? = null,

    /** متراژ بالکن/تراس */
    @ColumnInfo(name = "balconyArea")
    val balconyArea: Double? = null,

    /** سهم مشاعات (مترمربع) */
    @ColumnInfo(name = "commonAreaShare")
    val commonAreaShare: Double? = null,

    /** ارتفاع سقف (سانتی‌متر) */
    @ColumnInfo(name = "ceilingHeight")
    val ceilingHeight: Int? = null,

    // --- موقعیت و نور ---
    /** جهت واحد: شمالی، جنوبی، ... */
    val direction: String? = null,

    /** نورگیری */
    val lighting: String? = null,

    /** منظر/ویو */
    val view: String? = null,

    /** وضعیت واحد در زمین: نبش، وسط، تک‌واحدی */
    @ColumnInfo(name = "positionType")
    val positionType: String? = null,

    // --- امکانات (رشته‌های جدا شده با کاما) ---
    // مثال: "آسانسور,پارکینگ,انباری,استخر"
    val facilities: String? = null,

    /** تعداد پارکینگ اختصاصی */
    @ColumnInfo(name = "parkingCount")
    val parkingCount: Int = 0,

    /** شماره پارکینگ */
    @ColumnInfo(name = "parkingNumber")
    val parkingNumber: String? = null,

    /** تعداد انباری */
    @ColumnInfo(name = "storageCount")
    val storageCount: Int = 0,

    /** شماره انباری */
    @ColumnInfo(name = "storageNumber")
    val storageNumber: String? = null,

    // --- مالی ---
    /** قیمت پایه هر مترمربع */
    @ColumnInfo(name = "pricePerMeter")
    val pricePerMeter: Long? = null,

    /** قیمت کل واحد */
    @ColumnInfo(name = "totalPrice")
    val totalPrice: Long? = null,

    /** قیمت نهایی توافق‌شده */
    @ColumnInfo(name = "finalPrice")
    val finalPrice: Long? = null,

    /** هزینه‌های جانبی (انشعابات، انباری، پارکینگ...) */
    @ColumnInfo(name = "extraCosts")
    val extraCosts: Long? = null,

    /** تخفیف (مبلغ) */
    val discount: Long? = null,

    /** مالیات بر ارزش افزوده (مبلغ) */
    @ColumnInfo(name = "vatAmount")
    val vatAmount: Long? = null,

    /** پیش‌پرداخت پیشنهادی */
    @ColumnInfo(name = "prepaymentSuggestion")
    val prepaymentSuggestion: Long? = null,

    /** مبلغ هر قسط پیشنهادی */
    @ColumnInfo(name = "suggestedInstallment")
    val suggestedInstallment: Long? = null,

    /** تعداد اقساط پیشنهادی */
    @ColumnInfo(name = "suggestedInstallmentCount")
    val suggestedInstallmentCount: Int? = null,

    /** قیمت تمام‌شده (برای محاسبه سود) */
    @ColumnInfo(name = "costPrice")
    val costPrice: Long? = null,

    // --- وضعیت ---
    /** AVAILABLE / RESERVED / SOLD / DELIVERED */
    val status: String = "AVAILABLE",

    // --- تحویل ---
    @ColumnInfo(name = "deliveryDate")
    val deliveryDate: String? = null,

    @ColumnInfo(name = "deliveryStatus")
    val deliveryStatus: String? = null,

    /** ملاحظات فنی */
    @ColumnInfo(name = "technicalNotes")
    val technicalNotes: String? = null,

    val description: String? = null,
    val floorPlanPath: String? = null,
    val photoPaths: String? = null,

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
    /** عنوان کامل نمایشی: «بلوک A - واحد ۱۰۲ (طبقه ۱)» */
    val displayTitle: String
        get() = buildString {
            if (!block.isNullOrBlank()) append("بلوک $block - ")
            append("واحد $unitNumber")
            floor?.let { append(" (طبقه $it)") }
        }
}
