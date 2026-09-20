package ir.pishfile.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * مشتری / خریدار — اطلاعات هویتی، تماس، وضعیت اعتباری و سابقه خرید.
 */
@Entity(
    tableName = "customers",
    indices = [Index("remoteId"), Index("syncState"), Index("status"), Index("nationalId"), Index("phonePrimary")]
)
data class CustomerEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    // --- نام ---
    val firstName: String,
    val lastName: String,
    /** نام پدر */
    @ColumnInfo(name = "fatherName")
    val fatherName: String? = null,

    /** نحوه خطاب: آقا / خانم / شرکت */
    val title: String? = null,

    /** نام شرکت (برای خریداران حقوقی) */
    @ColumnInfo(name = "companyName")
    val companyName: String? = null,

    /** کد ملی / شناسه ملی */
    @ColumnInfo(name = "nationalId")
    val nationalId: String? = null,

    /** شماره شناسنامه */
    @ColumnInfo(name = "idNumber")
    val idNumber: String? = null,

    @ColumnInfo(name = "birthDate")
    val birthDate: String? = null,

    @ColumnInfo(name = "registrationNumber")
    val registrationNumber: String? = null,

    // --- تماس ---
    @ColumnInfo(name = "phonePrimary")
    val phonePrimary: String? = null,

    @ColumnInfo(name = "phoneSecondary")
    val phoneSecondary: String? = null,

    @ColumnInfo(name = "whatsapp")
    val whatsapp: String? = null,

    val email: String? = null,

    // --- نشانی ---
    val province: String? = null,
    val city: String? = null,
    val address: String? = null,
    val postalCode: String? = null,

    /** محل کار/شغل */
    val job: String? = null,
    @ColumnInfo(name = "workAddress")
    val workAddress: String? = null,
    @ColumnInfo(name = "workPhone")
    val workPhone: String? = null,

    /** شماره حساب/شبا مشتری برای عودت وجه */
    val iban: String? = null,
    @ColumnInfo(name = "bankName")
    val bankName: String? = null,

    // --- اعتبارسنجی و وضعیت ---
    /** LEAD / ACTIVE / INSTALLMENT / SETTLED / INACTIVE */
    val status: String = "LEAD",

    /** سرنخ از کجا آمده: معرفی، اینستاگرام، ... */
    val source: String? = null,

    /** کد معرف (اگر توسط شخص دیگری معرفی شده) */
    @ColumnInfo(name = "referredBy")
    val referredBy: String? = null,

    /** امتیاز/تخمین قدرت خرید مشتری */
    @ColumnInfo(name = "creditScore")
    val creditScore: Int? = null,

    /** سقف اعتبار */
    @ColumnInfo(name = "creditLimit")
    val creditLimit: Long? = null,

    /** آیا چک برگشتی دارد */
    @ColumnInfo(name = "hasBouncedCheque")
    val hasBouncedCheque: Boolean = false,

    /** آیا قبلاً خریدار ما بوده */
    @ColumnInfo(name = "isReturningCustomer")
    val isReturningCustomer: Boolean = false,

    /** تعداد کل خریدها (تجمیعی) */
    @ColumnInfo(name = "totalPurchases")
    val totalPurchases: Long? = null,

    // --- شخص حقوقی ---
    /** نوع شخص: حقیقی / حقوقی */
    @ColumnInfo(name = "entityType")
    val entityType: String = "INDIVIDUAL",

    /** کد اقتصادی */
    @ColumnInfo(name = "economicCode")
    val economicCode: String? = null,

    // --- یادداشت‌ها ---
    val notes: String? = null,
    val tags: String? = null,
    val photoPath: String? = null,

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
    val fullName: String
        get() = listOfNotNull(title, firstName, lastName).joinToString(" ").trim()
}
