package ir.pishfile.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.pishfile.app.data.local.dao.PreFileRow
import ir.pishfile.app.data.local.entity.FollowUpEntity
import ir.pishfile.app.data.local.entity.UnitEntity
import ir.pishfile.app.data.repository.FollowUpRepository
import ir.pishfile.app.data.repository.PreFileRepository
import ir.pishfile.app.data.repository.UnitRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * داشبورد:
 * ۱) پیگیری‌های امروز
 * ۲) آخرین فایل پیش‌فروش ثبت‌شده
 * ۳) آخرین واحد آماده ثبت‌شده
 */
class DashboardViewModel(
    private val preFileRepository: PreFileRepository,
    private val unitRepository: UnitRepository,
    private val followUpRepository: FollowUpRepository,
) : ViewModel() {

    /** پیگیری‌های امروز */
    val todayFollowUps: StateFlow<List<FollowUpEntity>> = followUpRepository.observeDueToday()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** آخرین فایل پیش‌فروش */
    val latestPreFile: StateFlow<PreFileRow?> = preFileRepository.observeLatest()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** آخرین واحد آماده ثبت‌شده */
    val latestAvailableUnit: StateFlow<UnitEntity?> = unitRepository.observeLatestAvailable()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun completeFollowUp(id: String) {
        viewModelScope.launch { followUpRepository.markDone(id) }
    }
}
