package ir.pishfile.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import ir.pishfile.app.core.Constants
import ir.pishfile.app.data.local.dao.CustomerDao
import ir.pishfile.app.data.local.dao.FollowUpDao
import ir.pishfile.app.data.local.dao.InstallmentDao
import ir.pishfile.app.data.local.dao.PreFileDao
import ir.pishfile.app.data.local.dao.ProjectDao
import ir.pishfile.app.data.local.dao.UnitDao
import ir.pishfile.app.data.local.entity.CustomerEntity
import ir.pishfile.app.data.local.entity.FollowUpEntity
import ir.pishfile.app.data.local.entity.InstallmentEntity
import ir.pishfile.app.data.local.entity.PreFileEntity
import ir.pishfile.app.data.local.entity.ProjectEntity
import ir.pishfile.app.data.local.entity.UnitEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * دیتابیس اصلی برنامه — همه‌چیز روی گوشی ذخیره می‌شود (آفلاین).
 *
 * نکته‌ی مهم برای همگام‌سازی آینده: هیچ‌کدام از جداول رکورد را واقعاً پاک نمی‌کنند؛
 * فقط deletedAt پر می‌شود (soft delete) تا تغییرات برای سرور قابل ارسال باشد.
 */
@Database(
    entities = [
        ProjectEntity::class,
        UnitEntity::class,
        CustomerEntity::class,
        PreFileEntity::class,
        InstallmentEntity::class,
        FollowUpEntity::class,
    ],
    version = 1,
    exportSchema = true
)
abstract class PishFileDatabase : RoomDatabase() {

    abstract fun projectDao(): ProjectDao
    abstract fun unitDao(): UnitDao
    abstract fun customerDao(): CustomerDao
    abstract fun preFileDao(): PreFileDao
    abstract fun installmentDao(): InstallmentDao
    abstract fun followUpDao(): FollowUpDao

