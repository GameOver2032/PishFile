package ir.pishfile.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.pishfile.app.core.Formatters
import ir.pishfile.app.data.local.entity.FollowUpEntity
import ir.pishfile.app.data.repository.CustomerRepository
import ir.pishfile.app.data.repository.FollowUpRepository
import ir.pishfile.app.data.repository.PreFileRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** پیگیری‌ها: تماس‌ها، بازدیدها، جلسات و یادآوری‌ها */
@OptIn(ExperimentalCoroutinesApi::class)
class FollowUpsViewModel(
    private val repository: FollowUpRepository,
    private val preFileRepository: PreFileRepository,
    private val customerRepository: CustomerRepository,
) : ViewModel() {

    private val filterFlow = MutableStateFlow(false)

    val followUps: StateFlow<List<FollowUpEntity>> = filterFlow
        .flatMapLatest { done ->
            if (done) repository.observeAll().map { list -> list.filter { it.status != "PENDING" } }
            else repository.observePending()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val pendingCount: StateFlow<Int> = repository.observePendingCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val dueCount: StateFlow<Int> = repository.observeDueCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    /** نام مشتری برای هر پیگیری — برای نمایش در فهرست */
    val customerNames: StateFlow<Map<String, String>> = customerRepository.observeAll()
        .map { list -> list.associate { it.id to "${it.firstName} ${it.lastName}".trim() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    val customers = customerRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val preFileRows = preFileRepository.observeAllRows()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setShowDone(value: Boolean) {
        filterFlow.value = value
    }

    fun save(followUp: FollowUpEntity, onSaved: () -> Unit) {
        viewModelScope.launch {
            repository.save(followUp)
            onSaved()
        }
    }

    fun markDone(id: String, outcome: String? = null) {
        viewModelScope.launch { repository.markDone(id, outcome) }
    }

    fun delete(id: String) {
        viewModelScope.launch { repository.delete(id) }
    }

    companion object {
        val types = listOf("CALL" to "تماس", "VISIT" to "بازدید", "MEETING" to "جلسه", "MESSAGE" to "پیام", "REMINDER" to "یادآوری")

        val priorities = listOf("LOW" to "کم", "NORMAL" to "معمولی", "HIGH" to "مهم", "URGENT" to "فوری")

        fun typeLabel(code: String): String = types.firstOrNull { it.first == code }?.second ?: "تماس"

        fun priorityLabel(code: String): String = priorities.firstOrNull { it.first == code }?.second ?: "معمولی"

        fun today(): String = Formatters.todayJalali()
    }
}
