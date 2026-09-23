package ir.pishfile.app.ui.screens.projects

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ir.pishfile.app.core.Constants
import ir.pishfile.app.data.local.entity.ProjectAreaEntity
import ir.pishfile.app.core.Formatters
import ir.pishfile.app.data.local.entity.ProjectEntity
import ir.pishfile.app.ui.AppViewModelProvider
import ir.pishfile.app.ui.components.ConfirmDialog
import ir.pishfile.app.ui.components.GameHeader
import ir.pishfile.app.ui.components.GameStat
import ir.pishfile.app.ui.components.DropdownField
import ir.pishfile.app.ui.components.EmptyState
import ir.pishfile.app.ui.components.InfoRow
import ir.pishfile.app.ui.components.JalaliDateField
import ir.pishfile.app.ui.components.MoneyField
import ir.pishfile.app.ui.components.NumberField
import ir.pishfile.app.ui.components.SearchField
import ir.pishfile.app.ui.components.SectionCard
import ir.pishfile.app.ui.components.SpacerH
import ir.pishfile.app.ui.components.StatusChip
import ir.pishfile.app.ui.viewmodel.ProjectDetailViewModel
import ir.pishfile.app.ui.viewmodel.ProjectEditViewModel
import ir.pishfile.app.ui.viewmodel.ProjectAreaItem
import ir.pishfile.app.ui.viewmodel.ProjectForm
import ir.pishfile.app.ui.viewmodel.ProjectListViewModel

@Composable
fun ProjectListScreen(
    onOpen: (String) -> Unit,
    viewModel: ProjectListViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    var query by remember { mutableStateOf("") }
    var pendingDelete by remember { mutableStateOf<ProjectEntity?>(null) }
    val projects by viewModel.projects.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize()) {
        GameHeader(
            title = "پروژه‌ها",
            emoji = "🏗️",
            subtitle = "پروژه‌های ساختمانی و متراژهای آن‌ها",
            stats = listOf(GameStat(Formatters.number(projects.size), "کل پروژه‌ها")),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        )
        SearchField(
            query = query,
            onQueryChange = {
                query = it
                viewModel.setQuery(it)
            },
            placeholder = "نام پروژه، کد، شهر…",
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        )

        if (projects.isEmpty()) {
            EmptyState(
                title = "پروژه‌ای ثبت نشده",
                subtitle = "برای شروع، یک پروژه‌ی ساختمانی بسازید تا فایل‌های پیش‌فروش و واحدها به آن متصل شوند",
                icon = { Icon(Icons.Filled.Apartment, contentDescription = null) },
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(projects, key = { it.id }) { project ->
                    ProjectCard(
                        project = project,
                        onClick = { onOpen(project.id) },
                        onToggleFavorite = { viewModel.toggleFavorite(project) },
                        onDelete = { pendingDelete = project },
                    )
                }
            }
        }
    }

    pendingDelete?.let { project ->
        ConfirmDialog(
            title = "حذف پروژه",
            message = "«${project.name}» و همه‌ی پیش‌فروش‌های متصل به آن حذف می‌شوند.",
            onConfirm = { viewModel.delete(project.id) },
            onDismiss = { pendingDelete = null },
        )
    }
}

