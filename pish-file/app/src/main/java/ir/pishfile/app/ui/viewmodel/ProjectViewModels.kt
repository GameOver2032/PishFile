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

class ProjectEditViewModel(private val repository: ProjectRepository) : ViewModel() {

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
            isLoaded = true
        }
    }

    fun update(transform: (ProjectForm) -> ProjectForm) { form = transform(form) }

    fun save(onSaved: (String) -> Unit) {
        viewModelScope.launch {
            if (form.name.isBlank()) return@launch
            val entity = form.toEntity(existing)
            repository.save(entity)
            onSaved(entity.id)
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class ProjectDetailViewModel(
    private val projectRepository: ProjectRepository,
    private val unitRepository: UnitRepository,
    private val preFileRepository: PreFileRepository,
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

    fun setProjectId(id: String) { projectId.value = id }

    fun delete(onDone: () -> Unit) {
        val id = projectId.value ?: return
        viewModelScope.launch {
            projectRepository.delete(id)
            onDone()
        }
    }
}
