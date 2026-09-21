package ir.pishfile.app.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.pishfile.app.core.Constants
import ir.pishfile.app.core.Formatters
import ir.pishfile.app.data.local.dao.PreFileRow
import ir.pishfile.app.data.local.entity.PreFileEntity
import ir.pishfile.app.data.local.entity.ProjectEntity
import ir.pishfile.app.data.local.entity.UnitEntity
import ir.pishfile.app.data.repository.FollowUpRepository
import ir.pishfile.app.data.repository.PreFileRepository
import ir.pishfile.app.data.repository.ProjectRepository
import ir.pishfile.app.data.repository.UnitRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * فرم ثبت / ویرایش فایل پیش‌فروش:
 *  - مدل قیمت‌گذاری: متری، واریزی-امتیاز، سهامی
 *  - شرایط فروش: نقد، شرایطی، تهاتر + توضیحات شرایط فروش
 *  - رتبه‌بندی با کلید روشن/خاموش
 *  - اطلاعات اقساط: تعداد، مانده، مبلغ قسط، دوره، تاریخ سررسید قسط پیش‌رو
 *  - وضعیت: پیش‌نویس، فروش فوری، غیر فوری، منصرف از فروش
 */
data class PreFileForm(
    val draftNumber: String = "",
    val draftDate: String = Formatters.todayJalali(),
    val projectId: String = "",
    val unitId: String = "",
    val ownerName: String = "",
    val ownerPhone: String = "",

    // مدل قیمت‌گذاری
    val pricingModel: String = Constants.PRICING_METER,

    // ۱) واریزی و امتیاز
    val depositAmount: String = "",
    val bonusAmount: String = "",

    // ۲) متری
    val pricePerMeter: String = "",
    val meterArea: String = "",
    val totalPrice: String = "",

    // ۳) سهامی
    val shareMeterArea: String = "",
    val shareCount: String = "",
    val sharePrice: String = "",

    // رتبه بندی
    val hasRanking: Boolean = false,
    val ranking: String = "",

    // شرایط فروش
    val saleConditionCash: Boolean = true,
    val saleConditionInstallment: Boolean = false,
    val saleConditionExchange: Boolean = false,
    val saleConditionNotes: String = "",

    // اقساط
    val installmentCount: String = "",
    val remainingInstallmentsCount: String = "",
    val installmentAmount: String = "",
    val installmentPeriod: String = "ماهانه",
    val nextInstallmentDueDate: String = "",

    // وضعیت و تحویل
    val status: String = Constants.PREFILE_NORMAL,
    val deliveryDate: String = "",
    val notes: String = "",
) {
    val depositValue: Long get() = Formatters.parseLong(depositAmount) ?: 0L
    val bonusValue: Long get() = Formatters.parseLong(bonusAmount) ?: 0L
    val pricePerMeterValue: Long get() = Formatters.parseLong(pricePerMeter) ?: 0L
    val meterAreaValue: Double get() = Formatters.toLatinDigits(meterArea).toDoubleOrNull() ?: 0.0
    val totalDirectPrice: Long get() = Formatters.parseLong(totalPrice) ?: 0L
    val sharePriceValue: Long get() = Formatters.parseLong(sharePrice) ?: 0L
    val shareCountValue: Int get() = Formatters.parseLong(shareCount)?.toInt() ?: 1

    /** مبلغ کل محاسبه‌شده */
    val computedTotal: Long
        get() = when (pricingModel) {
            Constants.PRICING_DEPOSIT_BONUS -> depositValue + bonusValue
            Constants.PRICING_SHARE -> sharePriceValue * shareCountValue
            else -> {
                if (totalDirectPrice > 0) totalDirectPrice
                else (meterAreaValue * pricePerMeterValue).toLong()
            }
        }

    fun toEntity(existing: PreFileEntity? = null, unit: UnitEntity? = null): PreFileEntity {
        return PreFileEntity(
            id = existing?.id ?: java.util.UUID.randomUUID().toString(),
            draftNumber = draftNumber.ifBlank { "PF-${Formatters.todayJalali().substringBefore('/')}-0001" },
            draftDate = draftDate.ifBlank { Formatters.todayJalali() },
            projectId = projectId,
            unitId = unitId.ifBlank { null },
            ownerName = ownerName.ifBlank { null },
            ownerPhone = ownerPhone.ifBlank { null },
            pricingModel = pricingModel,
            depositAmount = Formatters.parseLong(depositAmount),
            bonusAmount = Formatters.parseLong(bonusAmount),
            pricePerMeter = Formatters.parseLong(pricePerMeter),
            meterArea = meterAreaValue.takeIf { it > 0 },
            totalPrice = computedTotal,
            shareMeterArea = Formatters.toLatinDigits(shareMeterArea).toDoubleOrNull(),
            shareCount = Formatters.parseLong(shareCount)?.toInt(),
            sharePrice = Formatters.parseLong(sharePrice),
            hasRanking = hasRanking,
            ranking = if (hasRanking) ranking.ifBlank { null } else null,
            saleConditionCash = saleConditionCash,
            saleConditionInstallment = saleConditionInstallment,
            saleConditionExchange = saleConditionExchange,
            saleConditionNotes = saleConditionNotes.ifBlank { null },
            installmentCount = Formatters.parseLong(installmentCount)?.toInt(),
            remainingInstallmentsCount = Formatters.parseLong(remainingInstallmentsCount)?.toInt(),
            installmentAmount = Formatters.parseLong(installmentAmount),
            installmentPeriod = installmentPeriod,
            nextInstallmentDueDate = nextInstallmentDueDate.ifBlank { null },
            unitBlock = unit?.block,
            unitNumber = unit?.unitNumber,
            unitFloor = unit?.floor,
            status = status,
            deliveryDate = deliveryDate.ifBlank { null },
            notes = notes.ifBlank { null },
            isFavorite = existing?.isFavorite ?: false,
            createdAt = existing?.createdAt ?: System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
        )
    }

    companion object {
        fun from(entity: PreFileEntity) = PreFileForm(
            draftNumber = entity.draftNumber,
            draftDate = entity.draftDate,
            projectId = entity.projectId,
            unitId = entity.unitId.orEmpty(),
            ownerName = entity.ownerName.orEmpty(),
            ownerPhone = entity.ownerPhone.orEmpty(),
            pricingModel = entity.pricingModel,
            depositAmount = entity.depositAmount?.toString().orEmpty(),
            bonusAmount = entity.bonusAmount?.toString().orEmpty(),
            pricePerMeter = entity.pricePerMeter?.toString().orEmpty(),
            meterArea = entity.meterArea?.toString().orEmpty(),
            totalPrice = entity.totalPrice.toString(),
            shareMeterArea = entity.shareMeterArea?.toString().orEmpty(),
            shareCount = entity.shareCount?.toString().orEmpty(),
            sharePrice = entity.sharePrice?.toString().orEmpty(),
            hasRanking = entity.hasRanking,
            ranking = entity.ranking.orEmpty(),
            saleConditionCash = entity.saleConditionCash,
            saleConditionInstallment = entity.saleConditionInstallment,
            saleConditionExchange = entity.saleConditionExchange,
            saleConditionNotes = entity.saleConditionNotes.orEmpty(),
            installmentCount = entity.installmentCount?.toString().orEmpty(),
            remainingInstallmentsCount = entity.remainingInstallmentsCount?.toString().orEmpty(),
            installmentAmount = entity.installmentAmount?.toString().orEmpty(),
            installmentPeriod = entity.installmentPeriod ?: "ماهانه",
            nextInstallmentDueDate = entity.nextInstallmentDueDate.orEmpty(),
            status = entity.status,
            deliveryDate = entity.deliveryDate.orEmpty(),
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

    val totalCount = repository.observeCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    fun setQuery(query: String) { queryFlow.value = query }
    fun setStatusFilter(status: String?) { statusFilter.value = status }

    fun delete(id: String) { viewModelScope.launch { repository.delete(id) } }
}

class PreFileEditViewModel(
    private val repository: PreFileRepository,
    private val projectRepository: ProjectRepository,
    private val unitRepository: UnitRepository,
) : ViewModel() {

    var form by mutableStateOf(PreFileForm())
        private set

    var projects by mutableStateOf<List<ProjectEntity>>(emptyList())
        private set
    var availableUnits by mutableStateOf<List<UnitEntity>>(emptyList())
        private set

    private var existing: PreFileEntity? = null
    private var selectedUnit: UnitEntity? = null

    var isLoaded by mutableStateOf(false)
        private set

    fun load(id: String) {
        if (isLoaded || id.isBlank()) return
        viewModelScope.launch {
            projects = projectRepository.getAll()
            repository.getById(id)?.let { entity ->
                existing = entity
                selectedUnit = entity.unitId?.let { unitRepository.getById(it) }
                form = PreFileForm.from(entity)
                if (form.projectId.isNotBlank()) {
                    availableUnits = unitRepository.getByProject(form.projectId)
                }
            }
            isLoaded = true
        }
    }

    fun startNew(projectId: String?, unitId: String?) {
        if (isLoaded) return
        viewModelScope.launch {
            projects = projectRepository.getAll()
            val defaultProject = projectId?.takeIf { it.isNotBlank() }
                ?: projects.firstOrNull()?.id.orEmpty()

            availableUnits = unitRepository.getByProject(defaultProject)
            selectedUnit = unitId?.takeIf { it.isNotBlank() }?.let { unitRepository.getById(it) }
                ?: availableUnits.firstOrNull { it.status == Constants.UNIT_AVAILABLE }

            form = PreFileForm(
                draftNumber = repository.nextDraftNumber(),
                projectId = defaultProject,
                unitId = selectedUnit?.id.orEmpty(),
                status = Constants.PREFILE_NORMAL,
            )

            // اگر پروژه انتخاب شده باشد، ویژگی‌های پروژه را روی فرم بنشان
            val prj = projects.firstOrNull { it.id == defaultProject }
            if (prj != null) {
                applyProjectToForm(prj)
            }
            if (selectedUnit != null) {
                applyUnitToForm(selectedUnit!!)
            }

            isLoaded = true
        }
    }

    /** با انتخاب پروژه، مدل قیمت‌گذاری و شرایط مخصوص پروژه خودکار پر می‌شود */
    fun selectProject(projectId: String) {
        viewModelScope.launch {
            availableUnits = unitRepository.getByProject(projectId)
            val project = projects.firstOrNull { it.id == projectId }
            form = form.copy(
                projectId = projectId,
                unitId = "",
            )
            selectedUnit = null
            if (project != null) {
                applyProjectToForm(project)
            }
        }
    }

    private fun applyProjectToForm(project: ProjectEntity) {
        val model = project.pricingModel.ifBlank { Constants.PRICING_METER }
        form = form.copy(
            pricingModel = model,
            pricePerMeter = project.salePricePerMeter?.toString() ?: form.pricePerMeter,
            depositAmount = project.defaultDepositAmount?.toString() ?: form.depositAmount,
            shareMeterArea = project.shareMeterArea?.toString() ?: form.shareMeterArea,
            sharePrice = project.sharePrice?.toString() ?: form.sharePrice,
            deliveryDate = form.deliveryDate.ifBlank { project.deliveryDate.orEmpty() },
        )
    }

    fun selectUnit(unitId: String) {
        viewModelScope.launch {
            val unit = availableUnits.firstOrNull { it.id == unitId }
            selectedUnit = unit
            if (unit != null) applyUnitToForm(unit) else form = form.copy(unitId = "")
        }
    }

    private fun applyUnitToForm(unit: UnitEntity) {
        form = form.copy(
            unitId = unit.id,
            meterArea = unit.grossArea?.toString() ?: form.meterArea,
            pricePerMeter = unit.pricePerMeter?.toString() ?: form.pricePerMeter,
            totalPrice = (unit.finalPrice ?: unit.totalPrice)?.toString() ?: form.totalPrice,
            installmentCount = unit.suggestedInstallmentCount?.toString() ?: form.installmentCount,
            installmentAmount = unit.suggestedInstallment?.toString() ?: form.installmentAmount,
            deliveryDate = form.deliveryDate.ifBlank { unit.deliveryDate.orEmpty() },
        )
    }

    fun update(transform: (PreFileForm) -> PreFileForm) { form = transform(form) }

    fun save(onSaved: (String) -> Unit) {
        viewModelScope.launch {
            if (form.projectId.isBlank()) return@launch
            val entity = form.toEntity(existing, selectedUnit)
            repository.save(entity)
            onSaved(entity.id)
        }
    }
}

class PreFileDetailViewModel(
    private val repository: PreFileRepository,
    private val unitRepository: UnitRepository,
    private val followUpRepository: FollowUpRepository,
) : ViewModel() {

    private val preFileId = MutableStateFlow<String?>(null)

    val row: StateFlow<PreFileRow?> = preFileId
        .flatMapLatest { id -> if (id == null) flowOf(null) else repository.observeRowById(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val unit: StateFlow<UnitEntity?> = preFileId
        .flatMapLatest { id ->
            if (id == null) flowOf(null)
            else repository.observeById(id).map { it?.unitId }.flatMapLatest { unitId ->
                if (unitId == null) flowOf(null) else unitRepository.observeById(unitId)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun setPreFileId(id: String) { preFileId.value = id }

    fun changeStatus(status: String) {
        val current = row.value?.preFile ?: return
        viewModelScope.launch {
            repository.save(current.copy(status = status))
        }
    }

    fun delete(onDone: () -> Unit) {
        val id = preFileId.value ?: return
        viewModelScope.launch {
            repository.delete(id)
            onDone()
        }
    }
}
