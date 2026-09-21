package ir.pishfile.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import ir.pishfile.app.core.Constants
import ir.pishfile.app.data.local.dao.FollowUpDao
import ir.pishfile.app.data.local.dao.PreFileDao
import ir.pishfile.app.data.local.dao.ProjectDao
import ir.pishfile.app.data.local.dao.UnitDao
import ir.pishfile.app.data.local.entity.FollowUpEntity
import ir.pishfile.app.data.local.entity.PreFileEntity
import ir.pishfile.app.data.local.entity.ProjectEntity
import ir.pishfile.app.data.local.entity.UnitEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

@Database(
    entities = [
        ProjectEntity::class,
        UnitEntity::class,
        PreFileEntity::class,
        FollowUpEntity::class,
    ],
    version = 2,
    exportSchema = false
)
abstract class PishFileDatabase : RoomDatabase() {

    abstract fun projectDao(): ProjectDao
    abstract fun unitDao(): UnitDao
    abstract fun preFileDao(): PreFileDao
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
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }

        private class SeedCallback(private val context: Context) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                val database = INSTANCE ?: return
                CoroutineScope(Dispatchers.IO).launch {
                    seed(database)
                }
            }

            private suspend fun seed(database: PishFileDatabase) {
                if (database.projectDao().getAll().isNotEmpty()) return

                val now = System.currentTimeMillis()
                val today = ir.pishfile.app.core.Formatters.todayJalali()

                // ۱) پروژه واریزی و امتیازی
                val p1Id = UUID.randomUUID().toString()
                database.projectDao().insert(
                    ProjectEntity(
                        id = p1Id,
                        name = "پروژه شهید خرازی (واریزی و امتیاز)",
                        code = "KH-01",
                        projectType = "مسکونی",
                        pricingModel = Constants.PRICING_DEPOSIT_BONUS,
                        city = "تهران",
                        district = "منطقه ۲۲ - چیتگر",
                        defaultDepositAmount = 1_850_000_000L,
                        phase = Constants.PROJECT_STRUCTURE,
                        progressPercent = 65,
                        deliveryDate = "1406/06/31",
                        description = "پروژه معتبر تعاونی منطقه ۲۲ — مدل واریزی + حق امتیاز",
                        createdAt = now,
                        updatedAt = now,
                    )
                )

                // ۲) پروژه متری
                val p2Id = UUID.randomUUID().toString()
                database.projectDao().insert(
                    ProjectEntity(
                        id = p2Id,
                        name = "برج پرهام سعادت‌آباد (قیمت متری)",
                        code = "PR-02",
                        projectType = "مسکونی",
                        pricingModel = Constants.PRICING_METER,
                        city = "تهران",
                        district = "سعادت‌آباد",
                        salePricePerMeter = 160_000_000L,
                        phase = Constants.PROJECT_FINISHING,
                        progressPercent = 85,
                        deliveryDate = "1405/11/30",
                        description = "برج لوکس شخصی‌ساز سعادت‌آباد",
                        createdAt = now,
                        updatedAt = now,
                    )
                )

                // ۳) پروژه سهامی
                val p3Id = UUID.randomUUID().toString()
                database.projectDao().insert(
                    ProjectEntity(
                        id = p3Id,
                        name = "پروژه تجاری اداری الماس چیتگر (سهامی)",
                        code = "AL-03",
                        projectType = "تجاری",
                        pricingModel = Constants.PRICING_SHARE,
                        city = "تهران",
                        district = "منطقه ۲۲",
                        shareMeterArea = 10.0,
                        sharePrice = 950_000_000L,
                        phase = Constants.PROJECT_EXCAVATION,
                        progressPercent = 25,
                        deliveryDate = "1407/12/29",
                        description = "فروش به‌صورت سهام ۱۰ متری تجاری",
                        createdAt = now,
                        updatedAt = now,
                    )
                )

                // یک واحد آماده نمونه
                val uId = UUID.randomUUID().toString()
                database.unitDao().insert(
                    UnitEntity(
                        id = uId,
                        projectId = p2Id,
                        block = "A",
                        unitNumber = "502",
                        floor = 5,
                        grossArea = 120.0,
                        pricePerMeter = 160_000_000L,
                        totalPrice = 19_200_000_000L,
                        status = Constants.UNIT_AVAILABLE,
                        deliveryDate = "1405/11/30",
                        direction = "جنوبی",
                        createdAt = now,
                        updatedAt = now,
                    )
                )

                // فایل پیش‌فروش نمونه
                val pfId = UUID.randomUUID().toString()
                database.preFileDao().insert(
                    PreFileEntity(
                        id = pfId,
                        draftNumber = "PF-1405-0001",
                        draftDate = today,
                        projectId = p1Id,
                        ownerName = "آقای رحیمی",
                        ownerPhone = "09121112233",
                        pricingModel = Constants.PRICING_DEPOSIT_BONUS,
                        depositAmount = 1_850_000_000L,
                        bonusAmount = 950_000_000L,
                        totalPrice = 2_800_000_000L,
                        hasRanking = true,
                        ranking = "رتبه ۱۸ اولویت بلوک A",
                        saleConditionCash = true,
                        saleConditionInstallment = true,
                        saleConditionExchange = true,
                        saleConditionNotes = "تهاتر فقط با خودرو ۲۰۶ یا پارس تا ۵۰۰ میلیون",
                        installmentCount = 12,
                        remainingInstallmentsCount = 6,
                        installmentAmount = 75_000_000L,
                        installmentPeriod = "ماهانه",
                        nextInstallmentDueDate = "1405/08/15",
                        status = Constants.PREFILE_URGENT,
                        notes = "فروش فوری به دلیل نیاز مالی",
                        createdAt = now,
                        updatedAt = now,
                    )
                )

                // پیگیری امروز نمونه
                database.followUpDao().insert(
                    FollowUpEntity(
                        type = "CALL",
                        priority = "HIGH",
                        title = "تماس با خریدار پروژه خرازی جهت اعلام تخفیف امتیاز",
                        contactPhone = "09121112233",
                        preFileId = pfId,
                        projectId = p1Id,
                        dueDate = today,
                        dueTime = "11:00",
                        status = Constants.FOLLOWUP_PENDING,
                        createdAt = now,
                        updatedAt = now,
                    )
                )
            }
        }
    }
}
