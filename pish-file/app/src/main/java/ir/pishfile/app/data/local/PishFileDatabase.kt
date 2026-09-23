package ir.pishfile.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import ir.pishfile.app.core.Constants
import ir.pishfile.app.data.local.dao.AttachmentDao
import ir.pishfile.app.data.local.dao.CustomerDao
import ir.pishfile.app.data.local.dao.FollowUpDao
import ir.pishfile.app.data.local.dao.NoteDao
import ir.pishfile.app.data.local.dao.PreFileDao
import ir.pishfile.app.data.local.dao.ProjectAreaDao
import ir.pishfile.app.data.local.dao.ProjectDao
import ir.pishfile.app.data.local.dao.UnitDao
import ir.pishfile.app.data.local.entity.AttachmentEntity
import ir.pishfile.app.data.local.entity.CustomerEntity
import ir.pishfile.app.data.local.entity.FollowUpEntity
import ir.pishfile.app.data.local.entity.NoteEntity
import ir.pishfile.app.data.local.entity.PreFileEntity
import ir.pishfile.app.data.local.entity.ProjectAreaEntity
import ir.pishfile.app.data.local.entity.ProjectEntity
import ir.pishfile.app.data.local.entity.UnitEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

@Database(
    entities = [
        ProjectEntity::class,
        ProjectAreaEntity::class,
        UnitEntity::class,
        PreFileEntity::class,
        FollowUpEntity::class,
        CustomerEntity::class,
        NoteEntity::class,
        AttachmentEntity::class,
    ],
    version = 5,
    exportSchema = false
)
abstract class PishFileDatabase : RoomDatabase() {

