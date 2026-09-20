package ir.pishfile.app.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.pishfile.app.core.Constants
import ir.pishfile.app.core.Formatters
import ir.pishfile.app.data.local.dao.PreFileRow
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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class UnitForm(
    val projectId: String = "",
    val block: String = "",
    val unitNumber: String = "",
    val floor: String = "",
    val unitType: String = Constants.UNIT_TYPE_APARTMENT,
    val grossArea: String = "",
    val netArea: String = "",
    val bedrooms: String = "",
    val direction: String = "",
    val pricePerMeter: String = "",
    val totalPrice: String = "",
    val status: String = Constants.UNIT_AVAILABLE,
    val deliveryDate: String = "",
    val description: String = "",
) {
    fun computedTotalPrice(): Long? {
        val area = Formatters.toLatinDigits(grossArea).toDoubleOrNull() ?: return null
        val perMeter = Formatters.parseLong(pricePerMeter) ?: return null
        return (area * perMeter).toLong()
    }

    fun toEntity(existing: UnitEntity? = null): UnitEntity {
        val total = Formatters.parseLong(totalPrice) ?: computedTotalPrice()
        return UnitEntity(
            id = existing?.id ?: java.util.UUID.randomUUID().toString(),
            projectId = projectId,
            block = block.ifBlank { null },
            unitNumber = unitNumber.trim(),
            floor = Formatters.parseLong(floor)?.toInt(),
            unitType = unitType,
            grossArea = Formatters.toLatinDigits(grossArea).toDoubleOrNull(),
            netArea = Formatters.toLatinDigits(netArea).toDoubleOrNull(),
            bedrooms = Formatters.parseLong(bedrooms)?.toInt(),
            direction = direction.ifBlank { null },
            pricePerMeter = Formatters.parseLong(pricePerMeter),
            totalPrice = total,
            status = status,
            deliveryDate = deliveryDate.ifBlank { null },
            description = description.ifBlank { null },
            createdAt = existing?.createdAt ?: System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
        )
    }

    companion object {
        fun typeOptions() = listOf(
            Constants.UNIT_TYPE_APARTMENT,
            Constants.UNIT_TYPE_SHOP,
            Constants.UNIT_TYPE_OFFICE,
            Constants.UNIT_TYPE_VILLA,
            Constants.UNIT_TYPE_PARKING,
            Constants.UNIT_TYPE_STORAGE,
        )

        fun from(e: UnitEntity) = UnitForm(
            projectId = e.projectId,
            block = e.block.orEmpty(),
            unitNumber = e.unitNumber,
            floor = e.floor?.toString().orEmpty(),
            unitType = e.unitType,
            grossArea = e.grossArea?.toString().orEmpty(),
            netArea = e.netArea?.toString().orEmpty(),
            bedrooms = e.bedrooms?.toString().orEmpty(),
            direction = e.direction.orEmpty(),
            pricePerMeter = e.pricePerMeter?.toString().orEmpty(),
            totalPrice = e.totalPrice?.toString().orEmpty(),
            status = e.status,
            deliveryDate = e.deliveryDate.orEmpty(),
            description = e.description.orEmpty(),
        )
    }
}

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class UnitListViewModel(
    private val repository: UnitRepository,
    private val projectRepository: ProjectRepository,
) : ViewModel() {

    private val queryFlow = MutableStateFlow("")
    private val statusFilter = MutableStateFlow<String?>(null)
    private val projectFilter = MutableStateFlow<String?>(null)

    val projects = projectRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val units: StateFlow<List<UnitEntity>> = combine(
        queryFlow.debounce(180),
        statusFilter,
        projectFilter,
    ) { q, status, prj -> Triple(q, status, prj) }
        .flatMapLatest { (q, status, prj) ->
            val base = when {
                prj != null && prj.isNotBlank() -> repository.observeByProject(prj)
                q.isNotBlank() -> repository.search(q)
                else -> repository.observeAll()
            }
            if (status == null) base else kotlinx.coroutines.flow.map(base) { list -> list.filter { it.status == status } }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setQuery(q: String) { queryFlow.value = q }
    fun setStatusFilter(s: String?) { statusFilter.value = s }
    fun setProjectFilter(p: String?) { projectFilter.value = p }

    fun delete(id: String) { viewModelScope.launch { repository.delete(id) } }
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

    var batchBlock by mutableStateOf("A")
    var batchFromFloor by mutableStateOf("1")
    var batchToFloor by mutableStateOf("5")
    var batchUnitsPerFloor by mutableStateOf("2")
    var batchBaseArea by mutableStateOf("90")
    var batchAreaStep by mutableStateOf("5")

    fun startNew(projectId: String?) {
        if (isLoaded) return
        viewModelScope.launch {
            projects = projectRepository.getAll()
            val defProject = projectId?.takeIf { it.isNotBlank() }
                ?: projects.firstOrNull()?.id.orEmpty()
            form = UnitForm(projectId = defProject)
            isLoaded = true
        }
    }

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

    fun update(transform: (UnitForm) -> UnitForm) { form = transform(form) }

    fun save(onSaved: (String) -> Unit) {
        viewModelScope.launch {
            if (form.projectId.isBlank() || form.unitNumber.isBlank()) return@launch
            val entity = form.toEntity(existing)
            repository.save(entity)
            onSaved(entity.id)
        }
    }

    fun createBatch(onDone: (Int) -> Unit) {
        viewModelScope.launch {
            if (form.projectId.isBlank()) return@launch
            val fromF = batchFromFloor.toIntOrNull() ?: 1
            val toF = batchToFloor.toIntOrNull() ?: 1
            val perFloor = batchUnitsPerFloor.toIntOrNull() ?: 1
            val baseArea = batchBaseArea.toDoubleOrNull() ?: 90.0
            val step = batchAreaStep.toDoubleOrNull() ?: 0.0

            val prj = projects.firstOrNull { it.id == form.projectId }
            val pricePerM = prj?.salePricePerMeter

            var count = 0
            for (f in fromF..toF) {
                for (u in 1..perFloor) {
                    val area = baseArea + (f - fromF) * step
                    val unit = UnitEntity(
                        projectId = form.projectId,
                        block = batchBlock.ifBlank { null },
                        unitNumber = "${f}0$u",
                        floor = f,
                        grossArea = area,
                        pricePerMeter = pricePerM,
                        totalPrice = pricePerM?.let { (it * area).toLong() },
                        status = Constants.UNIT_AVAILABLE,
                    )
                    repository.save(unit)
                    count++
                }
            }
            onDone(count)
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class UnitDetailViewModel(
    private val unitRepository: UnitRepository,
    private val projectRepository: ProjectRepository,
    private val preFileRepository: PreFileRepository,
) : ViewModel() {

    private val unitId = MutableStateFlow<String?>(null)

    val unit: StateFlow<UnitEntity?> = unitId
        .flatMapLatest { id -> if (id == null) flowOf(null) else unitRepository.observeById(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val project: StateFlow<ProjectEntity?> = unit
        .flatMapLatest { u -> if (u == null) flowOf(null) else projectRepository.observeById(u.projectId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val preFiles: StateFlow<List<PreFileRow>> = unitId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList())
            else preFileRepository.observeAllRows().flatMapLatest { rows ->
                flowOf(rows.filter { it.preFile.unitId == id })
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setUnitId(id: String) { unitId.value = id }

    fun delete(onDone: () -> Unit) {
        val id = unitId.value ?: return
        viewModelScope.launch {
            unitRepository.delete(id)
            onDone()
        }
    }
}
