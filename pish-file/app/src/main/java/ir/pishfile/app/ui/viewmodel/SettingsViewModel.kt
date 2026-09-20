package ir.pishfile.app.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.pishfile.app.core.BackupManager
import ir.pishfile.app.data.repository.SettingsRepository
import ir.pishfile.app.data.sync.PendingChanges
import ir.pishfile.app.data.sync.SyncRepository
import ir.pishfile.app.data.sync.SyncResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * تنظیمات + ابزارهای داده (بکاپ، بازیابی، خروجی CSV) + وضعیت همگام‌سازی.
 */
class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val backupManager: BackupManager,
    private val syncRepository: SyncRepository,
    private val context: Context,
) : ViewModel() {

    val officeName = settingsRepository.officeName.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")
    val agentName = settingsRepository.agentName.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")
    val agentPhone = settingsRepository.agentPhone.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")
    val defaultCity = settingsRepository.defaultCity.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")
    val defaultInstallmentCount = settingsRepository.defaultInstallmentCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "24")
    val themeMode = settingsRepository.themeMode.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "system")
    val persianDigits = settingsRepository.persianDigits.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)
    val showAmountsInMillions = settingsRepository.showAmountsInMillions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)
    val autoGenerateInstallments = settingsRepository.autoGenerateInstallments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)
    val lastSyncAt = settingsRepository.lastSyncAt.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")
    val serverUrl = settingsRepository.serverUrl.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    private val _pending = MutableStateFlow(PendingChanges())
    val pending: StateFlow<PendingChanges> = _pending.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    val lastSyncResult: StateFlow<SyncResult?> = syncRepository.lastResult
    val isSyncing: StateFlow<Boolean> = syncRepository.isSyncing

    init {
        refreshPending()
    }

    fun setOfficeName(v: String) = viewModelScope.launch { settingsRepository.setOfficeName(v) }
    fun setAgentName(v: String) = viewModelScope.launch { settingsRepository.setAgentName(v) }
    fun setAgentPhone(v: String) = viewModelScope.launch { settingsRepository.setAgentPhone(v) }
    fun setDefaultCity(v: String) = viewModelScope.launch { settingsRepository.setDefaultCity(v) }
    fun setDefaultInstallmentCount(v: String) = viewModelScope.launch { settingsRepository.setDefaultInstallmentCount(v) }
    fun setThemeMode(v: String) = viewModelScope.launch { settingsRepository.setThemeMode(v) }
    fun setPersianDigits(v: Boolean) = viewModelScope.launch { settingsRepository.setPersianDigits(v) }
    fun setShowAmountsInMillions(v: Boolean) = viewModelScope.launch { settingsRepository.setShowAmountsInMillions(v) }
    fun setAutoGenerateInstallments(v: Boolean) = viewModelScope.launch { settingsRepository.setAutoGenerateInstallments(v) }

    fun refreshPending() {
        viewModelScope.launch { _pending.value = syncRepository.countPending() }
    }

    fun syncNow() {
        viewModelScope.launch {
            val result = syncRepository.syncNow()
            _message.value = result.error ?: "همگام‌سازی انجام شد (${result.uploadedCount} رکورد)"
            if (result.isSuccess) settingsRepository.setLastSyncAt(ir.pishfile.app.core.Formatters.todayJalali())
            refreshPending()
        }
    }

    /** خروجی بکاپ کامل JSON */
    fun exportBackup(onReady: (android.content.Intent) -> Unit) {
        viewModelScope.launch {
            val file = withContext(Dispatchers.IO) { backupManager.exportFullBackup() }
            _message.value = "بکاپ ساخته شد: ${file.name}"
            onReady(backupManager.shareFile(file, "application/json"))
        }
    }

    fun exportPreFilesCsv(onReady: (android.content.Intent) -> Unit) {
        viewModelScope.launch {
            val file = withContext(Dispatchers.IO) { backupManager.exportPreFilesCsv() }
            _message.value = "خروجی اکسل ساخته شد: ${file.name}"
            onReady(backupManager.shareFile(file, "text/csv"))
        }
    }

    fun exportInstallmentsCsv(onReady: (android.content.Intent) -> Unit) {
        viewModelScope.launch {
            val file = withContext(Dispatchers.IO) { backupManager.exportInstallmentsCsv() }
            _message.value = "خروجی اقساط ساخته شد: ${file.name}"
            onReady(backupManager.shareFile(file, "text/csv"))
        }
    }

    /** بازیابی از فایل بکاپ */
    fun importBackup(uri: android.net.Uri) {
        viewModelScope.launch {
            runCatching {
                val content = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                } ?: error("فایل قابل خواندن نیست")
                val summary = withContext(Dispatchers.IO) { backupManager.importFullBackup(content) }
                _message.value = "بازیابی انجام شد: ${summary.total} رکورد"
            }.onFailure { error ->
                _message.value = "خطا در بازیابی: ${error.message}"
            }
            refreshPending()
        }
    }

    fun clearMessage() { _message.value = null }
}