    abstract fun projectDao(): ProjectDao
    abstract fun projectAreaDao(): ProjectAreaDao
    abstract fun unitDao(): UnitDao
    abstract fun preFileDao(): PreFileDao
    abstract fun followUpDao(): FollowUpDao
    abstract fun customerDao(): CustomerDao
    abstract fun noteDao(): NoteDao
    abstract fun attachmentDao(): AttachmentDao

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
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }

        /**
         * نسخه‌ی ۲ → ۳: افزودن «پیش‌فرض‌های ثبت فایل» به جدول projects.
         * مهاجرت واقعی (نه مخرب) تا داده‌های کاربر حفظ شود.
         */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE projects ADD COLUMN default_bonus_amount INTEGER")
                db.execSQL("ALTER TABLE projects ADD COLUMN has_ranking INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE projects ADD COLUMN default_ranking TEXT")
                db.execSQL("ALTER TABLE projects ADD COLUMN sale_condition_cash INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE projects ADD COLUMN sale_condition_installment INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE projects ADD COLUMN sale_condition_exchange INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE projects ADD COLUMN sale_condition_notes TEXT")
                db.execSQL("ALTER TABLE projects ADD COLUMN installment_count INTEGER")
                db.execSQL("ALTER TABLE projects ADD COLUMN remaining_installments_count INTEGER")
                db.execSQL("ALTER TABLE projects ADD COLUMN installment_amount INTEGER")
                db.execSQL("ALTER TABLE projects ADD COLUMN installment_period TEXT")
                db.execSQL("ALTER TABLE projects ADD COLUMN next_installment_due_date TEXT")
            }
        }

        /**
         * نسخه‌ی ۳ → ۴: افزودن جدول‌های customers و notes و ستون customerId به follow_ups.
         * مهاجرت واقعی (نه مخرب) تا داده‌های کاربر حفظ شود.
         */
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS customers (
                        id TEXT NOT NULL PRIMARY KEY,
                        name TEXT NOT NULL,
                        phone TEXT,
                        role TEXT NOT NULL DEFAULT 'BUYER',
                        preFileId TEXT,
                        unitId TEXT,
                        notes TEXT,
                        status TEXT NOT NULL DEFAULT 'ACTIVE',
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL,
                        remoteId TEXT,
                        syncState TEXT NOT NULL DEFAULT 'PENDING_UPLOAD',
                        serverUpdatedAt INTEGER,
                        deletedAt INTEGER,
                        FOREIGN KEY(preFileId) REFERENCES pre_files(id) ON UPDATE NO ACTION ON DELETE SET NULL,
                        FOREIGN KEY(unitId) REFERENCES units(id) ON UPDATE NO ACTION ON DELETE SET NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_customers_preFileId ON customers(preFileId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_customers_unitId ON customers(unitId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_customers_status ON customers(status)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_customers_syncState ON customers(syncState)")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS notes (
                        id TEXT NOT NULL PRIMARY KEY,
                        preFileId TEXT,
                        customerId TEXT,
                        type TEXT NOT NULL DEFAULT 'CALL',
                        text TEXT NOT NULL,
                        outcome TEXT,
                        noteDate TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL,
                        remoteId TEXT,
                        syncState TEXT NOT NULL DEFAULT 'PENDING_UPLOAD',
                        serverUpdatedAt INTEGER,
                        deletedAt INTEGER,
                        FOREIGN KEY(preFileId) REFERENCES pre_files(id) ON UPDATE NO ACTION ON DELETE CASCADE,
                        FOREIGN KEY(customerId) REFERENCES customers(id) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_notes_preFileId ON notes(preFileId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_notes_customerId ON notes(customerId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_notes_noteDate ON notes(noteDate)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_notes_syncState ON notes(syncState)")

                db.execSQL("ALTER TABLE follow_ups ADD COLUMN customerId TEXT")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_follow_ups_customerId ON follow_ups(customerId)")
            }
        }

        /**
         * نسخه‌ی ۴ → ۵:
         *  - جدول project_areas (متراژهای پروژه با شرایط مالی مخصوص هرکدام)
         *  - جدول attachments (فایل/عکس/ویدیو متصل به فایل پیش‌فروش یا واحد)
         *  - ستون approx_total_price روی projects (قیمت حدودی کل، مخصوص پروژه‌های سهامی)
         *  - ستون‌های fileType و areaId روی pre_files
         */
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS project_areas (
                        id TEXT NOT NULL PRIMARY KEY,
                        projectId TEXT NOT NULL,
                        label TEXT NOT NULL,
                        areaValue REAL,
                        totalPrice INTEGER,
                        depositAmount INTEGER,
                        bonusAmount INTEGER,
                        installmentCount INTEGER,
                        installmentAmount INTEGER,
                        installmentPeriod TEXT,
                        sortIndex INTEGER NOT NULL DEFAULT 0,
                        FOREIGN KEY(projectId) REFERENCES projects(id) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_project_areas_projectId ON project_areas(projectId)")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS attachments (
                        id TEXT NOT NULL PRIMARY KEY,
                        ownerType TEXT NOT NULL,
                        ownerId TEXT NOT NULL,
                        displayName TEXT NOT NULL,
                        mimeType TEXT NOT NULL,
                        uri TEXT NOT NULL,
                        sizeBytes INTEGER,
                        createdAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_attachments_ownerType ON attachments(ownerType)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_attachments_ownerId ON attachments(ownerId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_attachments_owner ON attachments(ownerType, ownerId)")

                db.execSQL("ALTER TABLE projects ADD COLUMN approx_total_price INTEGER")

                db.execSQL("ALTER TABLE pre_files ADD COLUMN fileType TEXT")
                db.execSQL("ALTER TABLE pre_files ADD COLUMN areaId TEXT")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_pre_files_areaId ON pre_files(areaId)")
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
                        defaultBonusAmount = 950_000_000L,
                        hasRanking = true,
                        defaultRanking = "رتبه ",
                        saleConditionCash = true,
                        saleConditionInstallment = true,
                        saleConditionExchange = true,
                        saleConditionNotes = "تهاتر فقط با خودرو ۲۰ یا پارس تا ۵۰۰ میلیون",
                        installmentCount = 12,
                        remainingInstallmentsCount = 6,
                        installmentAmount = 75_000_000L,
                        installmentPeriod = "ماهانه",
                        nextInstallmentDueDate = "1405/08/15",
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
                        saleConditionCash = true,
                        saleConditionInstallment = true,
                        installmentCount = 24,
                        installmentPeriod = "ماهانه",
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
                        saleConditionCash = true,
                        saleConditionInstallment = true,
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

                // مشتری نمونه (خریدار فایل خرازی)
                val c1Id = UUID.randomUUID().toString()
                database.customerDao().insert(
                    CustomerEntity(
                        id = c1Id,
                        name = "خانم محمدی",
                        phone = "09123334455",
                        role = "BUYER",
                        preFileId = pfId,
                        status = "ACTIVE",
                        notes = "به فایل پیش‌فروش علاقه‌مند است؛ شرایط اقساطی می‌خواهد",
                        createdAt = now,
                        updatedAt = now,
                    )
                )

                // مکالمات و نوت‌های تاریخ‌دار نمونه
                database.noteDao().insert(
                    NoteEntity(
                        preFileId = pfId,
                        customerId = c1Id,
                        type = "CALL",
                        text = "مکالمه‌ی تلفنی اول: معرفی فایل و شرایط پروژه، توضیح نحوه‌ی واریزی و امتیاز",
                        outcome = "موافق بازدید از پروژه شد",
                        noteDate = ir.pishfile.app.core.Formatters.addJalaliDays(today, -1),
                        createdAt = now,
                        updatedAt = now,
                    )
                )
                database.noteDao().insert(
                    NoteEntity(
                        preFileId = pfId,
                        customerId = c1Id,
                        type = "VISIT",
                        text = "بازدید حضوری از پروژه به همراه مشتری؛ بررسی بلوک و واحد",
                        outcome = "درخواست افزایش تعداد اقساط؛ تا آخر هفته پاسخ می‌دهد",
                        noteDate = today,
                        createdAt = now,
                        updatedAt = now,
                    )
                )

                // پیگیری با آلارم برای مشتری
                database.followUpDao().insert(
                    FollowUpEntity(
                        type = "CALL",
                        priority = "HIGH",
                        title = "تماس با خانم محمدی برای نهایی‌سازی شرایط اقساط",
                        contactPhone = "09123334455",
                        preFileId = pfId,
                        projectId = p1Id,
                        customerId = c1Id,
                        dueDate = today,
                        dueTime = "15:00",
                        status = Constants.FOLLOWUP_PENDING,
                        createdAt = now,
                        updatedAt = now,
                    )
                )
            }
        }
    }
}
