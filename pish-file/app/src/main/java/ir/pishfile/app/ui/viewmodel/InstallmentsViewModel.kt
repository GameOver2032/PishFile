package ir.pishfile.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.pishfile.app.core.Formatters
import ir.pishfile.app.data.local.entity.InstallmentEntity
import ir.pishfile.app.data.repository.PreFileRepository
import ir.pishfile.app.data.repository.UnitRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** سررسیدها و اقساط — قلب پیگیری مالی پروژه‌های پیش‌فروش */
class InstallmentsViewModel(
    private val repository: PreFileRepository,
    unitRepository: UnitRepository,
) : ViewModel() {

    /** بازه‌ی نمایش: رو به جلو / معوق‌ها / همه */
    private var currentFilter: String = FILTER_UPCOMING

    private val today = Formatters.todayJalali()

    val upcoming: StateFlow<List<InstallmentEntity>> = repository.observeUpcomingInstallments(30)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val overdue: StateFlow<List<InstallmentEntity>> = repository.observeOverdueInstallments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val current: StateFlow<List<InstallmentEntity>> = combine(upcoming, overdue) { up, over ->
        when (currentFilter) {
            FILTER_OVERDUE -> over
            FILTER_ALL -> (over + up).sortedBy { it.dueDate }
            else -> up
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val overdueTotal: StateFlow<Long?> = repository.observeOverdueAmount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val upcomingTotal: StateFlow<Long?> = upcoming.map { list ->
        list.filter { it.status != "PAID" }.sumOf { it.amount - it.paidAmount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val openCount: StateFlow<Int> = upcoming.map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    fun setFilter(value: String) { currentFilter = value }

    fun markPaid(installmentId: String, amount: Long, method: String?, reference: String?) {
        viewModelScope.launch {
            repository.markInstallmentPaid(
                installmentId = installmentId,
                amountPaid = amount,
                paidDate = today,
                paymentMethod = method,
                referenceNumber = reference,
            )
        }
    }

    fun markUnpaid(installmentId: String) {
        viewModelScope.launch { repository.markInstallmentUnpaid(installmentId) }
    }

    fun refreshOverdue() {
        viewModelScope.launch { repository.markOverdueInstallments() }
    }

    companion object {
        const val FILTER_UPCOMING = "upcoming"
        const val FILTER_OVERDUE = "overdue"
        const val FILTER_ALL = "all"
    }
}
