package ir.pishfile.app.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.pishfile.app.core.Constants
import ir.pishfile.app.core.Formatters
import ir.pishfile.app.data.local.dao.PreFileRow
import ir.pishfile.app.data.local.entity.AttachmentEntity
import ir.pishfile.app.data.local.entity.CustomerEntity
import ir.pishfile.app.data.local.entity.NoteEntity
import ir.pishfile.app.data.local.entity.PreFileEntity
import ir.pishfile.app.data.local.entity.ProjectAreaEntity
import ir.pishfile.app.data.local.entity.ProjectEntity
import ir.pishfile.app.data.local.entity.UnitEntity
import ir.pishfile.app.data.repository.AttachmentRepository
import ir.pishfile.app.data.repository.CustomerRepository
import ir.pishfile.app.data.repository.FollowUpRepository
import ir.pishfile.app.data.repository.NoteRepository
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

    // نوع فایل (واحد آماده / پیش‌فروش) و متراژ انتخاب‌شده از متراژهای پروژه
    val fileType: String = Constants.FILE_TYPE_PRESALE,
    val areaId: String = "",
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
            fileType = fileType,
            areaId = areaId.ifBlank { null },
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
            fileType = entity.fileType ?: Constants.FILE_TYPE_PRESALE,
            areaId = entity.areaId.orEmpty(),
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

