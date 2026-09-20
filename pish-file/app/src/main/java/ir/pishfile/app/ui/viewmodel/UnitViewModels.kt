package ir.pishfile.app.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.pishfile.app.core.Constants
import ir.pishfile.app.core.Formatters
import ir.pishfile.app.data.local.entity.ProjectEntity
import ir.pishfile.app.data.local.entity.UnitEntity
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

data class UnitForm(
    val projectId: String = "",
    val block: String = "",
    val unitNumber: String = "",
    val floor: String = "",
    val unitType: String = "آپارتمان",
    val bedrooms: String = "",
    val bathrooms: String = "",
    val kitchens: String = "",
    val grossArea: String = "",
    val netArea: String = "",
    val balconyArea: String = "",
    val commonAreaShare: String = "",
    val ceilingHeight: String = "",
    val direction: String = "",
    val lighting: String = "",
    val view: String = "",
    val positionType: String = "",
    val facilities: Set<String> = emptySet(),
    val parkingCount: String = "0",
    val parkingNumber: String = "",
    val storageCount: String = "0",
    val storageNumber: String = "",
    val pricePerMeter: String = "",
    val totalPrice: String = "",
    val finalPrice: String = "",
    val extraCosts: String = "",
    val discount: String = "",
    val vatAmount: String = "",
    val prepaymentSuggestion: String = "",
    val suggestedInstallment: String = "",
    val suggestedInstallmentCount: String = "",
    val costPrice: String = "",
    val status: String = Constants.UNIT_AVAILABLE,
    val deliveryDate: String = "",
    val deliveryStatus: String = "",
    val technicalNotes: String = "",
    val description: String = "",
) {
    /** قیمت کل خودکار = متراژ × قیمت هر متر */
    fun computedTotalPrice(): Long? {
        val area = Formatters.parseDouble(grossArea)
        val perMeter = Formatters.parseLong(pricePerMeter)
        return if (area != null && perMeter != null) (area * perMeter).toLong() else null
    }

    fun toEntity(existing: UnitEntity? = null): UnitEntity {
        val perMeter = Formatters.parseLong(pricePerMeter)
        val area = Formatters.parseDouble(grossArea)
        val autoTotal = if (perMeter != null && area != null) (area * perMeter).toLong() else null
        return UnitEntity(
            id = existing?.id ?: java.util.UUID.randomUUID().toString(),
            projectId = projectId,
            block = block.ifBlank { null },
            unitNumber = unitNumber.trim().ifBlank { "—" },
            floor = Formatters.parseLong(floor)?.toInt(),
            unitType = unitTypeToCode(unitType),
            bedrooms = Formatters.parseLong(bedrooms)?.toInt(),
            bathrooms = Formatters.parseLong(bathrooms)?.toInt(),
            kitchens = Formatters.parseLong(kitchens)?.toInt(),
            grossArea = area,
            netArea = Formatters.parseDouble(netArea),
            balconyArea = Formatters.parseDouble(balconyArea),
            commonAreaShare = Formatters.parseDouble(commonAreaShare),
            ceilingHeight = Formatters.parseLong(ceilingHeight)?.toInt(),
            direction = direction.ifBlank { null },
            lighting = lighting.ifBlank { null },
            view = view.ifBlank { null },
            positionType = positionType.ifBlank { null },
            facilities = facilities.takeIf { it.isNotEmpty() }?.joinToString(","),
            parkingCount = (Formatters.parseLong(parkingCount) ?: 0L).toInt(),
            parkingNumber = parkingNumber.ifBlank { null },
            storageCount = (Formatters.parseLong(storageCount) ?: 0L).toInt(),
            storageNumber = storageNumber.ifBlank { null },
            pricePerMeter = perMeter,
            totalPrice = Formatters.parseLong(totalPrice) ?: autoTotal,
            finalPrice = Formatters.parseLong(finalPrice),
            extraCosts = Formatters.parseLong(extraCosts),
            discount = Formatters.parseLong(discount),
            vatAmount = Formatters.parseLong(vatAmount),
            prepaymentSuggestion = Formatters.parseLong(prepaymentSuggestion),
            suggestedInstallment = Formatters.parseLong(suggestedInstallment),
            suggestedInstallmentCount = Formatters.parseLong(suggestedInstallmentCount)?.toInt(),
            costPrice = Formatters.parseLong(costPrice),
            status = status,
            deliveryDate = deliveryDate.ifBlank { null },
            deliveryStatus = deliveryStatus.ifBlank { null },
            technicalNotes = technicalNotes.ifBlank { null },
            description = description.ifBlank { null },
            createdAt = existing?.createdAt ?: System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
        )
    }

    companion object {
        fun unitTypeToCode(label: String): String = when (label) {
            "آپارتمان" -> Constants.UNIT_TYPE_APARTMENT
            "مغازه" -> Constants.UNIT_TYPE_SHOP
            "اداری" -> Constants.UNIT_TYPE_OFFICE
            "پارکینگ" -> Constants.UNIT_TYPE_PARKING
            "انباری" -> Constants.UNIT_TYPE_STORAGE
            "ویلا" -> Constants.UNIT_TYPE_VILLA
            "زمین" -> Constants.UNIT_TYPE_LAND
            else -> Constants.UNIT_TYPE_APARTMENT
        }

        fun typeOptions(): List<String> = Constants.unitTypes.map { Constants.unitTypeLabel(it) }

        fun from(entity: UnitEntity) = UnitForm(
            projectId = entity.projectId,
            block = entity.block.orEmpty(),
            unitNumber = entity.unitNumber,
            floor = entity.floor?.toString().orEmpty(),
            unitType = Constants.unitTypeLabel(entity.unitType),
            bedrooms = entity.bedrooms?.toString().orEmpty(),
            bathrooms = entity.bathrooms?.toString().orEmpty(),
            kitchens = entity.kitchens?.toString().orEmpty(),
            grossArea = entity.grossArea?.let { trimNumber(it) }.orEmpty(),
            netArea = entity.netArea?.let { trimNumber(it) }.orEmpty(),
            balconyArea = entity.balconyArea?.let { trimNumber(it) }.orEmpty(),
            commonAreaShare = entity.commonAreaShare?.let { trimNumber(it) }.orEmpty(),
            ceilingHeight = entity.ceilingHeight?.toString().orEmpty(),
            direction = entity.direction.orEmpty(),
            lighting = entity.lighting.orEmpty(),
            view = entity.view.orEmpty(),
            positionType = entity.positionType.orEmpty(),
            facilities = entity.facilities?.split(",")?.filter { it.isNotBlank() }?.toSet() ?: emptySet(),
            parkingCount = entity.parkingCount.toString(),
            parkingNumber = entity.parkingNumber.orEmpty(),
            storageCount = entity.storageCount.toString(),
            storageNumber = entity.storageNumber.orEmpty(),
            pricePerMeter = entity.pricePerMeter?.toString().orEmpty(),
            totalPrice = entity.totalPrice?.toString().orEmpty(),
            finalPrice = entity.finalPrice?.toString().orEmpty(),
            extraCosts = entity.extraCosts?.toString().orEmpty(),
            discount = entity.discount?.toString().orEmpty(),
            vatAmount = entity.vatAmount?.toString().orEmpty(),
            prepaymentSuggestion = entity.prepaymentSuggestion?.toString().orEmpty(),
            suggestedInstallment = entity.suggestedInstallment?.toString().orEmpty(),
            suggestedInstallmentCount = entity.suggestedInstallmentCount?.toString().orEmpty(),
            costPrice = entity.costPrice?.toString().orEmpty(),
            status = entity.status,
            deliveryDate = entity.deliveryDate.orEmpty(),
            deliveryStatus = entity.deliveryStatus.orEmpty(),
            technicalNotes = entity.technicalNotes.orEmpty(),
            description = entity.description.orEmpty(),
        )

        private fun trimNumber(value: Double): String =
            if (value % 1.0 == 0.0) value.toInt().toString() else value.toString()
    }
}

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class UnitListViewModel(
    private val repository: UnitRepository,
    projectRepository: ProjectRepository,
) : ViewModel() {

    private val queryFlow = MutableStateFlow("")
    private val statusFilter = MutableStateFlow<String?>(null)
    private val projectFilter = MutableStateFlow<String?>(null)

    val projects: StateFlow<List<ProjectEntity>> = projectRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val units: StateFlow<List<UnitEntity>> = combine(
        queryFlow.debounce(180),
        statusFilter,
        projectFilter,
    ) { query, status, projectId -> Triple(query, status, projectId) }
        .flatMapLatest { (query, status, projectId) ->
            val base = when {
                projectId != null -> repository.observeByProject(projectId)
                query.isNotBlank() -> repository.search(query)
                else -> repository.observeAll()
            }
            if (status == null) base else base.map { list -> list.filter { it.status == status } }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val statusCounts = repository.observeStatusCounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val totalCount = repository.observeCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    fun setQuery(query: String) { queryFlow.value = query }
    fun setStatusFilter(status: String?) { statusFilter.value = status }
    fun setProjectFilter(projectId: String?) { projectFilter.value = projectId }

    fun delete(id: String) { viewModelScope.launch { repository.delete(id) } }

    fun changeStatus(unit: UnitEntity, status: String) {
        viewModelScope.launch { repository.updateStatus(unit.id, status) }
    }
}

class UnitEditViewModel(
    private val repository: UnitRepository,
    private val projectRepository: ProjectRepository,
) : ViewModel() {

    var form by mutableStateOf(UnitForm())
        private set

    var projects by mutableStateOf<List<ProjectEntity>>(emptyList())
        private set

    private var existing: UnitEntity? = null
    var isLoaded by mutableStateOf(false)
        private set

    // --- ساخت گروهی واحدها ---
    var batchBlock by mutableStateOf("A")
    var batchFromFloor by mutableStateOf("1")
    var batchToFloor by mutableStateOf("5")
    var batchUnitsPerFloor by mutableStateOf("2")
    var batchBaseArea by mutableStateOf("95")
    var batchAreaStep by mutableStateOf("5")

    fun load(id: String) {
        if (isLoaded || id.isBlank()) return
        viewModelScope.launch {
            projects = projectRepository.getAll()
            repository.getById(id)?.let {
                existing = it
                form = UnitForm.from(it)
            }
            isLoaded = true
        }
    }

    fun startNew(projectId: String?) {
        if (isLoaded) return
        viewModelScope.launch {
            val list = projectRepository.getAll()
            projects = list
            val defaultProject = projectId?.takeIf { it.isNotBlank() } ?: list.firstOrNull()?.id.orEmpty()
            val project = list.firstOrNull { it.id == defaultProject }
            form = UnitForm(
                projectId = defaultProject,
                pricePerMeter = project?.salePricePerMeter?.toString().orEmpty(),
                deliveryDate = project?.deliveryDate.orEmpty(),
            )
            isLoaded = true
        }
    }

    fun update(transform: (UnitForm) -> UnitForm) { form = transform(form) }

    fun save(onSaved: (String) -> Unit) {
        viewModelScope.launch {
            if (form.projectId.isBlank()) return@launch
            val entity = form.toEntity(existing)
            repository.save(entity)
            onSaved(entity.id)
        }
    }

    fun createBatch(onDone: (Int) -> Unit) {
        viewModelScope.launch {
            if (form.projectId.isBlank()) return@launch
            val from = Formatters.parseLong(batchFromFloor)?.toInt() ?: 1
            val to = Formatters.parseLong(batchToFloor)?.toInt() ?: from
            val perFloor = Formatters.parseLong(batchUnitsPerFloor)?.toInt() ?: 1
            val area = Formatters.parseDouble(batchBaseArea) ?: 90.0
            val step = Formatters.parseDouble(batchAreaStep) ?: 0.0
            val count = repository.createBatch(
                projectId = form.projectId,
                block = batchBlock.ifBlank { null },
                fromFloor = minOf(from, to),
                toFloor = maxOf(from, to),
                unitsPerFloor = perFloor.coerceIn(1, 20),
                baseArea = area,
                areaStepPerFloor = step,
                pricePerMeter = Formatters.parseLong(form.pricePerMeter),
                unitType = UnitForm.unitTypeToCode(form.unitType),
                bedrooms = Formatters.parseLong(form.bedrooms)?.toInt(),
                facilities = form.facilities.takeIf { it.isNotEmpty() }?.joinToString(","),
                deliveryDate = form.deliveryDate.ifBlank { null },
            )
            onDone(count)
        }
    }
}

class UnitDetailViewModel(
    private val unitRepository: UnitRepository,
    private val projectRepository: ProjectRepository,
    private val preFileRepository: PreFileRepository,
) : ViewModel() {

    private val unitId = MutableStateFlow<String?>(null)

    val unit: StateFlow<UnitEntity?> = unitId
        .flatMapLatest { id -> if (id == null) flowOf(null) else unitRepository.observeById(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val project: StateFlow<ProjectEntity?> = unitId
        .flatMapLatest { id ->
            if (id == null) flowOf(null)
            else unitRepository.observeById(id)
                .flatMapLatest { unit ->
                    val projectId = unit?.projectId
                    if (projectId == null) flowOf(null) else projectRepository.observeById(projectId)
                }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val preFiles: StateFlow<List<ir.pishfile.app.data.local.dao.PreFileRow>> = unitId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList())
            else preFileRepository.observeAllRows().map { rows -> rows.filter { it.preFile.unitId == id } }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setUnitId(id: String) { unitId.value = id }

    fun changeStatus(status: String) {
        val id = unitId.value ?: return
        viewModelScope.launch { unitRepository.updateStatus(id, status) }
    }

    fun delete(onDone: () -> Unit) {
        val id = unitId.value ?: return
        viewModelScope.launch {
            unitRepository.delete(id)
            onDone()
        }
    }
}