@Composable
private fun ProjectCard(
    project: ProjectEntity,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(project.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        listOfNotNull(project.city, project.district, project.projectType)
                            .joinToString(" • "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        if (project.isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                        contentDescription = "علاقه‌مندی",
                        tint = MaterialTheme.colorScheme.secondary,
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
                }
            }
            SpacerH(8)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                StatusChip(Constants.projectPricingModelLabel(project.pricingModel), MaterialTheme.colorScheme.primary)
                StatusChip(Constants.projectPhaseLabel(project.phase), MaterialTheme.colorScheme.tertiary)
                project.unitCount?.let { StatusChip("${Formatters.number(it)} واحد", MaterialTheme.colorScheme.secondary) }
            }
            SpacerH(8)
            LinearProgressIndicator(
                progress = { (project.progressPercent / 100f).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth(),
            )
            SpacerH(6)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                when (project.pricingModel) {
                    Constants.PRICING_DEPOSIT_BONUS -> {
                        Text(
                            "واریزی پیش‌فرض: ${Formatters.amountShort(project.defaultDepositAmount)} تومان",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Constants.PRICING_SHARE -> {
                        Text(
                            "سهم: ${Formatters.number(project.shareMeterArea?.toInt())} متری • هر سهم ${Formatters.amountShort(project.sharePrice)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    else -> {
                        Text(
                            "قیمت متری: ${Formatters.amountWithUnit(project.salePricePerMeter)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                project.deliveryDate?.let {
                    Text(
                        "تحویل: ${Formatters.toPersianDigits(it)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
fun ProjectEditScreen(
    projectId: String?,
    onBack: () -> Unit,
    onSaved: (String) -> Unit,
    viewModel: ProjectEditViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    if (projectId == null) viewModel.markNew() else viewModel.load(projectId)
    val form = viewModel.form

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            GameHeader(
                title = if (projectId == null) "پروژه جدید" else "ویرایش پروژه",
                emoji = "🏗️",
                subtitle = "مشخصات، مدل قیمت‌گذاری، متراژها و پیش‌فرض‌های ثبت فایل",
            )
        }

        item {
            SectionCard(title = "مشخصات اصلی پروژه") {
                ir.pishfile.app.ui.components.FormTextField(
                    value = form.name,
                    onValueChange = { v -> viewModel.update { it.copy(name = v) } },
                    label = "نام پروژه *",
                )
                SpacerH(8)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ir.pishfile.app.ui.components.FormTextField(
                        value = form.code,
                        onValueChange = { v -> viewModel.update { it.copy(code = v) } },
                        label = "کد پروژه",
                        modifier = Modifier.weight(1f),
                    )
                    ir.pishfile.app.ui.components.FormTextField(
                        value = form.projectType,
                        onValueChange = { v -> viewModel.update { it.copy(projectType = v) } },
                        label = "نوع پروژه",
                        modifier = Modifier.weight(1f),
                    )
                }
                SpacerH(8)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ir.pishfile.app.ui.components.FormTextField(
                        value = form.city,
                        onValueChange = { v -> viewModel.update { it.copy(city = v) } },
                        label = "شهر",
                        modifier = Modifier.weight(1f),
                    )
                    ir.pishfile.app.ui.components.FormTextField(
                        value = form.district,
                        onValueChange = { v -> viewModel.update { it.copy(district = v) } },
                        label = "محله",
                        modifier = Modifier.weight(1f),
                    )
                }
                SpacerH(8)
                ir.pishfile.app.ui.components.FormTextField(
                    value = form.address,
                    onValueChange = { v -> viewModel.update { it.copy(address = v) } },
                    label = "نشانی پروژه",
                    singleLine = false,
                    minLines = 2,
                )
            }
        }

        // مدل قیمت‌گذاری پیش‌فرض پروژه (واریزی/امتیاز، متری، سهامی)
        item {
            SectionCard(
                title = "مدل قیمت‌گذاری و شرایط پیش‌فرض پروژه",
                subtitle = "این شرایط هنگام ثبت فایل پیش‌فروش جدید برای این پروژه خودکار پر می‌شوند",
            ) {
                Text("مدل قیمت‌گذاری پروژه:", style = MaterialTheme.typography.bodySmall)
                SpacerH(6)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = form.pricingModel == Constants.PRICING_DEPOSIT_BONUS,
                        onClick = { viewModel.update { it.copy(pricingModel = Constants.PRICING_DEPOSIT_BONUS) } },
                        label = { Text("واریزی و امتیاز") },
                    )
                    FilterChip(
                        selected = form.pricingModel == Constants.PRICING_METER,
                        onClick = { viewModel.update { it.copy(pricingModel = Constants.PRICING_METER) } },
                        label = { Text("قیمت متری") },
                    )
                    FilterChip(
                        selected = form.pricingModel == Constants.PRICING_SHARE,
                        onClick = { viewModel.update { it.copy(pricingModel = Constants.PRICING_SHARE) } },
                        label = { Text("سهامی") },
                    )
                }

                SpacerH(10)

                when (form.pricingModel) {
                    Constants.PRICING_DEPOSIT_BONUS -> {
                        MoneyField(
                            value = form.defaultDepositAmount,
                            onValueChange = { v -> viewModel.update { it.copy(defaultDepositAmount = v) } },
                            label = "مبلغ واریزی پیش‌فرض پروژه تا امروز",
                            helperText = "این مبلغ با تغییر شرایط پروژه، اینجا به‌روز می‌شود",
                        )
                        SpacerH(8)
                        MoneyField(
                            value = form.defaultBonusAmount,
                            onValueChange = { v -> viewModel.update { it.copy(defaultBonusAmount = v) } },
                            label = "پیش‌فرض مبلغ امتیاز (اختیاری)",
                            helperText = "اگر خالی بماند، هنگام ثبت فایل از کاربر پرسیده می‌شود",
                        )
                    }
                    Constants.PRICING_SHARE -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            NumberField(
                                value = form.shareMeterArea,
                                onValueChange = { v -> viewModel.update { it.copy(shareMeterArea = v) } },
                                label = "متراژ هر سهم",
                                suffix = "م²",
                                decimal = true,
                                modifier = Modifier.weight(1f),
                            )
                            MoneyField(
                                value = form.sharePrice,
                                onValueChange = { v -> viewModel.update { it.copy(sharePrice = v) } },
                                label = "قیمت هر سهم",
                                modifier = Modifier.weight(1.5f),
                            )
                        }
                        SpacerH(8)
                        Text(
                            "پروژه‌های سهامی ممکن است واریزی و امتیاز هم داشته باشند:",
                            style = MaterialTheme.typography.bodySmall,
                        )
                        SpacerH(6)
                        MoneyField(
                            value = form.defaultDepositAmount,
                            onValueChange = { v -> viewModel.update { it.copy(defaultDepositAmount = v) } },
                            label = "مبلغ واریزی پروژه (اختیاری)",
                        )
                        SpacerH(8)
                        MoneyField(
                            value = form.defaultBonusAmount,
                            onValueChange = { v -> viewModel.update { it.copy(defaultBonusAmount = v) } },
                            label = "مبلغ امتیاز (اختیاری)",
                        )
                        SpacerH(8)
                        MoneyField(
                            value = form.approxTotalPrice,
                            onValueChange = { v -> viewModel.update { it.copy(approxTotalPrice = v) } },
                            label = "قیمت حدودی کل پروژه (اختیاری)",
                        )
                    }
                    else -> { // METER
                        MoneyField(
                            value = form.salePricePerMeter,
                            onValueChange = { v -> viewModel.update { it.copy(salePricePerMeter = v) } },
                            label = "قیمت پایه هر مترمربع",
                        )
                    }
                }
            }
        }

        // متراژهای پروژه با شرایط مالی مخصوص هرکدام
        item {
            AreasEditor(
                areas = form.areas,
                onAdd = { viewModel.addArea() },
                onItemChange = { id, transform -> viewModel.updateArea(id, transform) },
                onItemRemove = { id -> viewModel.removeArea(id) },
            )
        }

        // پیش‌فرض‌های ثبت فایل: رتبه، شرایط فروش و اقساط
        item {
            SectionCard(
                title = "پیش‌فرض‌های ثبت فایل",
                subtitle = "این مقادیر هنگام «ثبت فایل جدید» خودکار پر می‌شوند و فقط فیلدهای خالی از کاربر پرسیده می‌شوند",
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.material3.Switch(
                        checked = form.hasRanking,
                        onCheckedChange = { v -> viewModel.update { it.copy(hasRanking = v) } },
                    )
                    Text(
                        " فایل‌های این پروژه دارای رتبه هستند",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                if (form.hasRanking) {
                    SpacerH(8)
                    ir.pishfile.app.ui.components.FormTextField(
                        value = form.defaultRanking,
                        onValueChange = { v -> viewModel.update { it.copy(defaultRanking = v) } },
                        label = "پیش‌فرض متن رتبه (مثلاً: رتبه اولویت بلوک A)",
                    )
                }
                SpacerH(10)
                Text("شرایط فروش پیش‌فرض:", style = MaterialTheme.typography.bodySmall)
                SpacerH(4)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.material3.Checkbox(
                            checked = form.saleConditionCash,
                            onCheckedChange = { v -> viewModel.update { it.copy(saleConditionCash = v) } },
                        )
                        Text("نقد", style = MaterialTheme.typography.bodyMedium)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.material3.Checkbox(
                            checked = form.saleConditionInstallment,
                            onCheckedChange = { v -> viewModel.update { it.copy(saleConditionInstallment = v) } },
                        )
                        Text("شرایطی", style = MaterialTheme.typography.bodyMedium)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.material3.Checkbox(
                            checked = form.saleConditionExchange,
                            onCheckedChange = { v -> viewModel.update { it.copy(saleConditionExchange = v) } },
                        )
                        Text("تهاتر", style = MaterialTheme.typography.bodyMedium)
                    }
                }
                SpacerH(8)
                ir.pishfile.app.ui.components.FormTextField(
                    value = form.saleConditionNotes,
                    onValueChange = { v -> viewModel.update { it.copy(saleConditionNotes = v) } },
                    label = "توضیحات پیش‌فرض شرایط فروش (مثلاً نوع خودرو جهت تهاتر)",
                    singleLine = false,
                    minLines = 2,
                )
                SpacerH(10)
                Text("اقساط پیش‌فرض پروژه:", style = MaterialTheme.typography.bodySmall)
                SpacerH(6)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumberField(
                        value = form.installmentCount,
                        onValueChange = { v -> viewModel.update { it.copy(installmentCount = v) } },
                        label = "تعداد کل اقساط",
                        modifier = Modifier.weight(1f),
                    )
                    NumberField(
                        value = form.remainingInstallmentsCount,
                        onValueChange = { v -> viewModel.update { it.copy(remainingInstallmentsCount = v) } },
                        label = "اقساط باقی‌مانده",
                        modifier = Modifier.weight(1f),
                    )
                }
                SpacerH(8)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MoneyField(
                        value = form.installmentAmount,
                        onValueChange = { v -> viewModel.update { it.copy(installmentAmount = v) } },
                        label = "مبلغ هر قسط",
                        modifier = Modifier.weight(1.2f),
                    )
                    DropdownField(
                        label = "دوره پرداخت",
                        options = listOf("ماهانه", "دو ماهه", "سه ماهه / فصلی", "شش ماهه", "سالانه"),
                        selected = form.installmentPeriod.takeIf { it.isNotBlank() },
                        onSelect = { v -> viewModel.update { it.copy(installmentPeriod = v) } },
                        allowEmpty = true,
                        emptyLabel = "انتخاب کنید",
                        modifier = Modifier.weight(1f),
                    )
                }
                SpacerH(8)
                JalaliDateField(
                    value = form.nextInstallmentDueDate,
                    onValueChange = { v -> viewModel.update { it.copy(nextInstallmentDueDate = v) } },
                    label = "تاریخ سررسید قسط پیش‌رو",
                    quickMonths = listOf(1, 2, 3),
                )
            }
        }

        item {
            SectionCard(title = "پیشرفت و زمان‌بندی") {
                DropdownField(
                    label = "مرحله پروژه",
                    options = Constants.projectPhases.map { Constants.projectPhaseLabel(it) },
                    selected = Constants.projectPhaseLabel(form.phase),
                    onSelect = { label ->
                        val code = Constants.projectPhases.firstOrNull { Constants.projectPhaseLabel(it) == label } ?: form.phase
                        viewModel.update { it.copy(phase = code) }
                    },
                )
                SpacerH(8)
                NumberField(
                    value = form.progressPercent,
                    onValueChange = { v -> viewModel.update { it.copy(progressPercent = v) } },
                    label = "درصد پیشرفت فیزیکی",
                    suffix = "٪",
                )
                SpacerH(8)
                JalaliDateField(
                    value = form.deliveryDate,
                    onValueChange = { v -> viewModel.update { it.copy(deliveryDate = v) } },
                    label = "تاریخ تقریبی تحویل پروژه",
                )
            }
        }

        item {
            SectionCard(title = "توضیحات و امکانات") {
                ir.pishfile.app.ui.components.MultiSelectChips(
                    label = "امکانات پروژه",
                    options = Constants.facilities,
                    selected = form.facilities.split(",").filter { it.isNotBlank() }.toSet(),
                    onToggle = { item ->
                        val current = form.facilities.split(",").filter { it.isNotBlank() }.toMutableSet()
                        if (!current.add(item)) current.remove(item)
                        viewModel.update { it.copy(facilities = current.joinToString(",")) }
                    },
                )
                SpacerH(8)
                ir.pishfile.app.ui.components.FormTextField(
                    value = form.description,
                    onValueChange = { v -> viewModel.update { it.copy(description = v) } },
                    label = "توضیحات پروژه جهت ارائه به مشتریان",
                    singleLine = false,
                    minLines = 3,
                )
            }
        }

        item {
            Button(
                onClick = { viewModel.save { id -> onSaved(id) } },
                modifier = Modifier.fillMaxWidth(),
            ) { Text(if (projectId == null) "ثبت پروژه" else "ذخیره تغییرات") }
        }
        item {
            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("انصراف") }
        }
    }
}

