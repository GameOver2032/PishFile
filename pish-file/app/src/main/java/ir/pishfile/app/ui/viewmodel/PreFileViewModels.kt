package ir.pishfile.app.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.pishfile.app.core.Constants
import ir.pishfile.app.core.Formatters
import ir.pishfile.app.data.local.dao.PreFileRow
import ir.pishfile.app.data.local.entity.CustomerEntity
import ir.pishfile.app.data.local.entity.InstallmentEntity
import ir.pishfile.app.data.local.entity.PreFileEntity
import ir.pishfile.app.data.local.entity.ProjectEntity
import ir.pishfile.app.data.local.entity.UnitEntity
import ir.pishfile.app.data.repository.CustomerRepository
import ir.pishfile.app.data.repository.FollowUpRepository
import ir.pishfile.app.data.repository.PreFileRepository
import ir.pishfile.app.data.repository.ProjectRepository
import ir.pishfile.app.data.repository.SettingsRepository
import ir.pishfile.app.data.repository.UnitRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * فرم پیش‌فایل — پرجزئیات‌ترین فرم برنامه.
 * همه‌ی شرایط مالی و تعهدات در همین ساختار جمع شده است.
 */
data class PreFileForm(
    val draftNumber: String = "",
    val draftDate: String = Formatters.todayJalali(),
    val projectId: String = "",
    val unitId: String = "",
    val customerId: String = "",
    val salesAgentName: String = "",
    val salesAgentPhone: String = "",
    val trackingCode: String = "",
    val totalPrice: String = "",
    val pricePerMeter: String = "",
    val discount: String = "",
    val finalPrice: String = "",
    val prepayment: String = "",
    val installmentCount: String = "",
    val installmentAmount: String = "",
    val installmentPeriod: String = "ماهانه",
    val installmentStartDate: String = "",
    val paymentType: String = "اقساطی",
    val sellerCommitment: String = "",
    val buyerCommitment: String = "",
    val penaltyClause: String = "",
    val cancellationTerms: String = "",
    val deedDate: String = "",
    val deedOffice: String = "",
    val deliveryDate: String = "",
    val isUnitMortgaged: Boolean = false,
    val guaranteeType: String = "",
    val chequeCount: String = "",
    val chequeAmount: String = "",
    val exchangeDetails: String = "",
    val status: String = Constants.PREFILE_DRAFT,
    val notes: String = "",
) {
    val totalPriceValue: Long get() = Formatters.parseLong(totalPrice) ?: 0L
    val discountValue: Long get() = Formatters.parseLong(discount) ?: 0L
    val prepaymentValue: Long get() = Formatters.parseLong(prepayment) ?: 0L

    val finalPriceValue: Long
        get() = Formatters.parseLong(finalPrice) ?: (totalPriceValue - discountValue)

    /** مبلغ قابل قسط‌بندی */
    val remainingValue: Long get() = (finalPriceValue - prepaymentValue).coerceAtLeast(0L)

    /** مبلغ هر قسط پیشنهادی */
    fun suggestedInstallment(): Long {
        val count = Formatters.parseLong(installmentCount)?.toInt() ?: 0
        return if (count > 0) remainingValue / count else 0L
    }

    /** مبلغ کل قرارداد بر اساس متراژ واحد و قیمت هر متر */
    fun computedFromUnit(unit: UnitEntity): Long? {
        val area = unit.grossArea
        val perMeter = Formatters.parseLong(pricePerMeter) ?: unit.pricePerMeter
        return if (area != null && perMeter != null) (area * perMeter).toLong() else unit.finalPrice ?: unit.totalPrice
    }

    fun installmentPeriodMonths(): Int = when (installmentPeriod) {
        "دو ماهه" -> 2
        "فصلی" -> 3
        "شش‌ماهه" -> 6
        "سالانه" -> 12
        else -> 1
    }

    fun toEntity(existing: PreFileEntity? = null, unit: UnitEntity? = null): PreFileEntity {
        val final = finalPriceValue
        return PreFileEntity(
            id = existing?.id ?: java.util.UUID.randomUUID().toString(),
            draftNumber = draftNumber.ifBlank { "PF-${Formatters.todayJalali().substringBefore('/')}-0001" },
            draftDate = draftDate.ifBlank { Formatters.todayJalali() },
            projectId = projectId,
            unitId = unitId.ifBlank { null },
            customerId = customerId.ifBlank { null },
            salesAgentName = salesAgentName.ifBlank { null },
            salesAgentPhone = salesAgentPhone.ifBlank { null },
            trackingCode = trackingCode.ifBlank { null },
            unitBlock = unit?.block,
            unitNumber = unit?.unitNumber,
            unitFloor = unit?.floor,
            unitArea = unit?.grossArea,
            totalPrice = totalPriceValue,
            pricePerMeter = Formatters.parseLong(pricePerMeter) ?: unit?.pricePerMeter,
            discount = discountValue.takeIf { it > 0 },
            finalPrice = final,
            prepayment = prepaymentValue.takeIf { it > 0 },
            paidAmount = existing?.paidAmount ?: 0L,
            remainingAmount = final - (existing?.paidAmount ?: 0L),
            installmentCount = Formatters.parseLong(installmentCount)?.toInt(),
            installmentAmount = Formatters.parseLong(installmentAmount) ?: suggestedInstallment().takeIf { it > 0 },
            installmentPeriod = installmentPeriod,
            installmentStartDate = installmentStartDate.ifBlank { null },
            paymentType = paymentTypeLabelToCode(paymentType),
            sellerCommitment = sellerCommitment.ifBlank { null },
            buyerCommitment = buyerCommitment.ifBlank { null },
            penaltyClause = penaltyClause.ifBlank { null },
            cancellationTerms = cancellationTerms.ifBlank { null },
            deedDate = deedDate.ifBlank { null },
            deedOffice = deedOffice.ifBlank { null },
            deliveryDate = deliveryDate.ifBlank { null },
            isUnitMortgaged = isUnitMortgaged,
            guaranteeType = guaranteeType.ifBlank { null },
            chequeCount = Formatters.parseLong(chequeCount)?.toInt(),
            chequeAmount = Formatters.parseLong(chequeAmount),
            exchangeDetails = exchangeDetails.ifBlank { null },
            status = status,
            confirmedDate = existing?.confirmedDate ?: if (status == Constants.PREFILE_CONFIRMED) Formatters.todayJalali() else null,
            notes = notes.ifBlank { null },
            isFavorite = existing?.isFavorite ?: false,
            createdAt = existing?.createdAt ?: System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
        )
    }

    companion object {
        fun paymentTypeLabelToCode(label: String): String =
            Constants.paymentTypes.firstOrNull { Constants.paymentTypeLabel(it) == label } ?: Constants.PAYMENT_INSTALLMENT

        fun paymentTypeOptions(): List<String> = Constants.paymentTypes.map { Constants.paymentTypeLabel(it) }

        val installmentPeriods = listOf("ماهانه", "دو ماهه", "فصلی", "شش‌ماهه", "سالانه")

        fun from(entity: PreFileEntity) = PreFileForm(
            draftNumber = entity.draftNumber,
            draftDate = entity.draftDate,
            projectId = entity.projectId,
            unitId = entity.unitId.orEmpty(),
            customerId = entity.customerId.orEmpty(),
            salesAgentName = entity.salesAgentName.orEmpty(),
            salesAgentPhone = entity.salesAgentPhone.orEmpty(),
            trackingCode = entity.trackingCode.orEmpty(),
            totalPrice = entity.totalPrice.toString(),
            pricePerMeter = entity.pricePerMeter?.toString().orEmpty(),
            discount = entity.discount?.toString().orEmpty(),
            finalPrice = entity.finalPrice?.toString().orEmpty(),
            prepayment = entity.prepayment?.toString().orEmpty(),
            installmentCount = entity.installmentCount?.toString().orEmpty(),
            installmentAmount = entity.installmentAmount?.toString().orEmpty(),
            installmentPeriod = entity.installmentPeriod ?: "ماهانه",
            installmentStartDate = entity.installmentStartDate.orEmpty(),
            paymentType = Constants.paymentTypeLabel(entity.paymentType),
            sellerCommitment = entity.sellerCommitment.orEmpty(),
            buyerCommitment = entity.buyerCommitment.orEmpty(),
            penaltyClause = entity.penaltyClause.orEmpty(),
            cancellationTerms = entity.cancellationTerms.orEmpty(),
            deedDate = entity.deedDate.orEmpty(),
            deedOffice = entity.deedOffice.orEmpty(),
            deliveryDate = entity.deliveryDate.orEmpty(),
            isUnitMortgaged = entity.isUnitMortgaged,
            guaranteeType = entity.guaranteeType.orEmpty(),
            chequeCount = entity.chequeCount?.toString().orEmpty(),
            chequeAmount = entity.chequeAmount?.toString().orEmpty(),
            exchangeDetails = entity.exchangeDetails.orEmpty(),
            status = entity.status,
            notes = entity.notes.orEmpty(),
        )
    }
}

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class PreFileListViewModel(private val repository: PreFileRepository) : ViewModel() {

    private val queryFlow = MutableStateFlow("")
    private val statusFilter = MutableStateFlow<String?>(null)

    val preFiles: StateFlow<List<PreFileRow>> = combine(
        queryFlow.debounce(180),
        statusFilter,
    ) { query, status -> query to status }
        .flatMapLatest { (query, status) ->
            val base = if (query.isBlank()) repository.observeAllRows() else repository.search(query)
            if (status == null) base else base.map { rows -> rows.filter { it.preFile.status == status } }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val statusCounts = repository.observeStatusCounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val totalCount = repository.observeCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    fun setQuery(query: String) { queryFlow.value = query }
    fun setStatusFilter(status: String?) { statusFilter.value = status }

    fun delete(id: String) { viewModelScope.launch { repository.delete(id) } }

    fun changeStatus(preFile: PreFileEntity, status: String) {
        viewModelScope.launch { repository.save(preFile.copy(status = status)) }
    }
}

class PreFileEditViewModel(
    private val repository: PreFileRepository,
    private val projectRepository: ProjectRepository,
    private val unitRepository: UnitRepository,
    private val customerRepository: CustomerRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    var form by mutableStateOf(PreFileForm())
        private set

    var projects by mutableStateOf<List<ProjectEntity>>(emptyList())
        private set
    var customers by mutableStateOf<List<CustomerEntity>>(emptyList())
        private set
    var availableUnits by mutableStateOf<List<UnitEntity>>(emptyList())
        private set

    var autoGenerateInstallments by mutableStateOf(true)
        private set

    private var existing: PreFileEntity? = null
    private var selectedUnit: UnitEntity? = null

    var isLoaded by mutableStateOf(false)
        private set

    var isSaved by mutableStateOf(false)
        private set

    private var draftsThisSession = 0

    fun load(id: String) {
        if (isLoaded || id.isBlank()) return
        viewModelScope.launch {
            prepareLookups()
            repository.getById(id)?.let { entity ->
                existing = entity
                selectedUnit = entity.unitId?.let { unitRepository.getById(it) }
                form = PreFileForm.from(entity)
                if (form.projectId.isNotBlank()) {
                    availableUnits = unitRepository.getByProject(form.projectId)
                }
            }
            autoGenerateInstallments = settingsRepository.autoGenerateInstallments.first()
            isLoaded = true
        }
    }

    fun startNew(projectId: String?, unitId: String?, customerId: String?) {
        if (isLoaded) return
        viewModelScope.launch {
            prepareLookups()
            val settings = settingsRepository
            val defaultProject = projectId?.takeIf { it.isNotBlank() }
                ?: projects.firstOrNull()?.id.orEmpty()
            val agent = settings.agentName.first()
            val agentPhone = settings.agentPhone.first()
            val count = settings.defaultInstallmentCount.first()

            availableUnits = unitRepository.getByProject(defaultProject)
            selectedUnit = unitId?.takeIf { it.isNotBlank() }?.let { unitRepository.getById(it) }
                ?: availableUnits.firstOrNull { it.status == Constants.UNIT_AVAILABLE }

            autoGenerateInstallments = settings.autoGenerateInstallments.first()

            form = PreFileForm(
                draftNumber = repository.nextDraftNumber(),
                projectId = defaultProject,
                unitId = selectedUnit?.id.orEmpty(),
                customerId = customerId?.takeIf { it.isNotBlank() }.orEmpty(),
                salesAgentName = agent,
                salesAgentPhone = agentPhone,
                installmentPeriod = "ماهانه",
                installmentCount = count,
                installmentStartDate = Formatters.addJalaliMonths(Formatters.todayJalali(), 1),
                prepayment = selectedUnit?.prepaymentSuggestion?.toString().orEmpty(),
            ).let { initial ->
                selectedUnit?.let { unit -> applyUnitToForm(initial, unit) } ?: initial
            }
            isLoaded = true
        }
    }

    private suspend fun prepareLookups() {
        projects = projectRepository.getAll()
        customers = customerRepository.getAll()
    }

    /** با انتخاب واحد، مشخصات و قیمت‌ها خودکار پر می‌شوند (سرعت کار فروش) */
    private suspend fun applyUnitToForm(base: PreFileForm, unit: UnitEntity): PreFileForm {
        val pricePerMeter = unit.pricePerMeter ?: unit.totalPrice?.let { total ->
            unit.grossArea?.takeIf { it > 0 }?.let { (total / it).toLong() }
        }
        val totalPrice = unit.finalPrice ?: unit.totalPrice
            ?: (unit.grossArea?.let { area -> pricePerMeter?.let { (area * it).toLong() } })

        return base.copy(
            unitId = unit.id,
            pricePerMeter = pricePerMeter?.toString().orEmpty(),
            totalPrice = totalPrice?.toString().orEmpty(),
            finalPrice = totalPrice?.toString().orEmpty(),
            prepayment = base.prepayment.ifBlank { unit.prepaymentSuggestion?.toString().orEmpty() },
            installmentCount = base.installmentCount.ifBlank { unit.suggestedInstallmentCount?.toString().orEmpty() },
            installmentAmount = unit.suggestedInstallment?.toString().orEmpty(),
            deliveryDate = base.deliveryDate.ifBlank { unit.deliveryDate.orEmpty() },
        )
    }

    fun selectProject(projectId: String) {
        viewModelScope.launch {
            availableUnits = unitRepository.getByProject(projectId)
            val project = projects.firstOrNull { it.id == projectId }
            form = form.copy(
                projectId = projectId,
                unitId = "",
                pricePerMeter = project?.salePricePerMeter?.toString() ?: form.pricePerMeter,
                deliveryDate = form.deliveryDate.ifBlank { project?.deliveryDate.orEmpty() },
            )
            selectedUnit = null
        }
    }

    fun selectUnit(unitId: String) {
        viewModelScope.launch {
            val unit = availableUnits.firstOrNull { it.id == unitId }
            selectedUnit = unit
            if (unit != null) form = applyUnitToForm(form, unit) else form = form.copy(unitId = "")
        }
    }

    fun update(transform: (PreFileForm) -> PreFileForm) { form = transform(form) }

    fun save(onSaved: (String) -> Unit) {
        viewModelScope.launch {
            if (form.projectId.isBlank()) return@launch
            val entity = form.toEntity(existing, selectedUnit)
            val shouldGenerate = autoGenerateInstallments &&
                (existing == null || existing?.installmentCount != entity.installmentCount ||
                    existing?.installmentStartDate != entity.installmentStartDate)
            repository.save(entity, regenerateInstallments = shouldGenerate)
            isSaved = true
            onSaved(entity.id)
        }
    }

    /** تغییر تنظیم «ساخت خودکار اقساط» از دل فرم */
    fun setAutoGenerate(value: Boolean) { autoGenerateInstallments = value }

    /** شماره‌ی بعدی برای دکمه‌ی «پیش‌فایل جدید» در همان نشست */
    suspend fun refreshDraftNumber(): String {
        draftsThisSession++
        return repository.nextDraftNumber()
    }
}

class PreFileDetailViewModel(
    private val repository: PreFileRepository,
    private val unitRepository: UnitRepository,
    private val customerRepository: CustomerRepository,
    private val followUpRepository: FollowUpRepository,
) : ViewModel() {

    private val preFileId = MutableStateFlow<String?>(null)

    val row: StateFlow<PreFileRow?> = preFileId
        .flatMapLatest { id -> if (id == null) flowOf(null) else repository.observeRowById(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val installments: StateFlow<List<InstallmentEntity>> = preFileId
        .flatMapLatest { id -> if (id == null) flowOf(emptyList()) else repository.observeInstallments(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val unit: StateFlow<UnitEntity?> = preFileId
        .flatMapLatest { id ->
            if (id == null) flowOf(null)
            else repository.observeById(id).map { it?.unitId }.flatMapLatest { unitId ->
                if (unitId == null) flowOf(null) else unitRepository.observeById(unitId)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val customer: StateFlow<CustomerEntity?> = preFileId
        .flatMapLatest { id ->
            if (id == null) flowOf(null)
            else repository.observeById(id).map { it?.customerId }.flatMapLatest { customerId ->
                if (customerId == null) flowOf(null) else customerRepository.observeById(customerId)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val followUps = preFileId
        .flatMapLatest { id -> if (id == null) flowOf(emptyList()) else followUpRepository.observeByPreFile(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val isSaved = MutableStateFlow(false)

    fun setPreFileId(id: String) { preFileId.value = id }

    fun changeStatus(status: String) {
        val current = row.value?.preFile ?: return
        viewModelScope.launch {
            repository.save(
                current.copy(
                    status = status,
                    confirmedDate = if (status == Constants.PREFILE_CONFIRMED) Formatters.todayJalali() else current.confirmedDate,
                    cancelDate = if (status == Constants.PREFILE_CANCELED) Formatters.todayJalali() else current.cancelDate,
                )
            )
        }
    }

    fun payInstallment(installmentId: String, amount: Long, method: String?, reference: String?) {
        viewModelScope.launch {
            repository.markInstallmentPaid(installmentId, amount, paymentMethod = method, referenceNumber = reference)
        }
    }

    fun unpayInstallment(installmentId: String) {
        viewModelScope.launch { repository.markInstallmentUnpaid(installmentId) }
    }

    fun completeFollowUp(followUpId: String, outcome: String?) {
        viewModelScope.launch { followUpRepository.markDone(followUpId, outcome) }
    }

    fun delete(onDone: () -> Unit) {
        val id = preFileId.value ?: return
        viewModelScope.launch {
            repository.delete(id)
            onDone()
        }
    }
}
