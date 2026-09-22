package ir.pishfile.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.pishfile.app.core.ReminderScheduler
import ir.pishfile.app.data.local.entity.FollowUpEntity
import ir.pishfile.app.data.repository.CustomerRepository
import ir.pishfile.app.data.repository.FollowUpRepository
import ir.pishfile.app.data.repository.PreFileRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyList
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * صفحه‌ی «سریع» (خانه‌ی برنامه): آمار کلی و پیگیری‌های امروز.
 */
class FastViewModel(
    preFileRepository: PreFileRepository,
    customerRepository: CustomerRepository,
    followUpRepository: FollowUpRepository,
    private val reminderScheduler: ReminderScheduler,
) : ViewModel() {

    val preFileCount: StateFlow<Int> = preFileRepository.observeCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val customerCount: StateFlow<Int> = customerRepository.observeCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val todayFollowUps: StateFlow<List<FollowUpEntity>> =
        followUpRepository.observeDueToday()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun completeFollowUp(id: String) {
        viewModelScope.launch {
            reminderScheduler.cancel(id)
            followUpRepository.markDone(id)
        }
    }
}