@Composable
fun ProjectDetailScreen(
    projectId: String,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onAddUnit: () -> Unit,
    onOpenUnit: (String) -> Unit,
    onOpenPreFile: (String) -> Unit,
    viewModel: ProjectDetailViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    viewModel.setProjectId(projectId)

    val project by viewModel.project.collectAsStateWithLifecycle()
    val units by viewModel.units.collectAsStateWithLifecycle()
    val preFiles by viewModel.preFiles.collectAsStateWithLifecycle()
    val areas by viewModel.areas.collectAsStateWithLifecycle()
    var showDelete by remember { mutableStateOf(false) }

    val current = project ?: return

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            GameHeader(
                title = current.name,
                emoji = "🏗️",
                subtitle = listOfNotNull(current.city, current.district).joinToString(" • "),
                stats = listOf(
                    GameStat(Formatters.percent(current.progressPercent), "پیشرفت"),
                    GameStat(Formatters.number(areas.size), "متراژ"),
                    GameStat(Formatters.number(preFiles.size), "فایل"),
                ),
            )
        }

        item {
            SectionCard(title = current.name, subtitle = listOfNotNull(current.city, current.district).joinToString(" • ")) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusChip(Constants.projectPricingModelLabel(current.pricingModel), MaterialTheme.colorScheme.primary)
                    SpacerH(0)
                    StatusChip("مرحله: ${Constants.projectPhaseLabel(current.phase)}", MaterialTheme.colorScheme.tertiary)
                    Row(Modifier.weight(1f), horizontalArrangement = Arrangement.End) {
                        IconButton(onClick = onEdit) { Icon(Icons.Filled.Edit, contentDescription = "ویرایش") }
                        IconButton(onClick = { showDelete = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
                SpacerH(8)
                LinearProgressIndicator(
                    progress = { (current.progressPercent / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth(),
                )
                SpacerH(10)
                when (current.pricingModel) {
                    Constants.PRICING_DEPOSIT_BONUS -> {
                        InfoRow("مدل فروش", "واریزی و امتیاز")
                        InfoRow("واریزی تا امروز", Formatters.amountWithUnit(current.defaultDepositAmount), emphasize = true)
                        InfoRow("مجموع واریزی + امتیاز", formatters_sum(current.defaultDepositAmount, current.defaultBonusAmount), emphasize = true)
                    }
                    Constants.PRICING_SHARE -> {
                        InfoRow("مدل فروش", "سهامی")
                        InfoRow("متراژ هر سهم", current.shareMeterArea?.let { "${Formatters.number(it.toInt())} م²" })
                        InfoRow("قیمت هر سهم", Formatters.amountWithUnit(current.sharePrice), emphasize = true)
                        InfoRow("واریزی پروژه", Formatters.amountWithUnit(current.defaultDepositAmount))
                        InfoRow("مبلغ امتیاز", Formatters.amountWithUnit(current.defaultBonusAmount))
                        InfoRow("قیمت حدودی کل", Formatters.amountWithUnit(current.approxTotalPrice), emphasize = true)
                        InfoRow("مجموع واریزی + امتیاز", formatters_sum(current.defaultDepositAmount, current.defaultBonusAmount), emphasize = true)
                    }
                    else -> {
                        InfoRow("مدل فروش", "قیمت متری")
                        InfoRow("قیمت فروش هر متر", Formatters.amountWithUnit(current.salePricePerMeter), emphasize = true)
                    }
                }
                InfoRow("تحویل پروژه", current.deliveryDate?.let { Formatters.toPersianDigits(it) })
                InfoRow("نشانی", current.address)
                InfoRow("امکانات", current.facilities?.replace(",", " • "))
                InfoRow("توضیحات", current.description)
            }
        }

        item {
            SectionCard(
                title = "پیش‌فرض‌های ثبت فایل",
                subtitle = "هنگام ثبت فایل جدید، این مقادیر خودکار پر می‌شوند و فقط فیلدهای خالی پرسیده می‌شوند",
            ) {
                if (current.pricingModel == Constants.PRICING_DEPOSIT_BONUS) {
                    InfoRow("پیش‌فرض امتیاز", Formatters.amountWithUnit(current.defaultBonusAmount))
                }
                InfoRow(
                    "رتبه‌بندی فایل‌ها",
                    when {
                        !current.hasRanking -> "خیر"
                        current.defaultRanking.isNullOrBlank() -> "بله — متن رتبه هنگام ثبت پرسیده می‌شود"
                        else -> "بله — پیش‌فرض: ${current.defaultRanking}"
                    },
                    emphasize = current.hasRanking,
                )
                InfoRow(
                    "شرایط فروش پیش‌فرض",
                    buildList {
                        if (current.saleConditionCash) add("نقد")
                        if (current.saleConditionInstallment) add("شرایطی")
                        if (current.saleConditionExchange) add("تهاتر")
                    }.joinToString("، ").ifBlank { "تعیین‌نشده" },
                )
                InfoRow("توضیحات شرایط فروش", current.saleConditionNotes)
                InfoRow(
                    "اقساط پیش‌فرض",
                    listOfNotNull(
                        current.installmentCount?.let { "تعداد: ${Formatters.number(it)}" },
                        current.remainingInstallmentsCount?.let { "مانده: ${Formatters.number(it)}" },
                        current.installmentAmount?.let { "مبلغ قسط: ${Formatters.amountShort(it)} تومان" },
                        current.installmentPeriod?.let { "دوره: $it" },
                    ).joinToString(" • ").ifBlank { "تعیین‌نشده" },
                )
                InfoRow(
                    "سررسید قسط پیش‌رو",
                    current.nextInstallmentDueDate?.let { Formatters.toPersianDigits(it) },
                    emphasize = true,
                )
                Text(
                    "✅ وقتی به تاریخ سررسید رسیدیم، قسط به‌صورت خودکار از اینجا پاک می‌شود و مبلغش به واریزی پروژه اضافه می‌شود.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

        item {
            SectionCard(
                title = "متراژهای پروژه (${Formatters.number(areas.size)})",
                subtitle = "هر متراژ شرایط مالی مخصوص خودش را دارد؛ هنگام ثبت فایل انتخاب می‌شود",
            ) {
                if (areas.isEmpty()) {
                    Text(
                        "متراژی ثبت نشده — برای پروژه‌های چند متراژ، متراژها را از بخش ویرایش پروژه اضافه کنید",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    areas.forEach { area ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                        ) {
                            Column(Modifier.padding(10.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(
                                        area.label + (area.areaValue?.let { " — ${Formatters.number(it.toInt())} م²" } ?: ""),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    area.totalPrice?.let {
                                        StatusChip(Formatters.amountShort(it) + " تومان", MaterialTheme.colorScheme.primary)
                                    }
                                }
                                SpacerH(4)
                                Text(
                                    listOfNotNull(
                                        area.depositAmount?.let { "واریزی: ${Formatters.amountShort(it)}" },
                                        area.bonusAmount?.let { "امتیاز: ${Formatters.amountShort(it)}" },
                                        area.installmentCount?.let { "${Formatters.number(it)} قسط" },
                                        area.installmentAmount?.let { "مبلغ قسط: ${Formatters.amountShort(it)}" },
                                        area.installmentPeriod?.let { "دوره: $it" },
                                    ).joinToString(" • "),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        SpacerH(6)
                    }
                }
            }
        }

        item {
            SectionCard(
                title = "فایل‌های پیش‌فروش این پروژه (${Formatters.number(preFiles.size)})",
            ) {
                if (preFiles.isEmpty()) {
                    Text("هنوز فایل پیش‌فروشی برای این پروژه ثبت نشده است.", style = MaterialTheme.typography.bodySmall)
                } else {
                    preFiles.forEach { row ->
                        Card(
                            onClick = { onOpenPreFile(row.preFile.id) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                        ) {
                            Column(Modifier.padding(10.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(
                                        "${row.preFile.draftNumber} • ${row.unitTitle ?: "بدون واحد"}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    StatusChip(
                                        Constants.preFileStatusLabel(row.preFile.status),
                                        ir.pishfile.app.ui.components.preFileStatusColor(row.preFile.status),
                                    )
                                }
                                Text(
                                    listOfNotNull(
                                        row.preFile.ownerName?.let { "مالک: $it" },
                                        "قیمت کل: ${Formatters.amountShort(row.preFile.displayPrice)} تومان",
                                    ).joinToString(" • "),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        SpacerH(6)
                    }
                }
            }
        }

        item {
            SectionCard(
                title = "واحدهای پروژه (${Formatters.number(units.size)})",
                trailing = {
                    IconButton(onClick = onAddUnit) { Icon(Icons.Filled.Add, contentDescription = "افزودن واحد") }
                },
            ) {
                if (units.isEmpty()) {
                    Text(
                        "هنوز واحدی ثبت نشده — با دکمه + واحد اضافه کنید",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    units.forEach { unit ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(unit.displayTitle, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    listOfNotNull(
                                        unit.grossArea?.let { "${Formatters.number(it.toInt())} م²" },
                                        unit.direction,
                                    ).joinToString(" • "),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            StatusChip(
                                Constants.unitStatusLabel(unit.status),
                                ir.pishfile.app.ui.components.unitStatusColor(unit.status),
                            )
                            IconButton(onClick = { onOpenUnit(unit.id) }) {
                                Icon(Icons.Filled.Note, contentDescription = "جزئیات")
                            }
                        }
                    }
                }
            }
        }

        item {
            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("بازگشت") }
        }
    }

    if (showDelete) {
        ConfirmDialog(
            title = "حذف پروژه",
            message = "پروژه و همه‌ی فایل‌های پیش‌فروش آن حذف شوند؟",
            onConfirm = { viewModel.delete(onBack) },
            onDismiss = { showDelete = false },
        )
    }
}

// ---------------------------------------------------------------------------
// ویرایشگر متراژهای پروژه
// ---------------------------------------------------------------------------
@Composable
private fun AreasEditor(
    areas: List<ProjectAreaItem>,
    onAdd: () -> Unit,
    onItemChange: (String, (ProjectAreaItem) -> ProjectAreaItem) -> Unit,
    onItemRemove: (String) -> Unit,
) {
    SectionCard(
        title = "متراژهای پروژه",
        subtitle = "اگر پروژه چند متراژ دارد، هر متراژ واریزی، امتیاز، قیمت کل و شرایط اقساط مخصوص خودش را دارد؛ هنگام ثبت فایل، متراژ انتخاب می‌شود",
        trailing = {
            OutlinedButton(onClick = onAdd) { Text("افزودن متراژ") }
        },
    ) {
        if (areas.isEmpty()) {
            Text(
                "متراژی ثبت نشده — برای پروژه‌ای با متراژهای مختلف، هر متراژ را با شرایط مالی‌اش اضافه کنید",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            areas.forEachIndexed { index, area ->
                Column(Modifier.padding(vertical = 6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "متراژ ${Formatters.number(index + 1)}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f),
                        )
                        IconButton(onClick = { onItemRemove(area.id) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "حذف متراژ", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        NumberField(
                            value = area.areaValue,
                            onValueChange = { v -> onItemChange(area.id) { it.copy(areaValue = v) } },
                            label = "متراژ",
                            suffix = "م²",
                            decimal = true,
                            modifier = Modifier.weight(1f),
                        )
                        FormTextField(
                            value = area.label,
                            onValueChange = { v -> onItemChange(area.id) { it.copy(label = v) } },
                            label = "برچسب (اختیاری)",
                            modifier = Modifier.weight(1.2f),
                        )
                    }
                    SpacerH(8)
                    MoneyField(
                        value = area.totalPrice,
                        onValueChange = { v -> onItemChange(area.id) { it.copy(totalPrice = v) } },
                        label = "قیمت کل این متراژ",
                    )
                    SpacerH(8)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MoneyField(
                            value = area.depositAmount,
                            onValueChange = { v -> onItemChange(area.id) { it.copy(depositAmount = v) } },
                            label = "واریزی",
                            modifier = Modifier.weight(1f),
                        )
                        MoneyField(
                            value = area.bonusAmount,
                            onValueChange = { v -> onItemChange(area.id) { it.copy(bonusAmount = v) } },
                            label = "امتیاز",
                            modifier = Modifier.weight(1f),
                        )
                    }
                    SpacerH(8)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        NumberField(
                            value = area.installmentCount,
                            onValueChange = { v -> onItemChange(area.id) { it.copy(installmentCount = v) } },
                            label = "تعداد اقساط",
                            modifier = Modifier.weight(1f),
                        )
                        MoneyField(
                            value = area.installmentAmount,
                            onValueChange = { v -> onItemChange(area.id) { it.copy(installmentAmount = v) } },
                            label = "مبلغ هر قسط",
                            modifier = Modifier.weight(1.2f),
                        )
                        DropdownField(
                            label = "دوره پرداخت",
                            options = listOf("ماهانه", "دو ماهه", "سه ماهه / فصلی", "شش ماهه", "سالانه"),
                            selected = area.installmentPeriod.takeIf { it.isNotBlank() },
                            onSelect = { v -> onItemChange(area.id) { it.copy(installmentPeriod = v) } },
                            allowEmpty = true,
                            emptyLabel = "انتخاب کنید",
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

/** مجموع واریزی + امتیاز (نمایش «—» اگر هر دو خالی باشد) */
private fun formatters_sum(deposit: Long?, bonus: Long?): String {
    val total = (deposit ?: 0L) + (bonus ?: 0L)
    return if (total > 0) Formatters.amountWithUnit(total) else "—"
}
