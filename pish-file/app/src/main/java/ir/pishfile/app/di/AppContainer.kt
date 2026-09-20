package ir.pishfile.app.di

import android.content.Context
import ir.pishfile.app.core.BackupManager
import ir.pishfile.app.data.local.PishFileDatabase
import ir.pishfile.app.data.repository.CustomerRepository
import ir.pishfile.app.data.repository.FollowUpRepository
import ir.pishfile.app.data.repository.PreFileRepository
import ir.pishfile.app.data.repository.ProjectRepository
import ir.pishfile.app.data.repository.SettingsRepository
import ir.pishfile.app.data.repository.UnitRepository
import ir.pishfile.app.data.sync.DemoSyncEngine
import ir.pishfile.app.data.sync.SyncEngine
import ir.pishfile.app.data.sync.SyncRepository

/**
 * ظرف وابستگی‌ها (Dependency Container).
 *
 * چرا دستی و بدون Hilt؟ چون پروژه تازه شروع شده و می‌خواهیم ساخت اولیه سریع و بدون
 * پیچیدگی annotation processing باشد. اگر پروژه بزرگ شد، انتقال به Hilt ساده است.
 */
interface AppContainer {
    val database: PishFileDatabase
    val projectRepository: ProjectRepository
    val unitRepository: UnitRepository
    val customerRepository: CustomerRepository
    val preFileRepository: PreFileRepository
    val followUpRepository: FollowUpRepository
    val settingsRepository: SettingsRepository
    val syncRepository: SyncRepository
    val backupManager: BackupManager
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    override val database: PishFileDatabase by lazy { PishFileDatabase.getInstance(context) }

    override val projectRepository: ProjectRepository by lazy { ProjectRepository(database.projectDao()) }
    override val unitRepository: UnitRepository by lazy { UnitRepository(database.unitDao()) }
    override val customerRepository: CustomerRepository by lazy { CustomerRepository(database.customerDao()) }
    override val followUpRepository: FollowUpRepository by lazy { FollowUpRepository(database.followUpDao()) }
    override val preFileRepository: PreFileRepository by lazy {
        PreFileRepository(
            database.preFileDao(),
            database.installmentDao(),
            database.unitDao(),
            database.projectDao(),
            database.customerDao(),
            database.followUpDao()
        )
    }

    override val settingsRepository: SettingsRepository by lazy { SettingsRepository(context) }

    /** موتور همگام‌سازی: فعلاً فقط محلی — آماده برای اتصال به سرور در آینده */
    override val syncRepository: SyncRepository by lazy {
        SyncRepository(database, listOf<SyncEngine>(DemoSyncEngine()))
    }

    override val backupManager: BackupManager by lazy { BackupManager(context, database) }
}
