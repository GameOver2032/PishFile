package ir.pishfile.app.ui.viewmodel

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.pishfile.app.core.BackupManager
import ir.pishfile.app.core.Formatters
import ir.pishfile.app.data.local.PishFileDatabase
import ir.pishfile.app.data.repository.SettingsRepository
import ir.pishfile.app.data.sync.PendingSyncSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val database: PishFileDatabase,
    private val backupManager: BackupManager,
) : ViewModel() {

    val officeName: StateFlow<String> = settingsRepository.officeName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    val agentName: StateFlow<String> = settingsRepository.agentName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    val agentPhone: StateFlow<String> = settingsRepository.agentPhone
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    val defaultCity: StateFlow<String> = settingsRepository.defaultCity
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "تهران")

    val themeMode: StateFlow<String> = settingsRepository.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "system")

    val persianDigits: StateFlow<Boolean> = settingsRepository.persianDigits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    private val _pending = MutableStateFlow(PendingSyncSummary())
    val pending: StateFlow<PendingSyncSummary> = _pending.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun setOfficeName(v: String) = viewModelScope.launch { settingsRepository.setOfficeName(v) }
    fun setAgentName(v: String) = viewModelScope.launch { settingsRepository.setAgentName(v) }
    fun setAgentPhone(v: String) = viewModelScope.launch { settingsRepository.setAgentPhone(v) }
    fun setDefaultCity(v: String) = viewModelScope.launch { settingsRepository.setDefaultCity(v) }
    fun setThemeMode(v: String) = viewModelScope.launch { settingsRepository.setThemeMode(v) }
    fun setPersianDigits(v: Boolean) = viewModelScope.launch { settingsRepository.setPersianDigits(v) }
    fun clearMessage() { _message.value = null }

    fun refreshPending() {
        viewModelScope.launch {
            val projects = database.projectDao().getAll().size
            val units = database.unitDao().getAll().size
            val preFiles = database.preFileDao().getAll().size
            val followUps = database.followUpDao().getAll().size
            _pending.value = PendingSyncSummary(
                projects = projects,
                units = units,
                preFiles = preFiles,
                followUps = followUps,
            )
        }
    }

    fun exportBackup(onReady: (Intent) -> Unit) {
        viewModelScope.launch {
            try {
                val file = withContext(Dispatchers.IO) { backupManager.exportFullBackup() }
                onReady(backupManager.shareFile(file, "application/json"))
                _message.value = "فایل پشتیبان با موفقیت ساخته شد"
            } catch (e: Exception) {
                _message.value = "خطا در خروجی: ${e.message}"
            }
        }
    }

    fun exportPreFilesCsv(onReady: (Intent) -> Unit) {
        viewModelScope.launch {
            try {
                val file = withContext(Dispatchers.IO) { backupManager.exportPreFilesCsv() }
                onReady(backupManager.shareFile(file, "text/csv"))
                _message.value = "خروجی اکسل پیش‌فروش‌ها ساخته شد"
            } catch (e: Exception) {
                _message.value = "خطا در ساخت اکسل: ${e.message}"
            }
        }
    }

    fun importBackup(json: String) {
        viewModelScope.launch {
            try {
                val summary = withContext(Dispatchers.IO) { backupManager.importFullBackup(json) }
                _message.value = "بازیابی کامل شد: ${Formatters.toPersianDigits(summary.total.toString())} رکورد"
                refreshPending()
            } catch (e: Exception) {
                _message.value = "خطا در بازیابی: ${e.message}"
            }
        }
    }

    fun exportProjectsBackup(onReady: (Intent) -> Unit) {
        viewModelScope.launch {
            try {
                val file = withContext(Dispatchers.IO) { backupManager.exportProjectsBackup() }
                onReady(backupManager.shareFile(file, "application/json"))
                _message.value = "خروجی JSON پروژه‌ها ساخته شد"
            } catch (e: Exception) {
                _message.value = "خطا در خروجی: ${e.message}"
            }
        }
    }

    fun importProjectsBackup(json: String) {
        viewModelScope.launch {
            try {
                val (projects, areas) = withContext(Dispatchers.IO) { backupManager.importProjectsBackup(json) }
                _message.value = "بازیابی پروژه‌ها: ${Formatters.toPersianDigits(projects.toString())} پروژه، " +
                    "${Formatters.toPersianDigits(areas.toString())} متراژ"
                refreshPending()
            } catch (e: Exception) {
                _message.value = "خطا در بازیابی پروژه‌ها: ${e.message}"
            }
        }
    }

    fun exportUnitsBackup(onReady: (Intent) -> Unit) {
        viewModelScope.launch {
            try {
                val file = withContext(Dispatchers.IO) { backupManager.exportUnitsBackup() }
                onReady(backupManager.shareFile(file, "application/json"))
                _message.value = "خروجی JSON واحدها ساخته شد"
            } catch (e: Exception) {
                _message.value = "خطا در خروجی: ${e.message}"
            }
        }
    }

    fun importUnitsBackup(json: String) {
        viewModelScope.launch {
            try {
                val units = withContext(Dispatchers.IO) { backupManager.importUnitsBackup(json) }
                _message.value = "بازیابی واحدها: ${Formatters.toPersianDigits(units.toString())} واحد"
                refreshPending()
            } catch (e: Exception) {
                _message.value = "خطا در بازیابی واحدها: ${e.message}"
            }
        }
    }
}
