package ir.pishfile.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * پروژه ساختمانی (مثلاً «برج نگین شهرک غرب»).
 * هر پروژه شامل چند بلوک و واحد است و پیش‌فایل‌ها به آن متصل می‌شوند.
 */
@Entity(
    tableName = "projects",
    indices = [Index("remoteId"), Index("syncState"), Index("phase")]
)
data class ProjectEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    /** نام پروژه */
    val name: String,

    /** کد/شماره پروژه در دفتر */
    val code: String? = null,

    /** نوع پروژه: مسکونی، تجاری، اداری، مختلط */
    val projectType: String? = null,

    // --- موقعیت مکانی ---
    val province: String? = null,
    val city: String? = null,
    val district: String? = null,
    val address: String? = null,
    val postalCode: String? = null,

    /** موقعیت جغرافیایی */
    val latitude: Double? = null,
    val longitude: Double? = null,

    // --- مشخصات فنی ---
    /** مساحت زمین (مترمربع) */
    @ColumnInfo(name = "land_area")
    val landArea: Double? = null,

    /** مساحت کل زیربنا */
    @ColumnInfo(name = "total_built_area")
    val totalBuiltArea: Double? = null,

    /** تعداد بلوک */
    @ColumnInfo(name = "block_count")
    val blockCount: Int? = null,

    /** تعداد کل طبقات */
    @ColumnInfo(name = "floor_count")
    val floorCount: Int? = null,

    /** تعداد کل واحدها */
    @ColumnInfo(name = "unit_count")
    val unitCount: Int? = null,

    /** تعداد واحد در هر طبقه */
    @ColumnInfo(name = "units_per_floor")
    val unitsPerFloor: Int? = null,

    /** تعداد پارکینگ */
    @ColumnInfo(name = "parking_count")
    val parkingCount: Int? = null,

    /** تعداد آسانسور */
    @ColumnInfo(name = "elevator_count")
    val elevatorCount: Int? = null,

    /** نوع سازه: بتنی، فلزی، آجری... */
    @ColumnInfo(name = "structure_type")
    val structureType: String? = null,

    /** سیستم گرمایش/سرمایش */
    @ColumnInfo(name = "heating_system")
    val heatingSystem: String? = null,

    // --- مجوزها و تاریخ‌ها (شمسی، متن ساده) ---
    @ColumnInfo(name = "permit_number")
    val permitNumber: String? = null,

    @ColumnInfo(name = "permit_issue_date")
    val permitIssueDate: String? = null,

    @ColumnInfo(name = "permit_expiry_date")
    val permitExpiryDate: String? = null,

    @ColumnInfo(name = "land_area_document")
    val landDeedNumber: String? = null,

    // --- متره و برآورد ---
    /** قیمت تمام‌شده هر مترمربع (ریال/تومان) */
    @ColumnInfo(name = "cost_per_meter")
    val costPerMeter: Long? = null,

    /** قیمت فروش هر مترمربع (میانگین) */
    @ColumnInfo(name = "sale_price_per_meter")
    val salePricePerMeter: Long? = null,

    /** بودجه کل پروژه */
    @ColumnInfo(name = "total_budget")
    val totalBudget: Long? = null,

    // --- پیشرفت و زمان‌بندی ---
    /** مرحله فعلی پروژه */
    val phase: String = "PLANNING",

    /** درصد پیشرفت فیزیکی */
    @ColumnInfo(name = "progress_percent")
    val progressPercent: Int = 0,

    @ColumnInfo(name = "start_date")
    val startDate: String? = null,

    @ColumnInfo(name = "delivery_date")
    val deliveryDate: String? = null,

    /** تعهد تحویل از تاریخ ... */
    @ColumnInfo(name = "delivery_from")
    val deliveryFrom: String? = null,

    // --- افراد ---
    @ColumnInfo(name = "contractor_name")
    val contractorName: String? = null,

    @ColumnInfo(name = "contractor_phone")
    val contractorPhone: String? = null,

    @ColumnInfo(name = "supervisor_name")
    val supervisorName: String? = null,

    @ColumnInfo(name = "supervisor_phone")
    val supervisorPhone: String? = null,

    @ColumnInfo(name = "developer_name")
    val developerName: String? = null,

    /** شماره تماس مدیر فروش */
    @ColumnInfo(name = "sales_manager_phone")
    val salesManagerPhone: String? = null,

    // --- مالی ---
    /** شماره حساب/شبا برای واریز */
    val iban: String? = null,

    /** نام بانک */
    @ColumnInfo(name = "bank_name")
    val bankName: String? = null,

    // --- متادیتا ---
    /** امکانات پروژه به صورت رشته جدا شده با کاما */
    val facilities: String? = null,

    /** توضیحات و یادداشت */
    val description: String? = null,

    /** برچسب‌ها (کاما) */
    val tags: String? = null,

    val isFavorite: Boolean = false,
    val isArchived: Boolean = false,

    // --- میدان‌های آماده برای همگام‌سازی (Sync-ready) ---
    @ColumnInfo(name = "remoteId")
    val remoteId: String? = null,

    /** CLEAN / PENDING_UPLOAD / PENDING_DELETE */
    @ColumnInfo(name = "syncState")
    val syncState: String = "PENDING_UPLOAD",

    @ColumnInfo(name = "createdAt")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updatedAt")
    val updatedAt: Long = System.currentTimeMillis(),

    /** زمان آخرین تغییر روی سرور (برای حل تعارض) */
    @ColumnInfo(name = "serverUpdatedAt")
    val serverUpdatedAt: Long? = null,

    @ColumnInfo(name = "deletedAt")
    val deletedAt: Long? = null,
)
