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
import ir.pishfile.app.core.Formatters
import ir.pishfile.app.data.local.entity.ProjectEntity
import ir.pishfile.app.ui.AppViewModelProvider
import ir.pishfile.app.ui.components.ConfirmDialog
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
                subtitle = "برای شروع، یک پروژه‌ی ساختمانی بسازید تا واحدها و پیش‌فایل‌ها به آن متصل شوند",
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
            message = "«${project.name}» و همه‌ی واحدهای آن حذف می‌شوند. این کار قابل بازگشت نیست.",
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
                StatusChip(Constants.projectPhaseLabel(project.phase), MaterialTheme.colorScheme.tertiary)
                StatusChip(
                    "پیشرفت ${Formatters.percent(project.progressPercent)}",
                    MaterialTheme.colorScheme.primary,
                )
                project.unitCount?.let { StatusChip("${Formatters.number(it)} واحد", MaterialTheme.colorScheme.secondary) }
            }
            SpacerH(8)
            LinearProgressIndicator(
                progress = { (project.progressPercent / 100f).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth(),
            )
            SpacerH(6)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    "قیمت فروش هر متر: ${Formatters.amountWithUnit(project.salePricePerMeter)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
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
            SectionCard(title = "مشخصات اصلی") {
                FormFieldsBlock(
                    form = form,
                    update = viewModel::update,
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
private fun FormFieldsBlock(form: ProjectForm, update: ((ProjectForm) -> ProjectForm) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        ir.pishfile.app.ui.components.FormTextField(
            value = form.name,
            onValueChange = { v -> update { it.copy(name = v) } },
            label = "نام پروژه *",
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ir.pishfile.app.ui.components.FormTextField(
                value = form.code,
                onValueChange = { v -> update { it.copy(code = v) } },
                label = "کد پروژه",
                modifier = Modifier.weight(1f),
            )
            ir.pishfile.app.ui.components.FormTextField(
                value = form.projectType,
                onValueChange = { v -> update { it.copy(projectType = v) } },
                label = "نوع پروژه",
                modifier = Modifier.weight(1f),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ir.pishfile.app.ui.components.FormTextField(
                value = form.city,
                onValueChange = { v -> update { it.copy(city = v) } },
                label = "شهر",
                modifier = Modifier.weight(1f),
            )
            ir.pishfile.app.ui.components.FormTextField(
                value = form.district,
                onValueChange = { v -> update { it.copy(district = v) } },
                label = "محله",
                modifier = Modifier.weight(1f),
            )
        }
        ir.pishfile.app.ui.components.FormTextField(
            value = form.address,
            onValueChange = { v -> update { it.copy(address = v) } },
            label = "نشانی",
            singleLine = false,
            minLines = 2,
        )

        ir.pishfile.app.ui.components.SoftDivider()
        Text("مشخصات فنی", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NumberField(
                value = form.landArea,
                onValueChange = { v -> update { it.copy(landArea = v) } },
                label = "مساحت زمین",
                suffix = "م²",
                decimal = true,
                modifier = Modifier.weight(1f),
            )
            NumberField(
                value = form.totalBuiltArea,
                onValueChange = { v -> update { it.copy(totalBuiltArea = v) } },
                label = "زیربنا",
                suffix = "م²",
                decimal = true,
                modifier = Modifier.weight(1f),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NumberField(
                value = form.floorCount,
                onValueChange = { v -> update { it.copy(floorCount = v) } },
                label = "تعداد طبقات",
                modifier = Modifier.weight(1f),
            )
            NumberField(
                value = form.unitCount,
                onValueChange = { v -> update { it.copy(unitCount = v) } },
                label = "تعداد واحد",
                modifier = Modifier.weight(1f),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NumberField(
                value = form.parkingCount,
                onValueChange = { v -> update { it.copy(parkingCount = v) } },
                label = "پارکینگ",
                modifier = Modifier.weight(1f),
            )
            NumberField(
                value = form.elevatorCount,
                onValueChange = { v -> update { it.copy(elevatorCount = v) } },
                label = "آسانسور",
                modifier = Modifier.weight(1f),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ir.pishfile.app.ui.components.FormTextField(
                value = form.structureType,
                onValueChange = { v -> update { it.copy(structureType = v) } },
                label = "نوع سازه",
                modifier = Modifier.weight(1f),
            )
            ir.pishfile.app.ui.components.FormTextField(
                value = form.heatingSystem,
                onValueChange = { v -> update { it.copy(heatingSystem = v) } },
                label = "گرمایش/سرمایش",
                modifier = Modifier.weight(1f),
            )
        }

        ir.pishfile.app.ui.components.SoftDivider()
        Text("مالی و زمان‌بندی", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)

        MoneyField(
            value = form.salePricePerMeter,
            onValueChange = { v -> update { it.copy(salePricePerMeter = v) } },
            label = "قیمت فروش هر مترمربع",
        )
        MoneyField(
            value = form.costPerMeter,
            onValueChange = { v -> update { it.copy(costPerMeter = v) } },
            label = "قیمت تمام‌شده هر مترمربع",
        )
        MoneyField(
            value = form.totalBudget,
            onValueChange = { v -> update { it.copy(totalBudget = v) } },
            label = "بودجه کل پروژه",
        )
        DropdownField(
            label = "مرحله پروژه",
            options = Constants.projectPhases.map { Constants.projectPhaseLabel(it) },
            selected = Constants.projectPhaseLabel(form.phase),
            onSelect = { label ->
                val code = Constants.projectPhases.firstOrNull { Constants.projectPhaseLabel(it) == label } ?: form.phase
                update { it.copy(phase = code) }
            },
        )
        NumberField(
            value = form.progressPercent,
            onValueChange = { v -> update { it.copy(progressPercent = v) } },
            label = "درصد پیشرفت فیزیکی",
            suffix = "٪",
        )
        JalaliDateField(
            value = form.startDate,
            onValueChange = { v -> update { it.copy(startDate = v) } },
            label = "تاریخ شروع",
        )
        JalaliDateField(
            value = form.deliveryDate,
            onValueChange = { v -> update { it.copy(deliveryDate = v) } },
            label = "تاریخ تحویل",
        )

        ir.pishfile.app.ui.components.SoftDivider()
        Text("مجوزها", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
        ir.pishfile.app.ui.components.FormTextField(
            value = form.permitNumber,
            onValueChange = { v -> update { it.copy(permitNumber = v) } },
            label = "شماره پروانه ساخت",
        )
        JalaliDateField(
            value = form.permitIssueDate,
            onValueChange = { v -> update { it.copy(permitIssueDate = v) } },
            label = "تاریخ صدور پروانه",
            quickMonths = listOf(12),
        )
        ir.pishfile.app.ui.components.FormTextField(
            value = form.landDeedNumber,
            onValueChange = { v -> update { it.copy(landDeedNumber = v) } },
            label = "شماره سند زمین",
        )

        ir.pishfile.app.ui.components.SoftDivider()
        Text("امکانات و توضیحات", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
        ir.pishfile.app.ui.components.MultiSelectChips(
            label = "امکانات پروژه",
            options = Constants.facilities,
            selected = form.facilities.split(",").filter { it.isNotBlank() }.toSet(),
            onToggle = { item ->
                val current = form.facilities.split(",").filter { it.isNotBlank() }.toMutableSet()
                if (!current.add(item)) current.remove(item)
                update { it.copy(facilities = current.joinToString(",")) }
            },
        )
        ir.pishfile.app.ui.components.FormTextField(
            value = form.description,
            onValueChange = { v -> update { it.copy(description = v) } },
            label = "توضیحات",
            singleLine = false,
            minLines = 3,
        )
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
    val totalSales by viewModel.totalSales.collectAsStateWithLifecycle()
    val avgPrice by viewModel.averagePricePerMeter.collectAsStateWithLifecycle()
    var showDelete by remember { mutableStateOf(false) }

    val current = project ?: return

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            SectionCard(title = current.name, subtitle = listOfNotNull(current.city, current.district).joinToString(" • ")) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusChip(Constants.projectPhaseLabel(current.phase), MaterialTheme.colorScheme.tertiary)
                    SpacerH(0)
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
                InfoRow("تعداد واحد", Formatters.number(current.unitCount))
                InfoRow("تعداد طبقات", Formatters.number(current.floorCount))
                InfoRow("مساحت زمین", current.landArea?.let { "${Formatters.number(it.toInt())} مترمربع" })
                InfoRow("زیربنا", current.totalBuiltArea?.let { "${Formatters.number(it.toInt())} مترمربع" })
                InfoRow("سازه", current.structureType)
                InfoRow("پروانه ساخت", current.permitNumber)
                InfoRow("شماره سند", current.landDeedNumber)
                InfoRow("شروع", current.startDate?.let { Formatters.toPersianDigits(it) })
                InfoRow("تحویل", current.deliveryDate?.let { Formatters.toPersianDigits(it) })
                InfoRow("نشانی", current.address, emphasize = false)
                InfoRow("توضیحات", current.description)
            }
        }

        item {
            SectionCard(title = "خلاصه فروش") {
                InfoRow("واحدهای پیش‌فروش‌شده", "${Formatters.number(preFiles.size)} واحد")
                InfoRow("ارزش قراردادها", Formatters.amountWithUnit(totalSales))
                InfoRow("میانگین قیمت هر متر", Formatters.amountWithUnit(avgPrice?.toLong()))
                InfoRow("قیمت فروش پروژه", Formatters.amountWithUnit(current.salePricePerMeter), emphasize = true)
            }
        }

        item {
            SectionCard(
                title = "واحدها (${Formatters.number(units.size)})",
                trailing = {
                    IconButton(onClick = onAddUnit) { Icon(Icons.Filled.Add, contentDescription = "افزودن واحد") }
                },
            ) {
                if (units.isEmpty()) {
                    Text(
                        "هنوز واحدی ثبت نشده — با دکمه + واحد اضافه کنید یا از «ساخت گروهی» استفاده کنید",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    units.take(12).forEach { unit ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(unit.displayTitle, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    listOfNotNull(
                                        unit.grossArea?.let { "${Formatters.number(it.toInt())} م²" },
                                        unit.direction,
                                        Constants.unitTypeLabel(unit.unitType),
                                    ).joinToString(" • "),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            StatusChip(
                                Constants.unitStatusLabel(unit.status),
                                ir.pishfile.app.ui.screens.dashboard.unitStatusColor(unit.status),
                            )
                            IconButton(onClick = { onOpenUnit(unit.id) }) {
                                Icon(Icons.Filled.Note, contentDescription = "جزئیات")
                            }
                        }
                    }
                    if (units.size > 12) {
                        Text(
                            "و ${Formatters.number(units.size - 12)} واحد دیگر…",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        item {
            SectionCard(title = "پیش‌فایل‌های پروژه (${Formatters.number(preFiles.size)})") {
                if (preFiles.isEmpty()) {
                    Text("پیش‌فایلی برای این پروژه ثبت نشده", style = MaterialTheme.typography.bodySmall)
                } else {
                    preFiles.take(10).forEach { row ->
                        Card(
                            onClick = { onOpenPreFile(row.preFile.id) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                        ) {
                            Column(Modifier.padding(10.dp)) {
                                Text(
                                    "${row.preFile.draftNumber} • ${row.customerName ?: "—"}",
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                                Text(
                                    "${row.unitTitle ?: ""} • ${Formatters.amountShort(row.preFile.effectivePrice)} تومان",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
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
            message = "همه‌ی واحدها و پیش‌فایل‌های این پروژه حذف می‌شوند.",
            onConfirm = { viewModel.delete(onBack) },
            onDismiss = { showDelete = false },
        )
    }
}
