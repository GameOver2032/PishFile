package ir.pishfile.app.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.pishfile.app.core.Formatters
import ir.pishfile.app.data.local.dao.StatusCount
import ir.pishfile.app.data.local.entity.ProjectEntity
import ir.pishfile.app.data.repository.PreFileRepository
import ir.pishfile.app.data.repository.ProjectRepository
import ir.pishfile.app.data.repository.UnitRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * فرم پروژه — همه‌ی فیلدها به‌صورت متن نگه‌داری می‌شوند تا تبدیل‌ها در یک نقطه انجام شود.
 * مبالغ با فرمت هزارگان (۱۲۳٬۰۰۰) در ورودی نمایش داده می‌شوند.
 */
data class ProjectForm(
    val name: String = "",
    val code: String = "",
    val projectType: String = "مسکونی",
    val province: String = "",
    val city: String = "",
    val district: String = "",
    val address: String = "",
    val postalCode: String = "",
    val landArea: String = "",
    val totalBuiltArea: String = "",
    val blockCount: String = "",
    val floorCount: String = "",
    val unitCount: String = "",
    val unitsPerFloor: String = "",
    val parkingCount: String = "",
    val elevatorCount: String = "",
    val structureType: String = "بتنی",
    val heatingSystem: String = "",
    val permitNumber: String = "",
    val permitIssueDate: String = "",
    val permitExpiryDate: String = "",
    val landDeedNumber: String = "",
    val costPerMeter: String = "",
    val salePricePerMeter: String = "",
    val totalBudget: String = "",
    val phase: String = "PLANNING",
    val progressPercent: String = "0",
    val startDate: String = "",
    val deliveryDate: String = "",
    val contractorName: String = "",
    val contractorPhone: String = "",
    val supervisorName: String = "",
    val supervisorPhone: String = "",
    val developerName: String = "",
    val salesManagerPhone: String = "",
    val bankName: String = "",
    val iban: String = "",
    val facilities: String = "",
    val description: String = "",
    val tags: String = "",
) {
    fun toEntity(existing: ProjectEntity? = null): ProjectEntity = ProjectEntity(
        id = existing?.id ?: java.util.UUID.randomUUID().toString(),
        name = name.trim().ifBlank { "پروژه بدون نام" },
        code = code.ifBlank { null },
        projectType = projectType.ifBlank { null },
        province = province.ifBlank { null },
        city = city.ifBlank { null },
        district = district.ifBlank { null },
        address = address.ifBlank { null },
        postalCode = postalCode.ifBlank { null },
        landArea = Formatters.parseDouble(landArea),
        totalBuiltArea = Formatters.parseDouble(totalBuiltArea),
        blockCount = Formatters.parseLong(blockCount)?.toInt(),
        floorCount = Formatters.parseLong(floorCount)?.toInt(),
        unitCount = Formatters.parseLong(unitCount)?.toInt(),
        unitsPerFloor = Formatters.parseLong(unitsPerFloor)?.toInt(),
        parkingCount = Formatters.parseLong(parkingCount)?.toInt(),
        elevatorCount = Formatters.parseLong(elevatorCount)?.toInt(),
        structureType = structureType.ifBlank { null },
        heatingSystem = heatingSystem.ifBlank { null },
        permitNumber = permitNumber.ifBlank { null },
        permitIssueDate = permitIssueDate.ifBlank { null },
        permitExpiryDate = permitExpiryDate.ifBlank { null },
        landDeedNumber = landDeedNumber.ifBlank { null },
        costPerMeter = Formatters.parseLong(costPerMeter),
        salePricePerMeter = Formatters.parseLong(salePricePerMeter),
        totalBudget = Formatters.parseLong(totalBudget),
        phase = phase,
        progressPercent = (Formatters.parseLong(progressPercent) ?: 0L).toInt().coerceIn(0, 100),
        startDate = startDate.ifBlank { null },
        deliveryDate = deliveryDate.ifBlank { null },
        contractorName = contractorName.ifBlank { null },
        contractorPhone = contractorPhone.ifBlank { null },
        supervisorName = supervisorName.ifBlank { null },
        supervisorPhone = supervisorPhone.ifBlank { null },
        developerName = developerName.ifBlank { null },
        salesManagerPhone = salesManagerPhone.ifBlank { null },
        bankName = bankName.ifBlank { null },
        iban = iban.ifBlank { null },
        facilities = facilities.ifBlank { null },
        description = description.ifBlank { null },
        tags = tags.ifBlank { null },
        isFavorite = existing?.isFavorite ?: false,
        isArchived = existing?.isArchived ?: false,
        createdAt = existing?.createdAt ?: System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis(),
    )

    companion object {
        fun from(entity: ProjectEntity) = ProjectForm(
            name = entity.name,
            code = entity.code.orEmpty(),
            projectType = entity.projectType ?: "مسکونی",
            province = entity.province.orEmpty(),
            city = entity.city.orEmpty(),
            district = entity.district.orEmpty(),
            address = entity.address.orEmpty(),
            postalCode = entity.postalCode.orEmpty(),
            landArea = entity.landArea?.toString().orEmpty(),
            totalBuiltArea = entity.totalBuiltArea?.toString().orEmpty(),
            blockCount = entity.blockCount?.toString().orEmpty(),
            floorCount = entity.floorCount?.toString().orEmpty(),
            unitCount = entity.unitCount?.toString().orEmpty(),
            unitsPerFloor = entity.unitsPerFloor?.toString().orEmpty(),
            parkingCount = entity.parkingCount?.toString().orEmpty(),
            elevatorCount = entity.elevatorCount?.toString().orEmpty(),
            structureType = entity.structureType ?: "بتنی",
            heatingSystem = entity.heatingSystem.orEmpty(),
            permitNumber = entity.permitNumber.orEmpty(),
            permitIssueDate = entity.permitIssueDate.orEmpty(),
            permitExpiryDate = entity.permitExpiryDate.orEmpty(),
            landDeedNumber = entity.landDeedNumber.orEmpty(),
            costPerMeter = entity.costPerMeter?.toString().orEmpty(),
            salePricePerMeter = entity.salePricePerMeter?.toString().orEmpty(),
            totalBudget = entity.totalBudget?.toString().orEmpty(),
            phase = entity.phase,
            progressPercent = entity.progressPercent.toString(),
            startDate = entity.startDate.orEmpty(),
            deliveryDate = entity.deliveryDate.orEmpty(),
            contractorName = entity.contractorName.orEmpty(),
            contractorPhone = entity.contractorPhone.orEmpty(),
            supervisorName = entity.supervisorName.orEmpty(),
            supervisorPhone = entity.supervisorPhone.orEmpty(),
            developerName = entity.developerName.orEmpty(),
            salesManagerPhone = entity.salesManagerPhone.orEmpty(),
            bankName = entity.bankName.orEmpty(),
            iban = entity.iban.orEmpty(),
            facilities = entity.facilities.orEmpty(),
            description = entity.description.orEmpty(),
            tags = entity.tags.orEmpty(),
        )
    }
}

