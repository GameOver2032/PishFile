package ir.pishfile.app.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.pishfile.app.core.Constants
import ir.pishfile.app.core.Formatters
import ir.pishfile.app.data.local.entity.CustomerEntity
import ir.pishfile.app.data.local.entity.FollowUpEntity
import ir.pishfile.app.data.local.entity.PreFileEntity
import ir.pishfile.app.data.repository.CustomerRepository
import ir.pishfile.app.data.repository.FollowUpRepository
import ir.pishfile.app.data.repository.PreFileRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CustomerForm(
    val title: String = "آقای",
    val entityType: String = "حقیقی",
    val firstName: String = "",
    val lastName: String = "",
    val fatherName: String = "",
    val companyName: String = "",
    val nationalId: String = "",
    val idNumber: String = "",
    val birthDate: String = "",
    val registrationNumber: String = "",
    val economicCode: String = "",
    val phonePrimary: String = "",
    val phoneSecondary: String = "",
    val whatsapp: String = "",
    val email: String = "",
    val province: String = "",
    val city: String = "",
    val address: String = "",
    val postalCode: String = "",
    val job: String = "",
    val workAddress: String = "",
    val workPhone: String = "",
    val bankName: String = "",
    val iban: String = "",
    val status: String = Constants.CUSTOMER_LEAD,
    val source: String = "اینستاگرام",
    val referredBy: String = "",
    val creditScore: String = "",
    val creditLimit: String = "",
    val hasBouncedCheque: Boolean = false,
    val isReturningCustomer: Boolean = false,
    val notes: String = "",
    val tags: String = "",
) {
    val isLegalEntity: Boolean get() = entityType == "حقوقی"

    fun toEntity(existing: CustomerEntity? = null): CustomerEntity {
        val name = if (isLegalEntity) (companyName.trim().ifBlank { lastName.trim() }) else firstName.trim()
        return CustomerEntity(
            id = existing?.id ?: java.util.UUID.randomUUID().toString(),
            title = if (isLegalEntity) "شرکت" else title,
            firstName = name.ifBlank { "بدون نام" },
            lastName = lastName.trim(),
            fatherName = fatherName.ifBlank { null },
            companyName = companyName.ifBlank { null },
            nationalId = nationalId.ifBlank { null },
            idNumber = idNumber.ifBlank { null },
            birthDate = birthDate.ifBlank { null },
            registrationNumber = registrationNumber.ifBlank { null },
            economicCode = economicCode.ifBlank { null },
            phonePrimary = phonePrimary.ifBlank { null },
            phoneSecondary = phoneSecondary.ifBlank { null },
            whatsapp = whatsapp.ifBlank { null },
            email = email.ifBlank { null },
            province = province.ifBlank { null },
            city = city.ifBlank { null },
            address = address.ifBlank { null },
            postalCode = postalCode.ifBlank { null },
            job = job.ifBlank { null },
            workAddress = workAddress.ifBlank { null },
            workPhone = workPhone.ifBlank { null },
            bankName = bankName.ifBlank { null },
            iban = iban.ifBlank { null },
            status = status,
            source = sourceLabelToCode(source),
            referredBy = referredBy.ifBlank { null },
            creditScore = Formatters.parseLong(creditScore)?.toInt(),
            creditLimit = Formatters.parseLong(creditLimit),
            hasBouncedCheque = hasBouncedCheque,
            isReturningCustomer = isReturningCustomer,
            entityType = if (isLegalEntity) "LEGAL" else "INDIVIDUAL",
            notes = notes.ifBlank { null },
            tags = tags.ifBlank { null },
            isFavorite = existing?.isFavorite ?: false,
            createdAt = existing?.createdAt ?: System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
        )
    }

    companion object {
        fun sourceLabelToCode(label: String): String =
            Constants.customerSources.firstOrNull { Constants.customerSourceLabel(it) == label }
                ?: Constants.SOURCE_OTHER

        fun sourceOptions(): List<String> = Constants.customerSources.map { Constants.customerSourceLabel(it) }

        fun from(entity: CustomerEntity) = CustomerForm(
            title = entity.title ?: "آقای",
            entityType = if (entity.entityType == "LEGAL") "حقوقی" else "حقیقی",
            firstName = entity.firstName,
            lastName = entity.lastName,
            fatherName = entity.fatherName.orEmpty(),
            companyName = entity.companyName.orEmpty(),
            nationalId = entity.nationalId.orEmpty(),
            idNumber = entity.idNumber.orEmpty(),
            birthDate = entity.birthDate.orEmpty(),
            registrationNumber = entity.registrationNumber.orEmpty(),
            economicCode = entity.economicCode.orEmpty(),
            phonePrimary = entity.phonePrimary.orEmpty(),
            phoneSecondary = entity.phoneSecondary.orEmpty(),
            whatsapp = entity.whatsapp.orEmpty(),
            email = entity.email.orEmpty(),
            province = entity.province.orEmpty(),
            city = entity.city.orEmpty(),
            address = entity.address.orEmpty(),
            postalCode = entity.postalCode.orEmpty(),
            job = entity.job.orEmpty(),
            workAddress = entity.workAddress.orEmpty(),
            workPhone = entity.workPhone.orEmpty(),
            bankName = entity.bankName.orEmpty(),
            iban = entity.iban.orEmpty(),
            status = entity.status,
            source = Constants.customerSourceLabel(entity.source),
            referredBy = entity.referredBy.orEmpty(),
            creditScore = entity.creditScore?.toString().orEmpty(),
            creditLimit = entity.creditLimit?.toString().orEmpty(),
            hasBouncedCheque = entity.hasBouncedCheque,
            isReturningCustomer = entity.isReturningCustomer,
            notes = entity.notes.orEmpty(),
            tags = entity.tags.orEmpty(),
        )
    }
}

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class CustomerListViewModel(private val repository: CustomerRepository) : ViewModel() {

    private val queryFlow = MutableStateFlow("")
    private val statusFilter = MutableStateFlow<String?>(null)

    val customers: StateFlow<List<CustomerEntity>> = queryFlow
        .debounce(180)
        .flatMapLatest { query ->
            val base = if (query.isBlank()) repository.observeAll() else repository.search(query)
            base
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val filtered: StateFlow<List<CustomerEntity>> = kotlinx.coroutines.flow.combine(customers, statusFilter) { list, status ->
        if (status == null) list else list.filter { it.status == status }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val totalCount: StateFlow<Int> = repository.observeCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    fun setQuery(query: String) { queryFlow.value = query }
    fun setStatusFilter(status: String?) { statusFilter.value = status }

    fun toggleFavorite(customer: CustomerEntity) {
        viewModelScope.launch { repository.save(customer.copy(isFavorite = !customer.isFavorite)) }
    }

    fun delete(id: String) { viewModelScope.launch { repository.delete(id) } }
}

class CustomerEditViewModel(private val repository: CustomerRepository) : ViewModel() {

    var form by mutableStateOf(CustomerForm())
        private set

    private var existing: CustomerEntity? = null
    var isLoaded by mutableStateOf(false)
        private set

    fun load(id: String) {
        if (isLoaded || id.isBlank()) return
        viewModelScope.launch {
            repository.getById(id)?.let {
                existing = it
                form = CustomerForm.from(it)
            }
            isLoaded = true
        }
    }

    fun markNew() { isLoaded = true }

    fun update(transform: (CustomerForm) -> CustomerForm) { form = transform(form) }

    fun save(onSaved: (String) -> Unit) {
        viewModelScope.launch {
            val saved = form.toEntity(existing)
            repository.save(saved)
            onSaved(saved.id)
        }
    }
}

class CustomerDetailViewModel(
    private val customerRepository: CustomerRepository,
    private val preFileRepository: PreFileRepository,
    private val followUpRepository: FollowUpRepository,
) : ViewModel() {

    private val customerId = MutableStateFlow<String?>(null)

    val customer: StateFlow<CustomerEntity?> = customerId
        .flatMapLatest { id -> if (id == null) flowOf(null) else customerRepository.observeById(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val preFiles: StateFlow<List<ir.pishfile.app.data.local.dao.PreFileRow>> = customerId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else preFileRepository.observeRowsByCustomer(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val followUps: StateFlow<List<FollowUpEntity>> = customerId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else followUpRepository.observeByCustomer(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setCustomerId(id: String) { customerId.value = id }

    fun toggleFavorite() {
        val current = customer.value ?: return
        viewModelScope.launch { customerRepository.save(current.copy(isFavorite = !current.isFavorite)) }
    }

    fun delete(onDone: () -> Unit) {
        val id = customerId.value ?: return
        viewModelScope.launch {
            customerRepository.delete(id)
            onDone()
        }
    }
}

/** مجموع مبالغ قراردادهای یک مشتری */
fun List<PreFileEntity>.totalValue(): Long = sumOf { it.effectivePrice }

fun List<PreFileEntity>.totalPaid(): Long = sumOf { it.paidAmount }
