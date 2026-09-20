package ir.pishfile.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.pishfile.app.core.Formatters
import ir.pishfile.app.data.local.dao.FinanceSummary
import ir.pishfile.app.data.local.dao.PreFileRow
import ir.pishfile.app.data.local.entity.FollowUpEntity
import ir.pishfile.app.data.local.entity.InstallmentEntity
import ir.pishfile.app.data.repository.CustomerRepository
import ir.pishfile.app.data.repository.FollowUpRepository
import ir.pishfile.app.data.repository.PreFileRepository
import ir.pishfile.app.data.repository.ProjectRepository
import ir.pishfile.app.data.repository.UnitRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * داشبورد: نگاه یک‌نگاهی به وضعیت فروش.
 * اطلاعات کلیدی یک فروشنده‌ی املاک در یک صفحه.
 */
class DashboardViewModel(
    private val preFileRepository: PreFileRepository,
    private val unitRepository: UnitRepository,
    private val projectRepository: ProjectRepository,
    private val customerRepository: CustomerRepository,
    private val followUpRepository: FollowUpRepository,
) : ViewModel() {

    val financeSummary: StateFlow<FinanceSummary?> = preFileRepository.observeFinanceSummary()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val projectCount: StateFlow<Int> = projectRepository.observeCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val unitCount: StateFlow<Int> = unitRepository.observeCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val availableUnitCount: StateFlow<Int> = unitRepository.observeCountByStatus("AVAILABLE")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val reservedUnitCount: StateFlow<Int> = unitRepository.observeCountByStatus("RESERVED")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val soldUnitCount: StateFlow<Int> = unitRepository.observeCountByStatus("SOLD")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val customerCount: StateFlow<Int> = customerRepository.observeCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val preFileCount: StateFlow<Int> = preFileRepository.observeCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val totalReceivables: StateFlow<Long?> = preFileRepository.observeTotalReceivables()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val overdueAmount: StateFlow<Long?> = preFileRepository.observeOverdueAmount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** درآمد ۳۰ روز گذشته بر اساس مبالغ دریافتی اقساط */
    val receivedLast30Days: StateFlow<Long?> = preFileRepository
        .observeReceivedFrom(Formatters.addJalaliDays(Formatters.todayJalali(), -30))
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** سررسیدهای ۳۰ روز آینده */
    val upcomingInstallments: StateFlow<List<InstallmentEntity>> = preFileRepository
        .observeUpcomingInstallments(30)
        .map { it.take(8) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val overdueInstallments: StateFlow<List<InstallmentEntity>> = preFileRepository
        .observeOverdueInstallments()
        .map { it.take(8) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val pendingFollowUps: StateFlow<List<FollowUpEntity>> = followUpRepository.observeDueToday()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val latestPreFiles: StateFlow<List<PreFileRow>> = preFileRepository.observeAllRows()
        .map { it.take(5) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val unitStatusCounts = unitRepository.observeStatusCounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        // با هر بار باز شدن داشبورد، اقساط گذشته «معوق» علامت می‌خورند
        viewModelScope.launch { preFileRepository.markOverdueInstallments() }
    }

    fun completeFollowUp(id: String) {
        viewModelScope.launch { followUpRepository.markDone(id) }
    }
}
