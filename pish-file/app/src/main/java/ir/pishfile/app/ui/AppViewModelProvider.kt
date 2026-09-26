package ir.pishfile.app.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ir.pishfile.app.PishFileApp
import ir.pishfile.app.di.AppContainer
import ir.pishfile.app.ui.viewmodel.CustomerDetailViewModel
import ir.pishfile.app.ui.viewmodel.CustomerEditViewModel
import ir.pishfile.app.ui.viewmodel.CustomerListViewModel
import ir.pishfile.app.ui.viewmodel.FastViewModel
import ir.pishfile.app.ui.viewmodel.FollowUpsViewModel
import ir.pishfile.app.ui.viewmodel.QuickPickViewModel
import ir.pishfile.app.ui.viewmodel.PreFileDetailViewModel
import ir.pishfile.app.ui.viewmodel.PreFileEditViewModel
import ir.pishfile.app.ui.viewmodel.PreFileListViewModel
import ir.pishfile.app.ui.viewmodel.PreFileWizardViewModel
import ir.pishfile.app.ui.viewmodel.ProjectDetailViewModel
import ir.pishfile.app.ui.viewmodel.ProjectEditViewModel
import ir.pishfile.app.ui.viewmodel.ProjectListViewModel
import ir.pishfile.app.ui.viewmodel.SettingsViewModel
import ir.pishfile.app.ui.viewmodel.UnitDetailViewModel
import ir.pishfile.app.ui.viewmodel.UnitEditViewModel
import ir.pishfile.app.ui.viewmodel.UnitListViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer { ProjectListViewModel(container().projectRepository) }
        initializer { ProjectEditViewModel(container().projectRepository, container().projectAreaRepository) }
        initializer {
            ProjectDetailViewModel(
                container().projectRepository,
                container().unitRepository,
                container().preFileRepository,
                container().projectAreaRepository,
            )
        }

        initializer { UnitListViewModel(container().unitRepository, container().projectRepository) }
        initializer { UnitEditViewModel(container().unitRepository, container().projectRepository) }
        initializer {
            UnitDetailViewModel(
                container().unitRepository,
                container().projectRepository,
                container().preFileRepository,
                container().attachmentRepository,
            )
        }

        initializer { PreFileListViewModel(container().preFileRepository) }
        initializer {
            PreFileEditViewModel(
                container().preFileRepository,
                container().projectRepository,
                container().unitRepository,
                container().projectAreaRepository,
            )
        }
        initializer {
            PreFileWizardViewModel(
                container().preFileRepository,
                container().projectRepository,
                container().unitRepository,
                container().projectAreaRepository,
            )
        }
        initializer {
            PreFileDetailViewModel(
                container().preFileRepository,
                container().unitRepository,
                container().followUpRepository,
                container().customerRepository,
                container().noteRepository,
                container().attachmentRepository,
            )
        }

        initializer {
            FollowUpsViewModel(
                container().followUpRepository,
                container().preFileRepository,
                container().customerRepository,
                container().noteRepository,
                container().reminderScheduler,
            )
        }
        initializer {
            FastViewModel(
                container().preFileRepository,
                container().customerRepository,
                container().followUpRepository,
                container().noteRepository,
                container().reminderScheduler,
            )
        }
        initializer {
            QuickPickViewModel(
                container().unitRepository,
                container().preFileRepository,
                container().projectRepository,
            )
        }
        initializer {
            CustomerListViewModel(
                container().customerRepository,
                container().preFileRepository,
            )
        }
        initializer {
            CustomerEditViewModel(
                container().customerRepository,
                container().preFileRepository,
                container().unitRepository,
            )
        }
        initializer {
            CustomerDetailViewModel(
                container().customerRepository,
                container().preFileRepository,
                container().unitRepository,
                container().followUpRepository,
                container().noteRepository,
                container().reminderScheduler,
            )
        }
        initializer {
            SettingsViewModel(
                container().settingsRepository,
                container().database,
                container().backupManager,
            )
        }
    }
}

fun CreationExtras.container(): AppContainer {
    val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as PishFileApp
    return app.container
}
