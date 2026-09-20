package ir.pishfile.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.pishfile.app.data.local.dao.PreFileRow
import ir.pishfile.app.data.local.entity.FollowUpEntity
import ir.pishfile.app.data.repository.FollowUpRepository
import ir.pishfile.app.data.repository.PreFileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FollowUpsViewModel(
    private val repository: FollowUpRepository,
    private val preFileRepository: PreFileRepository,
) : ViewModel() {

    private val showDoneFlow = MutableStateFlow(false)

    val followUps: StateFlow<List<FollowUpEntity>> = showDoneFlow
        .flatMapLatest { showDone ->
            if (showDone) repository.observeAll() else repository.observePending()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val preFileRows: StateFlow<List<PreFileRow>> = preFileRepository.observeAllRows()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setShowDone(show: Boolean) { showDoneFlow.value = show }

    fun markDone(id: String) {
        viewModelScope.launch { repository.markDone(id) }
    }

    fun save(followUp: FollowUpEntity, onSaved: () -> Unit) {
        viewModelScope.launch {
            repository.save(followUp)
            onSaved()
        }
    }

    fun delete(id: String) {
        viewModelScope.launch { repository.delete(id) }
    }

    companion object {
        val types = listOf(
            "CALL" to "تماس تلفنی",
            "VISIT" to "بازدید حضوری",
            "MEETING" to "جلسه حضوری",
            "OTHER" to "سایر",
        )

        val priorities = listOf(
            "LOW" to "پایین",
            "NORMAL" to "معمولی",
            "HIGH" to "فوری",
        )

        fun typeLabel(type: String): String =
            types.firstOrNull { it.first == type }?.second ?: "پیگیری"
    }
}