@OptIn(FlowPreview::class, kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class ProjectListViewModel(private val repository: ProjectRepository) : ViewModel() {

    private val queryFlow = MutableStateFlow("")

    val projects: StateFlow<List<ProjectEntity>> = queryFlow
        .debounce(180)
        .flatMapLatest { query ->
            if (query.isBlank()) repository.observeAll() else repository.search(query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val totalCount: StateFlow<Int> = repository.observeCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    fun setQuery(query: String) { queryFlow.value = query }

    fun toggleFavorite(project: ProjectEntity) {
        viewModelScope.launch { repository.save(project.copy(isFavorite = !project.isFavorite)) }
    }

    fun delete(id: String) {
        viewModelScope.launch { repository.delete(id) }
    }
}

class ProjectDetailViewModel(
    private val projectRepository: ProjectRepository,
    private val unitRepository: UnitRepository,
    preFileRepository: PreFileRepository,
) : ViewModel() {

    private val projectId = MutableStateFlow<String?>(null)

    val project: StateFlow<ProjectEntity?> = projectId
        .flatMapLatest { id -> if (id == null) kotlinx.coroutines.flow.flowOf(null) else projectRepository.observeById(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val units: StateFlow<List<ir.pishfile.app.data.local.entity.UnitEntity>> = projectId
        .flatMapLatest { id ->
            if (id == null) kotlinx.coroutines.flow.flowOf(emptyList())
            else unitRepository.observeByProject(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val unitStatusCounts: StateFlow<List<StatusCount>> = projectId
        .flatMapLatest { id -> unitRepository.observeStatusCounts(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val preFiles: StateFlow<List<ir.pishfile.app.data.local.dao.PreFileRow>> = projectId
        .flatMapLatest { id ->
            if (id == null) kotlinx.coroutines.flow.flowOf(emptyList())
            else preFileRepository.observeRowsByProject(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val totalSales: StateFlow<Long?> = projectId
        .flatMapLatest { id ->
            if (id == null) kotlinx.coroutines.flow.flowOf(null) else preFileRepository.observeProjectSales(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val averagePricePerMeter: StateFlow<Double?> = projectId
        .flatMapLatest { id ->
            if (id == null) kotlinx.coroutines.flow.flowOf(null) else unitRepository.observeAveragePrice(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val totalArea: StateFlow<Double?> = projectId
        .flatMapLatest { id ->
            if (id == null) kotlinx.coroutines.flow.flowOf(null) else unitRepository.observeTotalArea(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun setProjectId(id: String) { projectId.value = id }

    fun toggleFavorite() {
        val current = project.value ?: return
        viewModelScope.launch { projectRepository.save(current.copy(isFavorite = !current.isFavorite)) }
    }

    fun delete(onDone: () -> Unit) {
        val id = projectId.value ?: return
        viewModelScope.launch {
            projectRepository.delete(id)
            onDone()
        }
    }
}

class ProjectEditViewModel(private val repository: ProjectRepository) : ViewModel() {

    var form by mutableStateOf(ProjectForm())
        private set

    private var existing: ProjectEntity? = null
    var isLoaded by mutableStateOf(false)
        private set

    fun load(id: String) {
        if (isLoaded || id.isBlank()) return
        viewModelScope.launch {
            repository.getById(id)?.let {
                existing = it
                form = ProjectForm.from(it)
            }
            isLoaded = true
        }
    }

    fun markNew() { isLoaded = true }

    fun update(transform: (ProjectForm) -> ProjectForm) { form = transform(form) }

    fun save(onSaved: (String) -> Unit) {
        viewModelScope.launch {
            val entity = form.toEntity(existing)
            repository.save(entity)
            onSaved(entity.id)
        }
    }
}
