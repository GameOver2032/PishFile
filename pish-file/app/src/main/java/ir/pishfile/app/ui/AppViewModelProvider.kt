package ir.pishfile.app.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ir.pishfile.app.PishFileApp
import ir.pishfile.app.di.AppContainer
import ir.pishfile.app.ui.viewmodel.DashboardViewModel
import ir.pishfile.app.ui.viewmodel.FollowUpsViewModel
import ir.pishfile.app.ui.viewmodel.PreFileDetailViewModel
import ir.pishfile.app.ui.viewmodel.PreFileEditViewModel
import ir.pishfile.app.ui.viewmodel.PreFileListViewModel
import ir.pishfile.app.ui.viewmodel.ProjectDetailViewModel
import ir.pishfile.app.ui.viewmodel.ProjectEditViewModel
import ir.pishfile.app.ui.viewmodel.ProjectListViewModel
import ir.pishfile.app.ui.viewmodel.SettingsViewModel
import ir.pishfile.app.ui.viewmodel.UnitDetailViewModel
import ir.pishfile.app.ui.viewmodel.UnitEditViewModel
import ir.pishfile.app.ui.viewmodel.UnitListViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            DashboardViewModel(
                container().preFileRepository,
                container().unitRepository,
                container().followUpRepository,
            )
        }
        initializer { ProjectListViewModel(container().projectRepository) }
        initializer { ProjectEditViewModel(container().projectRepository) }
        initializer {
            ProjectDetailViewModel(
                container().projectRepository,
                container().unitRepository,
                container().preFileRepository,
            )
        }

        initializer { UnitListViewModel(container().unitRepository, container().projectRepository) }
        initializer { UnitEditViewModel(container().unitRepository, container().projectRepository) }
        initializer {
            UnitDetailViewModel(
                container().unitRepository,
                container().projectRepository,
                container().preFileRepository,
            )
        }

        initializer { PreFileListViewModel(container().preFileRepository) }
        initializer {
            PreFileEditViewModel(
                container().preFileRepository,
                container().projectRepository,
                container().unitRepository,
            )
        }
        initializer {
            PreFileDetailViewModel(
                container().preFileRepository,
                container().unitRepository,
                container().followUpRepository,
            )
        }

        initializer {
            FollowUpsViewModel(
                container().followUpRepository,
                container().preFileRepository,
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
