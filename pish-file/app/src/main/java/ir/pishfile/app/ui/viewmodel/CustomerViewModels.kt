package ir.pishfile.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.pishfile.app.core.Constants
import ir.pishfile.app.core.ReminderScheduler
import ir.pishfile.app.data.local.dao.PreFileRow
import ir.pishfile.app.data.local.entity.CustomerEntity
import ir.pishfile.app.data.local.entity.FollowUpEntity
import ir.pishfile.app.data.local.entity.UnitEntity
import ir.pishfile.app.data.repository.CustomerRepository
import ir.pishfile.app.data.repository.FollowUpRepository
import ir.pishfile.app.data.repository.PreFileRepository
import ir.pishfile.app.data.repository.UnitRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

// ---------------------------------------------------------------------------
// فرم ویرایش / ثبت مشتری
// ---------------------------------------------------------------------------

data class CustomerForm(
    val id: String? = null,
    val name: String = "",
    val phone: String = "",
    val role: String = Constants.CUSTOMER_ROLE_BUYER,
    val status: String = Constants.CUSTOMER_ACTIVE,
    val notes: String = "",
    val preFileId: String? = null,
    val unitId: String? = null,
    val linkedFileLabel: String? = null,
    val linkedUnitLabel: String? = null,
)

class CustomerEditViewModel(
    private val customerRepository: CustomerRepository,
    private val preFileRepository: PreFileRepository,
    private val unitRepository: UnitRepository,
) : ViewModel() {

    private val formState = MutableStateFlow(CustomerForm())
    val form: StateFlow<CustomerForm> = formState.asStateFlow()

    /** هدف انتخاب‌شده در صفحه‌ی «از کجا شروع کنیم؟» (واحد و/یا فایل). */
    fun setTarget(unitId: String, preFileId: String) {
        val u = unitId.takeIf { it.isNotBlank() }
        val p = preFileId.takeIf { it.isNotBlank() }
        formState.update { it.copy(unitId = u, preFileId = p) }
        viewModelScope.launch {
            val fileLabel = p?.let {
                preFileRepository.observeRowById(it).first()
                    ?.let { row -> "${row.preFile.draftNumber} — ${row.projectName.orEmpty()}" }
            }
            val unitIdToUse = u ?: p?.let { preFileRepository.observeById(it).first()?.unitId }
            val unitLabel = unitIdToUse?.let { unitRepository.observeById(it).first()?.displayTitle }
            formState.update { f -> f.copy(linkedFileLabel = fileLabel, linkedUnitLabel = unitLabel) }
        }
    }

    fun load(customerId: String) {
        viewModelScope.launch {
            val c = customerRepository.getById(customerId) ?: return@launch
            formState.value = CustomerForm(
                id = c.id,
                name = c.name,
                phone = c.phone.orEmpty(),
                role = c.role,
                status = c.status,
                notes = c.notes.orEmpty(),
                preFileId = c.preFileId,
                unitId = c.unitId,
                linkedFileLabel = c.preFileId?.let {
                    preFileRepository.observeRowById(it).first()
                        ?.let { row -> "${row.preFile.draftNumber} — ${row.projectName.orEmpty()}" }
                },
                linkedUnitLabel = c.unitId?.let { unitRepository.observeById(it).first()?.displayTitle },
            )
        }
    }

    fun update(transform: (CustomerForm) -> CustomerForm) {
        formState.update(transform)
    }

    fun save(onSaved: (String) -> Unit) {
        val f = formState.value
        if (f.name.isBlank()) return
        viewModelScope.launch {
            val id = f.id ?: UUID.randomUUID().toString()
            customerRepository.save(
                CustomerEntity(
                    id = id,
                    name = f.name.trim(),
                    phone = f.phone.ifBlank { null },
                    role = f.role,
                    status = f.status,
                    notes = f.notes.ifBlank { null },
                    preFileId = f.preFileId,
                    unitId = f.unitId,
                )
            )
            onSaved(id)
        }
    }
}

// ---------------------------------------------------------------------------
// فهرست مشتریان
// ---------------------------------------------------------------------------

class CustomerListViewModel(
    private val customerRepository: CustomerRepository,
    preFileRepository: PreFileRepository,
) : ViewModel() {

    private val queryFlow = MutableStateFlow("")
    private val statusFilter = MutableStateFlow<String?>(null)

    val customers: StateFlow<List<CustomerEntity>> =
        combine(queryFlow.debounce(180), statusFilter) { q, s -> q to s }
            .flatMapLatest { (_, s) ->
                if (s == null) customerRepository.observeAll() else customerRepository.observeByStatus(s)
            }
            .map { list ->
                val q = queryFlow.value.trim()
                if (q.isBlank()) list
                else list.filter { it.name.contains(q, true) || (it.phone ?: "").contains(q, true) }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val preFileRows: StateFlow<List<PreFileRow>> = preFileRepository.observeAllRows()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setQuery(query: String) { queryFlow.value = query }
    fun setStatusFilter(status: String?) { statusFilter.value = status }
    fun delete(id: String) { viewModelScope.launch { customerRepository.delete(id) } }
}

// ---------------------------------------------------------------------------
// جزئیات مشتری
// ---------------------------------------------------------------------------

class CustomerDetailViewModel(
    private val customerRepository: CustomerRepository,
    private val preFileRepository: PreFileRepository,
    private val unitRepository: UnitRepository,
    private val followUpRepository: FollowUpRepository,
    private val reminderScheduler: ReminderScheduler,
) : ViewModel() {

    private val customerId = MutableStateFlow<String?>(null)

    val customer: StateFlow<CustomerEntity?> = customerId
        .flatMapLatest { id -> if (id == null) flowOf(null) else customerRepository.observeById(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val preFileRow: StateFlow<PreFileRow?> = customer
        .flatMapLatest { c ->
            val id = c?.preFileId
            if (id == null) flowOf(null) else preFileRepository.observeRowById(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val unit: StateFlow<UnitEntity?> = customer
        .flatMapLatest { c ->
            val id = c?.unitId
            if (id == null) flowOf(null) else unitRepository.observeById(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val followUps: StateFlow<List<FollowUpEntity>> = customerId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else followUpRepository.observeByCustomer(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val allCustomers: StateFlow<List<CustomerEntity>> = customerRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setCustomerId(id: String) { customerId.value = id }

    fun markStatus(status: String) {
        val c = customer.value ?: return
        viewModelScope.launch { customerRepository.markStatus(c.id, status) }
    }

    fun saveFollowUp(followUp: FollowUpEntity, onSaved: () -> Unit) {
        viewModelScope.launch {
            followUpRepository.save(followUp)
            if (followUp.status == Constants.FOLLOWUP_PENDING) {
                reminderScheduler.schedule(followUp)
            } else {
                reminderScheduler.cancel(followUp.id)
            }
            onSaved()
        }
    }

    fun markFollowUpDone(id: String) {
        viewModelScope.launch {
            reminderScheduler.cancel(id)
            followUpRepository.markDone(id)
        }
    }

    fun deleteFollowUp(id: String) {
        viewModelScope.launch {
            reminderScheduler.cancel(id)
            followUpRepository.delete(id)
        }
    }

    fun delete(onDone: () -> Unit) {
        val c = customer.value ?: return
        viewModelScope.launch {
            customerRepository.delete(c.id)
            onDone()
        }
    }
}