open class PreFileEditViewModel(
    protected val repository: PreFileRepository,
    protected val projectRepository: ProjectRepository,
    protected val unitRepository: UnitRepository,
    protected val projectAreaRepository: ProjectAreaRepository,
) : ViewModel() {

    var form by mutableStateOf(PreFileForm())
        protected set

    var projects by mutableStateOf<List<ProjectEntity>>(emptyList())
        private set
    var availableUnits by mutableStateOf<List<UnitEntity>>(emptyList())
        private set
    var projectAreas by mutableStateOf<List<ProjectAreaEntity>>(emptyList())
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

    fun startNew(projectId: String?, unitId: String?, fileType: String = Constants.FILE_TYPE_PRESALE) {
        if (isLoaded) return
        viewModelScope.launch {
            projects = projectRepository.getAll()
            // اگر واحد مشخص شد ولی پروژه نه، پروژه از خودِ واحد گرفته می‌شود
            val givenUnit = unitId?.takeIf { it.isNotBlank() }?.let { unitRepository.getById(it) }
            val defaultProject = projectId?.takeIf { it.isNotBlank() }
                ?: givenUnit?.projectId
                ?: projects.firstOrNull()?.id.orEmpty()

            availableUnits = unitRepository.getByProject(defaultProject)
            selectedUnit = givenUnit
                ?: availableUnits.firstOrNull { it.status == Constants.UNIT_AVAILABLE }

            form = PreFileForm(
                draftNumber = repository.nextDraftNumber(),
                projectId = defaultProject,
                unitId = selectedUnit?.id.orEmpty(),
                status = Constants.PREFILE_NORMAL,
                fileType = fileType,
            )

            // اگر پروژه انتخاب شده باشد، ویژگی‌های پروژه را روی فرم بنشان
            val prj = projects.firstOrNull { it.id == defaultProject }
            if (prj != null) {
                applyProjectToForm(prj)
            }
            if (selectedUnit != null) {
                applyUnitToForm(selectedUnit!!)
            }

            loadProjectAreas(defaultProject)

            isLoaded = true
        }
    }

    /** بارگذاری متراژهای پروژه + انتخاب خودکار اگر فقط یک متراژ داشته باشد */
    private fun loadProjectAreas(projectId: String) {
        viewModelScope.launch {
            val areas = if (projectId.isBlank()) emptyList() else projectAreaRepository.getByProject(projectId)
            projectAreas = areas
            if (areas.size == 1) {
                applyAreaToForm(areas.first())
            }
        }
    }

    /**
     * با انتخاب پروژه، فرم به «حالت پیش‌فرض همان پروژه» بازمی‌گردد:
     * فیلدهای مخصوص فایل (شماره، تاریخ، مالک، وضعیت) حفظ می‌شوند و
     * بقیه از پیش‌فرض‌های پروژه پر می‌شوند.
     */
    fun selectProject(projectId: String) {
        viewModelScope.launch {
            availableUnits = unitRepository.getByProject(projectId)
            val project = projects.firstOrNull { it.id == projectId }
            val kept = form
            form = PreFileForm(
                draftNumber = kept.draftNumber,
                draftDate = kept.draftDate,
                projectId = projectId,
                ownerName = kept.ownerName,
                ownerPhone = kept.ownerPhone,
                status = kept.status,
                fileType = kept.fileType,
            )
            selectedUnit = null
            if (project != null) {
                applyProjectToForm(project)
            }
        }
        // متراژها را دوباره بارگذاری می‌کنیم (با یک متراژ، خودکار انتخاب می‌شود)
        loadProjectAreas(projectId)
    }

    /** پروژه‌ی انتخاب‌شده در فرم */
    fun selectedProject(): ProjectEntity? = projects.firstOrNull { it.id == form.projectId }

    /**
     * با انتخاب پروژه، همه‌ی پیش‌فرض‌های آن روی فرم می‌نشیند:
     * مدل قیمت‌گذاری، واریزی تا امروز، متری/سهم، امتیاز، رتبه،
     * شرایط فروش، اقساط و تاریخ تحویل. کاربر فقط فیلدهای خالی را می‌پرند.
     */
    private fun applyProjectToForm(project: ProjectEntity) {
        val model = project.pricingModel.ifBlank { Constants.PRICING_METER }
        form = form.copy(
            pricingModel = model,
            pricePerMeter = project.salePricePerMeter?.toString() ?: form.pricePerMeter,
            depositAmount = project.defaultDepositAmount?.toString() ?: form.depositAmount,
            bonusAmount = project.defaultBonusAmount?.toString() ?: form.bonusAmount,
            shareMeterArea = project.shareMeterArea?.toString() ?: form.shareMeterArea,
            sharePrice = project.sharePrice?.toString() ?: form.sharePrice,
            deliveryDate = form.deliveryDate.ifBlank { project.deliveryDate.orEmpty() },
            hasRanking = project.hasRanking,
            ranking = if (project.hasRanking) {
                project.defaultRanking?.takeIf { it.isNotBlank() } ?: "رتبه "
            } else {
                ""
            },
            saleConditionCash = project.saleConditionCash,
            saleConditionInstallment = project.saleConditionInstallment,
            saleConditionExchange = project.saleConditionExchange,
            saleConditionNotes = project.saleConditionNotes.orEmpty(),
            installmentCount = project.installmentCount?.toString() ?: form.installmentCount,
            remainingInstallmentsCount = project.remainingInstallmentsCount?.toString() ?: form.remainingInstallmentsCount,
            installmentAmount = project.installmentAmount?.toString() ?: form.installmentAmount,
            installmentPeriod = project.installmentPeriod?.takeIf { it.isNotBlank() } ?: form.installmentPeriod,
            nextInstallmentDueDate = project.nextInstallmentDueDate?.takeIf { it.isNotBlank() } ?: form.nextInstallmentDueDate,
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
            // قیمت کلِ متریِ واحد فقط برای فایل‌های متری معنادار است
            // (در مدل واریزی-امتیاز، قیمت کل = واریزی + امتیاز است)
            totalPrice = if (form.pricingModel == Constants.PRICING_METER) {
                (unit.finalPrice ?: unit.totalPrice)?.toString() ?: form.totalPrice
            } else {
                form.totalPrice
            },
            installmentCount = unit.suggestedInstallmentCount?.toString() ?: form.installmentCount,
            installmentAmount = unit.suggestedInstallment?.toString() ?: form.installmentAmount,
            deliveryDate = form.deliveryDate.ifBlank { unit.deliveryDate.orEmpty() },
        )
    }

    /** انتخاب متراژ از متراژهای پروژه — شرایط مالی آن متراژ روی فرم می‌نشیند */
    fun selectArea(areaId: String) {
        val area = projectAreas.firstOrNull { it.id == areaId } ?: return
        applyAreaToForm(area)
    }

    /** شرایط مالی متراژ انتخاب‌شده روی فرم بنشیند (فقط فیلدهایی که متراژ مقدار دارد) */
    private fun applyAreaToForm(area: ProjectAreaEntity) {
        form = form.copy(
            areaId = area.id,
            depositAmount = area.depositAmount?.toString() ?: form.depositAmount,
            bonusAmount = area.bonusAmount?.toString() ?: form.bonusAmount,
            totalPrice = area.totalPrice?.toString() ?: form.totalPrice,
            meterArea = area.areaValue?.toString() ?: form.meterArea,
            installmentCount = area.installmentCount?.toString() ?: form.installmentCount,
            installmentAmount = area.installmentAmount?.toString() ?: form.installmentAmount,
            installmentPeriod = area.installmentPeriod?.takeIf { it.isNotBlank() } ?: form.installmentPeriod,
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

/** قدم‌های ثبت سریع فایل پیش‌فروش */
enum class PreFileWizardStep(val title: String) {
    PROJECT("پروژه و واحد"),
    AREA("انتخاب متراژ"),
    OWNER("مالک / سپارنده فایل"),
    PRICE("قیمت فایل"),
    RANK("رتبه در پروژه"),
    SALE("شرایط فروش"),
    INSTALLMENT("اقساط"),
    STATUS("وضعیت و تحویل"),
}

/**
 * ثبت سریع فایل پیش‌فروش به‌صورت قدم‌به‌قدم:
 * پروژه یک‌بار کامل تعریف می‌شود (پیش‌فرض‌ها روی پروژه) و در ثبت فایل
 * برنامه فقط فیلدهای خالی را می‌پرسد. قیمت کل (واریزی + امتیاز) همیشه
 * به‌عنوان یک فیلد مشخص به‌روزرسانی می‌شود.
 */
class PreFileWizardViewModel(
    repository: PreFileRepository,
    projectRepository: ProjectRepository,
    unitRepository: UnitRepository,
) : PreFileEditViewModel(repository, projectRepository, unitRepository) {

    var currentStepIndex by mutableStateOf(0)
        private set
    var stepError by mutableStateOf<String?>(null)
        private set

    /** فهرست قدم‌ها بر اساس پیش‌فرض‌های پروژه‌ی انتخاب‌شده */
    fun steps(project: ProjectEntity?): List<PreFileWizardStep> = buildList {
        add(PreFileWizardStep.PROJECT)
        // پروژه‌ای که چند متراژ دارد، انتخاب متراژ اجباری است
        if (projectAreas.size >= 2) add(PreFileWizardStep.AREA)
        add(PreFileWizardStep.OWNER)
        add(PreFileWizardStep.PRICE)
        if (project?.hasRanking == true) add(PreFileWizardStep.RANK)
        add(PreFileWizardStep.SALE)
        // اقساط و تاریخ تحویل دیگر از کاربر پرسیده نمی‌شوند؛ از پروژه می‌آیند
        add(PreFileWizardStep.STATUS)
    }

    /** قدم جاری (با محدودسازی امن در صورت تغییر پروژه) */
    fun currentStep(): PreFileWizardStep {
        val list = steps(selectedProject())
        return list.getOrNull(currentStepIndex.coerceIn(0, list.size - 1)) ?: list.last()
    }

    /** قدم‌های انتخابی (همه‌چیز از پیش‌فرض پروژه پر شده و قابل رد کردن است) */
    fun isOptionalStep(step: PreFileWizardStep): Boolean = when (step) {
        PreFileWizardStep.SALE, PreFileWizardStep.INSTALLMENT, PreFileWizardStep.STATUS -> true
        else -> false
    }

    /** اعتبارسنجی قدم جاری؛ در صورت خطا، پیام روی stepError می‌نشیند */
    fun validateCurrentStep(): Boolean {
        stepError = when (currentStep()) {
            PreFileWizardStep.PROJECT ->
                if (form.projectId.isBlank()) "ابتدا یک پروژه انتخاب کنید" else null
            PreFileWizardStep.AREA ->
                if (form.areaId.isBlank()) "یکی از متراژهای پروژه را انتخاب کنید" else null
            PreFileWizardStep.OWNER ->
                if (form.ownerName.isBlank()) "نام مالک / سپارنده را وارد کنید" else null
            PreFileWizardStep.PRICE ->
                if (form.computedTotal <= 0) "مبلغ امتیاز یا قیمت کل را وارد کنید" else null
            PreFileWizardStep.RANK -> {
                // پیش‌فرض «رتبه » به‌تنهایی کافی نیست؛ باید مقدار مشخصی بنویسد
                val meaningful = form.ranking.trim().removePrefix("رتبه").trim()
                if (meaningful.isEmpty()) "رتبه فایل را بنویسید (مثلاً: رتبه ۱۲)" else null
            }
            else -> null
        }
        return stepError == null
    }

    /** رفتن به قدم بعدی؛ در قدم آخر، ثبت فایل */
    fun next(onSaved: (String) -> Unit) {
        if (!validateCurrentStep()) return
        val list = steps(selectedProject())
        if (currentStepIndex >= list.size - 1) {
            save(onSaved)
        } else {
            currentStepIndex++
            stepError = null
        }
    }

    /** بازگشت به قدم قبل */
    fun back() {
        if (currentStepIndex > 0) {
            currentStepIndex--
            stepError = null
        }
    }

    /** پرش مستقیم (برای قدم‌های انتخابی) */
    fun goTo(index: Int) {
        val list = steps(selectedProject())
        if (index in list.indices) {
            currentStepIndex = index
            stepError = null
        }
    }

    // ---------- همگام‌سازی دوطرفه‌ی قیمت (مدل واریزی و امتیاز) ----------
    // قانون: قیمت کل پیشنهادی مالک = واریزی + امتیاز
    // تغییر هرکدام، دیگری را تنظیم می‌کند.

    private fun syncTotal(f: PreFileForm): String {
        if (f.pricingModel != Constants.PRICING_DEPOSIT_BONUS) return f.totalPrice
        val total = f.computedTotal
        return if (total > 0) Formatters.amount(total) else ""
    }

    /** کاربر مبلغ امتیاز را تغییر داد → قیمت کل به‌روز می‌شود */
    fun onBonusChange(v: String) {
        val f = form.copy(bonusAmount = v)
        form = f.copy(totalPrice = syncTotal(f))
    }

    /** کاربر واریزی را تغییر داد → قیمت کل به‌روز می‌شود */
    fun onDepositChange(v: String) {
        val f = form.copy(depositAmount = v)
        form = f.copy(totalPrice = syncTotal(f))
    }

    /** کاربر مستقیم قیمت کل را نوشت → امتیاز از آن کمّیت می‌شود */
    fun onTotalChange(v: String) {
        if (form.pricingModel != Constants.PRICING_DEPOSIT_BONUS) {
            form = form.copy(totalPrice = v)
            return
        }
        val total = Formatters.parseLong(v)
        if (total == null || total <= 0) {
            form = form.copy(totalPrice = "")
            return
        }
        val bonus = (total - form.depositValue).coerceAtLeast(0L)
        val f = form.copy(bonusAmount = if (bonus > 0) Formatters.amount(bonus) else "")
        form = f.copy(totalPrice = syncTotal(f))
    }
}

class PreFileDetailViewModel(
    private val repository: PreFileRepository,
    private val unitRepository: UnitRepository,
    private val followUpRepository: FollowUpRepository,
    private val customerRepository: CustomerRepository,
    private val noteRepository: NoteRepository,
    private val attachmentRepository: AttachmentRepository,
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

    val customers: StateFlow<List<CustomerEntity>> = preFileId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else customerRepository.observeByPreFile(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val notes: StateFlow<List<NoteEntity>> = preFileId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else noteRepository.observeByPreFile(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val attachments: StateFlow<List<AttachmentEntity>> = preFileId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList())
            else attachmentRepository.observeByOwner(Constants.ATTACH_PREFILE, id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setPreFileId(id: String) { preFileId.value = id }

    fun saveAttachment(attachment: AttachmentEntity) {
        viewModelScope.launch { attachmentRepository.save(attachment) }
    }

    fun deleteAttachment(attachment: AttachmentEntity) {
        viewModelScope.launch { attachmentRepository.delete(attachment) }
    }

    fun saveNote(note: NoteEntity, onSaved: () -> Unit) {
        viewModelScope.launch {
            noteRepository.save(note)
            onSaved()
        }
    }

    fun deleteNote(id: String) {
        viewModelScope.launch { noteRepository.delete(id) }
    }

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
