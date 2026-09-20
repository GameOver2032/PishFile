package ir.pishfile.app.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ir.pishfile.app.PishFileApp
import ir.pishfile.app.ui.viewmodel.CustomerDetailViewModel
import ir.pishfile.app.ui.viewmodel.CustomerEditViewModel
import ir.pishfile.app.ui.viewmodel.CustomerListViewModel
import ir.pishfile.app.ui.viewmodel.DashboardViewModel
import ir.pishfile.app.ui.viewmodel.FollowUpsViewModel
import ir.pishfile.app.ui.viewmodel.InstallmentsViewModel
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

/**
 * ساخت ViewModelها — همه از «ظرف وابستگی‌ها» تغذیه می‌شوند.
 * الگوی ساده و بدون کتابخانه‌ی اضافه؛ اگر پروژه بزرگ شد به Hilt منتقل می‌شود.
 */
object AppViewModelProvider {

    val Factory = viewModelFactory {

        initializer {
            DashboardViewModel(
                preFileRepository = container().preFileRepository,
                unitRepository = container().unitRepository,
                projectRepository = container().projectRepository,
                customerRepository = container().customerRepository,
                followUpRepository = container().followUpRepository,
            )
        }

        // --- پروژه‌ها ---
        initializer { ProjectListViewModel(container().projectRepository) }
        initializer {
            ProjectDetailViewModel(
                projectRepository = container().projectRepository,
                unitRepository = container().unitRepository,
                preFileRepository = container().preFileRepository,
            )
        }
        initializer { ProjectEditViewModel(container().projectRepository) }

        // --- واحدها ---
        initializer {
            UnitListViewModel(
                repository = container().unitRepository,
                projectRepository = container().projectRepository,
            )
        }
        initializer {
            UnitEditViewModel(
                repository = container().unitRepository,
                projectRepository = container().projectRepository,
            )
        }
        initializer {
            UnitDetailViewModel(
                unitRepository = container().unitRepository,
                projectRepository = container().projectRepository,
                preFileRepository = container().preFileRepository,
            )
        }

        // --- مشتری‌ها ---
        initializer { CustomerListViewModel(container().customerRepository) }
        initializer { CustomerEditViewModel(container().customerRepository) }
        initializer {
            CustomerDetailViewModel(
                customerRepository = container().customerRepository,
                preFileRepository = container().preFileRepository,
                followUpRepository = container().followUpRepository,
            )
        }

        // --- پیش‌فایل‌ها ---
        initializer { PreFileListViewModel(container().preFileRepository) }
        initializer {
            PreFileEditViewModel(
                repository = container().preFileRepository,
                projectRepository = container().projectRepository,
                unitRepository = container().unitRepository,
                customerRepository = container().customerRepository,
                settingsRepository = container().settingsRepository,
            )
        }
        initializer {
            PreFileDetailViewModel(
                repository = container().preFileRepository,
                unitRepository = container().unitRepository,
                customerRepository = container().customerRepository,
                followUpRepository = container().followUpRepository,
            )
        }

        // --- اقساط، پیگیری‌ها، تنظیمات ---
        initializer {
            InstallmentsViewModel(
                repository = container().preFileRepository,
                unitRepository = container().unitRepository,
            )
        }
        initializer {
            FollowUpsViewModel(
                repository = container().followUpRepository,
                preFileRepository = container().preFileRepository,
                customerRepository = container().customerRepository,
            )
        }
        initializer {
            SettingsViewModel(
                settingsRepository = container().settingsRepository,
                backupManager = container().backupManager,
                syncRepository = container().syncRepository,
                context = application().applicationContext,
            )
        }
    }
}

private fun CreationExtras.container() = application().container

private fun CreationExtras.application(): PishFileApp =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as PishFileApp)
