package ir.pishfile.app.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.pishfile.app.core.Constants
import ir.pishfile.app.core.Formatters
import ir.pishfile.app.data.local.dao.PreFileRow
import ir.pishfile.app.data.local.entity.ProjectAreaEntity
import ir.pishfile.app.data.local.entity.ProjectEntity
import ir.pishfile.app.data.local.entity.UnitEntity
import ir.pishfile.app.data.repository.PreFileRepository
import ir.pishfile.app.data.repository.ProjectAreaRepository
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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** یک متراژ پروژه در فرم ویرایش (مقادیر به‌صورت متن برای فیلدها) */
data class ProjectAreaItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val label: String = "",
    val areaValue: String = "",
    val totalPrice: String = "",
    val depositAmount: String = "",
    val bonusAmount: String = "",
    val installmentCount: String = "",
    val installmentAmount: String = "",
    val installmentPeriod: String = "",
)

data class ProjectForm(
    val name: String = "",
    val code: String = "",
    val projectType: String = "مسکونی",
    val pricingModel: String = Constants.PRICING_METER,
    val province: String = "تهران",
    val city: String = "تهران",
    val district: String = "",
    val address: String = "",
    val salePricePerMeter: String = "",
    val defaultDepositAmount: String = "",
    val shareMeterArea: String = "",
    val sharePrice: String = "",

    /** قیمت حدودی کل پروژه (مخصوص پروژه‌های سهامی که واریزی/امتیاز هم دارند) */
    val approxTotalPrice: String = "",

    // --- متراژهای پروژه با شرایط مالی مخصوص هرکدام ---
    val areas: List<ProjectAreaItem> = emptyList(),

    // --- پیش‌فرض‌های ثبت فایل (برای ثبت سریع پیش‌فروش) ---
    val defaultBonusAmount: String = "",
    val hasRanking: Boolean = false,
    val defaultRanking: String = "",
    val saleConditionCash: Boolean = true,
    val saleConditionInstallment: Boolean = false,
    val saleConditionExchange: Boolean = false,
    val saleConditionNotes: String = "",
    val installmentCount: String = "",
    val remainingInstallmentsCount: String = "",
    val installmentAmount: String = "",
    val installmentPeriod: String = "",
    val nextInstallmentDueDate: String = "",

    val phase: String = "PLANNING",
    val progressPercent: String = "0",
    val deliveryDate: String = "",
    val facilities: String = "",
    val description: String = "",
) {
    fun toEntity(existing: ProjectEntity? = null): ProjectEntity {
        return ProjectEntity(
            id = existing?.id ?: java.util.UUID.randomUUID().toString(),
            name = name.trim(),
            code = code.ifBlank { null },
            projectType = projectType.ifBlank { null },
            pricingModel = pricingModel,
            province = province.ifBlank { null },
            city = city.ifBlank { null },
            district = district.ifBlank { null },
            address = address.ifBlank { null },
            salePricePerMeter = Formatters.parseLong(salePricePerMeter),
            defaultDepositAmount = Formatters.parseLong(defaultDepositAmount),
            shareMeterArea = Formatters.toLatinDigits(shareMeterArea).toDoubleOrNull(),
            sharePrice = Formatters.parseLong(sharePrice),
            approxTotalPrice = Formatters.parseLong(approxTotalPrice),
            defaultBonusAmount = Formatters.parseLong(defaultBonusAmount),
            hasRanking = hasRanking,
            defaultRanking = if (hasRanking) defaultRanking.ifBlank { "رتبه " } else null,
            saleConditionCash = saleConditionCash,
            saleConditionInstallment = saleConditionInstallment,
            saleConditionExchange = saleConditionExchange,
            saleConditionNotes = saleConditionNotes.ifBlank { null },
            installmentCount = Formatters.parseLong(installmentCount)?.toInt(),
            remainingInstallmentsCount = Formatters.parseLong(remainingInstallmentsCount)?.toInt(),
            installmentAmount = Formatters.parseLong(installmentAmount),
            installmentPeriod = installmentPeriod.ifBlank { null },
            nextInstallmentDueDate = nextInstallmentDueDate.ifBlank { null },
            phase = phase,
            progressPercent = Formatters.parseLong(progressPercent)?.toInt() ?: 0,
            deliveryDate = deliveryDate.ifBlank { null },
            facilities = facilities.ifBlank { null },
            description = description.ifBlank { null },
            createdAt = existing?.createdAt ?: System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
        )
    }

    companion object {
        fun from(e: ProjectEntity) = ProjectForm(
            name = e.name,
            code = e.code.orEmpty(),
            projectType = e.projectType ?: "مسکونی",
            pricingModel = e.pricingModel,
            province = e.province ?: "تهران",
            city = e.city ?: "تهران",
            district = e.district.orEmpty(),
            address = e.address.orEmpty(),
            salePricePerMeter = e.salePricePerMeter?.toString().orEmpty(),
            defaultDepositAmount = e.defaultDepositAmount?.toString().orEmpty(),
            shareMeterArea = e.shareMeterArea?.toString().orEmpty(),
            sharePrice = e.sharePrice?.toString().orEmpty(),
            approxTotalPrice = e.approxTotalPrice?.toString().orEmpty(),
            defaultBonusAmount = e.defaultBonusAmount?.toString().orEmpty(),
            hasRanking = e.hasRanking,
            defaultRanking = e.defaultRanking.orEmpty(),
            saleConditionCash = e.saleConditionCash,
            saleConditionInstallment = e.saleConditionInstallment,
            saleConditionExchange = e.saleConditionExchange,
            saleConditionNotes = e.saleConditionNotes.orEmpty(),
            installmentCount = e.installmentCount?.toString().orEmpty(),
            remainingInstallmentsCount = e.remainingInstallmentsCount?.toString().orEmpty(),
            installmentAmount = e.installmentAmount?.toString().orEmpty(),
            installmentPeriod = e.installmentPeriod.orEmpty(),
            nextInstallmentDueDate = e.nextInstallmentDueDate.orEmpty(),
            phase = e.phase,
            progressPercent = e.progressPercent.toString(),
            deliveryDate = e.deliveryDate.orEmpty(),
            facilities = e.facilities.orEmpty(),
            description = e.description.orEmpty(),
        )
    }
}

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class ProjectListViewModel(private val repository: ProjectRepository) : ViewModel() {

    private val queryFlow = MutableStateFlow("")

    val projects: StateFlow<List<ProjectEntity>> = queryFlow
        .debounce(180)
        .flatMapLatest { q -> if (q.isBlank()) repository.observeAll() else repository.search(q) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setQuery(query: String) { queryFlow.value = query }

    fun toggleFavorite(project: ProjectEntity) {
        viewModelScope.launch { repository.toggleFavorite(project.id, !project.isFavorite) }
    }

    fun delete(id: String) {
        viewModelScope.launch { repository.delete(id) }
    }
}

class ProjectEditViewModel(
    private val repository: ProjectRepository,
    private val projectAreaRepository: ProjectAreaRepository,
) : ViewModel() {

    var form by mutableStateOf(ProjectForm())
        private set

    private var existing: ProjectEntity? = null
    var isLoaded by mutableStateOf(false)
        private set

    fun markNew() {
        if (!isLoaded) {
            form = ProjectForm()
            isLoaded = true
        }
    }

    fun load(id: String) {
        if (isLoaded || id.isBlank()) return
        viewModelScope.launch {
            repository.getById(id)?.let {
                existing = it
                form = ProjectForm.from(it)
            }
            form = form.copy(areas = projectAreaRepository.getByProject(id).map { area ->
                ProjectAreaItem(
                    id = area.id,
                    label = area.label,
                    areaValue = area.areaValue?.toString().orEmpty(),
                    totalPrice = area.totalPrice?.toString().orEmpty(),
                    depositAmount = area.depositAmount?.toString().orEmpty(),
                    bonusAmount = area.bonusAmount?.toString().orEmpty(),
                    installmentCount = area.installmentCount?.toString().orEmpty(),
                    installmentAmount = area.installmentAmount?.toString().orEmpty(),
                    installmentPeriod = area.installmentPeriod.orEmpty(),
                )
            })
            isLoaded = true
        }
    }

    fun update(transform: (ProjectForm) -> ProjectForm) { form = transform(form) }

    fun addArea() {
        form = form.copy(areas = form.areas + ProjectAreaItem())
    }

    fun updateArea(areaId: String, transform: (ProjectAreaItem) -> ProjectAreaItem) {
        form = form.copy(areas = form.areas.map { if (it.id == areaId) transform(it) else it })
    }

    fun removeArea(areaId: String) {
        form = form.copy(areas = form.areas.filter { it.id != areaId })
    }

    fun save(onSaved: (String) -> Unit) {
        viewModelScope.launch {
            if (form.name.isBlank()) return@launch
            val entity = form.toEntity(existing)
            repository.save(entity)
            // همگام‌سازی متراژها: حذف قدیمی + درج جدید (با projectId نهایی)
            projectAreaRepository.deleteByProject(entity.id)
            form.areas.forEachIndexed { index, item ->
                val label = item.label.trim().ifBlank {
                    "متراژ " + (item.areaValue.trim().ifBlank { "??" })
                }
                projectAreaRepository.save(
                    ProjectAreaEntity(
                        id = item.id,
                        projectId = entity.id,
                        label = label,
                        areaValue = Formatters.parseDouble(item.areaValue),
                        totalPrice = Formatters.parseLong(item.totalPrice),
                        depositAmount = Formatters.parseLong(item.depositAmount),
                        bonusAmount = Formatters.parseLong(item.bonusAmount),
                        installmentCount = Formatters.parseLong(item.installmentCount)?.toInt(),
                        installmentAmount = Formatters.parseLong(item.installmentAmount),
                        installmentPeriod = item.installmentPeriod.trim().ifBlank { null },
                        sortIndex = index,
                    )
                )
            }
            onSaved(entity.id)
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class ProjectDetailViewModel(
    private val projectRepository: ProjectRepository,
    private val unitRepository: UnitRepository,
    private val preFileRepository: PreFileRepository,
    private val projectAreaRepository: ProjectAreaRepository,
) : ViewModel() {

    private val projectId = MutableStateFlow<String?>(null)

    val project: StateFlow<ProjectEntity?> = projectId
        .flatMapLatest { id -> if (id == null) flowOf(null) else projectRepository.observeById(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val units: StateFlow<List<UnitEntity>> = projectId
        .flatMapLatest { id -> if (id == null) flowOf(emptyList()) else unitRepository.observeByProject(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val preFiles: StateFlow<List<PreFileRow>> = projectId
        .flatMapLatest { id -> if (id == null) flowOf(emptyList()) else preFileRepository.observeRowsByProject(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val areas: StateFlow<List<ProjectAreaEntity>> = projectId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else projectAreaRepository.observeByProject(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setProjectId(id: String) { projectId.value = id }

    fun delete(onDone: () -> Unit) {
        val id = projectId.value ?: return
        viewModelScope.launch {
            projectRepository.delete(id)
            onDone()
        }
    }
}
