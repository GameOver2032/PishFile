package ir.pishfile.app.di

import android.content.Context
import ir.pishfile.app.core.BackupManager
import ir.pishfile.app.core.ReminderScheduler
import ir.pishfile.app.data.local.PishFileDatabase
import ir.pishfile.app.data.repository.CustomerRepository
import ir.pishfile.app.data.repository.FollowUpRepository
import ir.pishfile.app.data.repository.NoteRepository
import ir.pishfile.app.data.repository.PreFileRepository
import ir.pishfile.app.data.repository.ProjectRepository
import ir.pishfile.app.data.repository.SettingsRepository
import ir.pishfile.app.data.repository.UnitRepository

interface AppContainer {
    val database: PishFileDatabase
    val projectRepository: ProjectRepository
    val unitRepository: UnitRepository
    val preFileRepository: PreFileRepository
    val followUpRepository: FollowUpRepository
    val customerRepository: CustomerRepository
    val noteRepository: NoteRepository
    val settingsRepository: SettingsRepository
    val backupManager: BackupManager
    val reminderScheduler: ReminderScheduler
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    override val database: PishFileDatabase by lazy {
        PishFileDatabase.getInstance(context)
    }

    override val projectRepository: ProjectRepository by lazy {
        ProjectRepository(database.projectDao())
    }

    override val unitRepository: UnitRepository by lazy {
        UnitRepository(database.unitDao())
    }

    override val followUpRepository: FollowUpRepository by lazy {
        FollowUpRepository(database.followUpDao())
    }

    override val customerRepository: CustomerRepository by lazy {
        CustomerRepository(database.customerDao())
    }

    override val noteRepository: NoteRepository by lazy {
        NoteRepository(database.noteDao())
    }

    override val preFileRepository: PreFileRepository by lazy {
        PreFileRepository(
            preFileDao = database.preFileDao(),
            unitDao = database.unitDao(),
            projectDao = database.projectDao(),
            followUpDao = database.followUpDao(),
        )
    }

    override val settingsRepository: SettingsRepository by lazy {
        SettingsRepository(context)
    }

    override val backupManager: BackupManager by lazy {
        BackupManager(context, database)
    }

    override val reminderScheduler: ReminderScheduler by lazy {
        ReminderScheduler(context)
    }
}