    companion object {
        const val DB_NAME = "pishfile.db"

        @Volatile
        private var INSTANCE: PishFileDatabase? = null

        fun getInstance(context: Context): PishFileDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    PishFileDatabase::class.java,
                    DB_NAME
                )
                    .addCallback(SeedCallback(context))
                    // TODO(نسخه ۲): مهاجرت‌ها را این‌جا اضافه کنید — هرگز fallbackToDestructiveMigration در نسخه نهایی
                    .fallbackToDestructiveMigrationOnDowngrade()
                    .build()
                    .also { INSTANCE = it }
            }
        }

        /**
         * داده‌ی نمونه‌ی اولیه (فقط وقتی دیتابیس برای اولین بار ساخته شود).
         * این داده‌ها نمونه هستند و کاربر می‌تواند همه را حذف/ویرایش کند.
         */
        private class SeedCallback(private val context: Context) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                val database = INSTANCE ?: return
                CoroutineScope(Dispatchers.IO).launch {
                    seed(database)
                }
            }

            private suspend fun seed(database: PishFileDatabase) {
                // فقط اگر از قبل چیزی نبود
                if (database.projectDao().getAll().isNotEmpty()) return

                val projectId = UUID.randomUUID().toString()
                val now = System.currentTimeMillis()
                val today = ir.pishfile.app.core.Formatters.todayJalali()

                database.projectDao().insert(
                    ProjectEntity(
                        id = projectId,
                        name = "پروژه نمونه — برج پرهام",
                        code = "PRJ-001",
                        projectType = "مسکونی",
                        province = "تهران",
                        city = "تهران",
                        district = "سعادت‌آباد",
                        address = "بلوار دریا، خیابان مطهری",
                        landArea = 1200.0,
                        totalBuiltArea = 8600.0,
                        blockCount = 1,
                        floorCount = 12,
                        unitCount = 48,
                        unitsPerFloor = 4,
                        parkingCount = 62,
                        elevatorCount = 3,
                        structureType = "بتنی",
                        phase = Constants.PROJECT_STRUCTURE,
                        progressPercent = 45,
                        startDate = "1404/03/15",
                        deliveryDate = "1406/12/29",
                        salePricePerMeter = 185_000_000,
                        costPerMeter = 120_000_000,
                        totalBudget = 1_000_000_000_000L,
                        facilities = "آسانسور,پارکینگ,انباری,لابی,نگهبانی ۲۴ ساعته,سالن ورزشی",
                        description = "داده‌ی نمونه برای آشنایی با برنامه — قابل حذف",
                        createdAt = now,
                        updatedAt = now,
                    )
                )

                val unitIds = mutableListOf<String>()
                for (floor in 1..4) {
                    for (num in 1..2) {
                        val area = (85 + floor * 5 + num * 3).toDouble()
                        val pricePerMeter = 180_000_000L
                        val id = UUID.randomUUID().toString()
                        unitIds += id
                        database.unitDao().insert(
                            UnitEntity(
                                id = id,
                                projectId = projectId,
                                block = "A",
                                unitNumber = "${floor}0$num",
                                floor = floor,
                                unitType = Constants.UNIT_TYPE_APARTMENT,
                                bedrooms = if (num == 1) 1 else 2,
                                bathrooms = 1,
                                grossArea = area,
                                netArea = area - 12,
                                balconyArea = 6.0,
                                ceilingHeight = 290,
                                direction = if (num == 1) "جنوبی" else "شمالی",
                                view = "منظر شهری",
                                facilities = "آسانسور,پارکینگ,انباری",
                                parkingCount = 1,
                                parkingNumber = "P-${floor}0$num",
                                storageCount = 1,
                                pricePerMeter = pricePerMeter,
                                totalPrice = (area * pricePerMeter).toLong(),
                                prepaymentSuggestion = 500_000_000,
                                suggestedInstallmentCount = 24,
                                status = if (floor == 1 && num == 1) Constants.UNIT_RESERVED else Constants.UNIT_AVAILABLE,
                                deliveryDate = "1406/12/29",
                                createdAt = now,
                                updatedAt = now,
                            )
                        )
                    }
                }

                val customerId = UUID.randomUUID().toString()
                database.customerDao().insert(
                    CustomerEntity(
                        id = customerId,
                        title = "آقای",
                        firstName = "مهدی",
                        lastName = "کریمی",
                        fatherName = "حسین",
                        nationalId = "0012345678",
                        phonePrimary = "09121234567",
                        whatsapp = "09121234567",
                        city = "تهران",
                        status = Constants.CUSTOMER_ACTIVE,
                        source = Constants.SOURCE_INSTAGRAM,
                        job = "مهندس نرم‌افزار",
                        notes = "داده‌ی نمونه — قابل حذف",
                        createdAt = now,
                        updatedAt = now,
                    )
                )

                val preFileId = UUID.randomUUID().toString()
                val unitPrice: Long = 93L * 180_000_000L
                database.preFileDao().insert(
                    PreFileEntity(
                        id = preFileId,
                        draftNumber = "PF-1405-0001",
                        draftDate = today,
                        projectId = projectId,
                        unitId = unitIds.first(),
                        customerId = customerId,
                        salesAgentName = "کارشناس فروش",
                        totalPrice = unitPrice,
                        pricePerMeter = 180_000_000,
                        discount = 50_000_000,
                        finalPrice = unitPrice - 50_000_000,
                        prepayment = 500_000_000,
                        paidAmount = 300_000_000,
                        installmentCount = 24,
                        installmentAmount = ((unitPrice - 50_000_000 - 500_000_000) / 24),
                        installmentPeriod = "ماهانه",
                        installmentStartDate = "1405/08/01",
                        paymentType = Constants.PAYMENT_INSTALLMENT,
                        deedDate = "1406/12/30",
                        deliveryDate = "1406/12/29",
                        status = Constants.PREFILE_RESERVED,
                        sellerCommitment = "تحویل واحد تا پایان اسفند ۱۴۰۶ همراه با سند تک‌برگ",
                        buyerCommitment = "پرداخت اقساط در سررسید مقرر",
                        notes = "داده‌ی نمونه — قابل حذف",
                        createdAt = now,
                        updatedAt = now,
                    )
                )

                // اقساط نمونه
                var number = 1
                for (jalaliMonth in 8..12) {
                    database.installmentDao().insert(
                        InstallmentEntity(
                            preFileId = preFileId,
                            installmentNumber = number++,
                            title = "قسط $number",
                            amount = ((unitPrice - 50_000_000 - 500_000_000) / 24),
                            dueDate = "1405/0$jalaliMonth/01",
                            status = Constants.INSTALLMENT_UNPAID,
                            createdAt = now,
                            updatedAt = now,
                        )
                    )
                }

                database.followUpDao().insert(
                    FollowUpEntity(
                        type = "CALL",
                        priority = "HIGH",
                        title = "تماس پیگیری پرداخت پیش‌پرداخت",
                        description = "داده‌ی نمونه — قابل حذف",
                        customerId = customerId,
                        preFileId = preFileId,
                        projectId = projectId,
                        dueDate = today,
                        dueTime = "10:30",
                        status = Constants.FOLLOWUP_PENDING,
                        assignee = "کارشناس فروش",
                        contactPhone = "09121234567",
                        createdAt = now,
                        updatedAt = now,
                    )
                )
            }
        }
    }
}
