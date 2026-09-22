package ir.pishfile.app.ui.screens.units

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
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import ir.pishfile.app.data.local.entity.UnitEntity
import ir.pishfile.app.ui.AppViewModelProvider
import ir.pishfile.app.ui.components.ConfirmDialog
import ir.pishfile.app.ui.components.DropdownField
import ir.pishfile.app.ui.components.EmptyState
import ir.pishfile.app.ui.components.FilterChipsRow
import ir.pishfile.app.ui.components.FormTextField
import ir.pishfile.app.ui.components.InfoRow
import ir.pishfile.app.ui.components.JalaliDateField
import ir.pishfile.app.ui.components.MoneyField
import ir.pishfile.app.ui.components.MultiSelectChips
import ir.pishfile.app.ui.components.NumberField
import ir.pishfile.app.ui.components.SearchField
import ir.pishfile.app.ui.components.SectionCard
import ir.pishfile.app.ui.components.SoftDivider
import ir.pishfile.app.ui.components.SpacerH
import ir.pishfile.app.ui.components.StatusChip
import ir.pishfile.app.ui.components.unitStatusColor
import ir.pishfile.app.ui.viewmodel.UnitDetailViewModel
import ir.pishfile.app.ui.viewmodel.UnitEditViewModel
import ir.pishfile.app.ui.viewmodel.UnitForm
import ir.pishfile.app.ui.viewmodel.UnitListViewModel

@Composable
private fun UnitCard(unit: UnitEntity, onClick: () -> Unit, onDelete: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(unit.displayTitle, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(
                        listOfNotNull(
                            unit.grossArea?.let { "${Formatters.number(it.toInt())} مترمربع" },
                            Constants.unitTypeLabel(unit.unitType),
                            unit.direction,
                            unit.bedrooms?.let { "${Formatters.number(it)} خواب" },
                        ).joinToString(" • "),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                StatusChip(Constants.unitStatusLabel(unit.status), unitStatusColor(unit.status))
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
                }
            }
            SpacerH(6)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    "قیمت کل: ${Formatters.amountWithUnit(unit.finalPrice ?: unit.totalPrice)}",
                    style = MaterialTheme.typography.labelMedium,
                )
                Text(
                    "هر متر: ${Formatters.amountShort(unit.pricePerMeter)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
fun UnitEditScreen(
    unitId: String?,
    projectId: String?,
    onBack: () -> Unit,
    onSaved: (String) -> Unit,
    viewModel: UnitEditViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    if (unitId == null) viewModel.startNew(projectId) else viewModel.load(unitId)

    val form = viewModel.form
    val projects = viewModel.projects

    var showBatchDialog by remember { mutableStateOf(false) }
    var batchCount by remember { mutableStateOf(0) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            SectionCard(title = "شناسه واحد") {
                DropdownField(
                    label = "پروژه *",
                    options = projects.map { it.name },
                    selected = projects.firstOrNull { it.id == form.projectId }?.name,
                    onSelect = { name ->
                        val id = projects.firstOrNull { it.name == name }?.id.orEmpty()
                        viewModel.update { it.copy(projectId = id) }
                    },
                )
                SpacerH(8)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FormTextField(
                        value = form.block,
                        onValueChange = { v -> viewModel.update { it.copy(block = v) } },
                        label = "بلوک",
                        modifier = Modifier.weight(1f),
                    )
                    FormTextField(
                        value = form.unitNumber,
                        onValueChange = { v -> viewModel.update { it.copy(unitNumber = v) } },
                        label = "شماره واحد *",
                        modifier = Modifier.weight(1f),
                    )
                }
                SpacerH(8)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumberField(
                        value = form.floor,
                        onValueChange = { v -> viewModel.update { it.copy(floor = v) } },
                        label = "طبقه",
                        modifier = Modifier.weight(1f),
                    )
                    DropdownField(
                        label = "نوع",
                        options = UnitForm.typeOptions(),
                        selected = form.unitType,
                        onSelect = { v -> viewModel.update { it.copy(unitType = v) } },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        item {
            SectionCard(title = "متراژ و معماری") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumberField(
                        value = form.grossArea,
                        onValueChange = { v -> viewModel.update { it.copy(grossArea = v) } },
                        label = "متراژ ناخالص",
                        suffix = "م²",
                        decimal = true,
                        modifier = Modifier.weight(1f),
                    )
                    NumberField(
                        value = form.netArea,
                        onValueChange = { v -> viewModel.update { it.copy(netArea = v) } },
                        label = "متراژ مفید",
                        suffix = "م²",
                        decimal = true,
                        modifier = Modifier.weight(1f),
                    )
                }
                SpacerH(8)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumberField(
                        value = form.bedrooms,
                        onValueChange = { v -> viewModel.update { it.copy(bedrooms = v) } },
                        label = "خواب",
                        modifier = Modifier.weight(1f),
                    )
                    DropdownField(
                        label = "جهت واحد",
                        options = Constants.unitDirections,
                        selected = form.direction.takeIf { it.isNotBlank() },
                        onSelect = { v -> viewModel.update { it.copy(direction = v) } },
                        allowEmpty = true,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        item {
            SectionCard(title = "قیمت‌گذاری") {
                MoneyField(
                    value = form.pricePerMeter,
                    onValueChange = { v -> viewModel.update { it.copy(pricePerMeter = v) } },
                    label = "قیمت هر مترمربع",
                )
                SpacerH(8)
                MoneyField(
                    value = form.totalPrice.ifBlank { form.computedTotalPrice()?.toString().orEmpty() },
                    onValueChange = { v -> viewModel.update { it.copy(totalPrice = v) } },
                    label = "قیمت کل واحد",
                    helperText = "از متراژ × قیمت هر متر محاسبه می‌شود",
                )
            }
        }

        item {
            SectionCard(title = "وضعیت و تحویل") {
                DropdownField(
                    label = "وضعیت واحد",
                    options = Constants.unitStatuses.map { Constants.unitStatusLabel(it) },
                    selected = Constants.unitStatusLabel(form.status),
                    onSelect = { label ->
                        val code = Constants.unitStatuses.firstOrNull { Constants.unitStatusLabel(it) == label } ?: form.status
                        viewModel.update { it.copy(status = code) }
                    },
                )
                SpacerH(8)
                JalaliDateField(
                    value = form.deliveryDate,
                    onValueChange = { v -> viewModel.update { it.copy(deliveryDate = v) } },
                    label = "تاریخ تحویل",
                )
                SpacerH(8)
                FormTextField(
                    value = form.description,
                    onValueChange = { v -> viewModel.update { it.copy(description = v) } },
                    label = "توضیحات",
                    singleLine = false,
                    minLines = 2,
                )
            }
        }

        if (unitId == null) {
            item {
                OutlinedButton(
                    onClick = { showBatchDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("ساخت گروهی واحدها (طبقه × واحد در طبقه)") }
            }
        }

        item {
            Button(
                onClick = { viewModel.save { id -> onSaved(id) } },
                modifier = Modifier.fillMaxWidth(),
            ) { Text(if (unitId == null) "ثبت واحد" else "ذخیره تغییرات") }
        }
        item {
            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("انصراف") }
        }
    }

    if (showBatchDialog) {
        AlertDialog(
            onDismissRequest = { showBatchDialog = false },
            title = { Text("ساخت گروهی واحدها") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "برای هر طبقه، تعدادی واحد با متراژ پله‌ای ساخته می‌شود.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    FormTextField(
                        value = viewModel.batchBlock,
                        onValueChange = { viewModel.batchBlock = it },
                        label = "نام بلوک",
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        NumberField(
                            value = viewModel.batchFromFloor,
                            onValueChange = { viewModel.batchFromFloor = it },
                            label = "از طبقه",
                            modifier = Modifier.weight(1f),
                        )
                        NumberField(
                            value = viewModel.batchToFloor,
                            onValueChange = { viewModel.batchToFloor = it },
                            label = "تا طبقه",
                            modifier = Modifier.weight(1f),
                        )
                    }
                    NumberField(
                        value = viewModel.batchUnitsPerFloor,
                        onValueChange = { viewModel.batchUnitsPerFloor = it },
                        label = "تعداد واحد در هر طبقه",
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        NumberField(
                            value = viewModel.batchBaseArea,
                            onValueChange = { viewModel.batchBaseArea = it },
                            label = "متراژ پایه",
                            suffix = "م²",
                            decimal = true,
                            modifier = Modifier.weight(1f),
                        )
                        NumberField(
                            value = viewModel.batchAreaStep,
                            onValueChange = { viewModel.batchAreaStep = it },
                            label = "افزایش هر طبقه",
                            suffix = "م²",
                            decimal = true,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    if (batchCount > 0) {
                        Text(
                            "${Formatters.number(batchCount)} واحد ساخته شد ✅",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.createBatch { count -> batchCount = count }
                }) { Text("ساخت واحدها") }
            },
            dismissButton = {
                TextButton(onClick = { showBatchDialog = false }) { Text("بستن") }
            },
        )
    }
}

@Composable
fun UnitDetailScreen(
    unitId: String,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onNewPreFile: () -> Unit,
    onOpenPreFile: (String) -> Unit,
    viewModel: UnitDetailViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    viewModel.setUnitId(unitId)

    val unit by viewModel.unit.collectAsStateWithLifecycle()
    val project by viewModel.project.collectAsStateWithLifecycle()
    val preFiles by viewModel.preFiles.collectAsStateWithLifecycle()
    var showDelete by remember { mutableStateOf(false) }

    val current = unit ?: return

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            SectionCard(
                title = current.displayTitle,
                subtitle = project?.name,
                trailing = {
                    Row {
                        IconButton(onClick = onEdit) { Icon(Icons.Filled.Edit, contentDescription = "ویرایش") }
                        IconButton(onClick = { showDelete = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                },
            ) {
                StatusChip(Constants.unitStatusLabel(current.status), unitStatusColor(current.status))
                SpacerH(10)
                InfoRow("نوع", Constants.unitTypeLabel(current.unitType))
                InfoRow("طبقه", Formatters.number(current.floor))
                InfoRow("بلوک", current.block)
                InfoRow("متراژ ناخالص", current.grossArea?.let { "${Formatters.number(it.toInt())} مترمربع" })
                InfoRow("خواب", current.bedrooms?.let { Formatters.number(it) })
                InfoRow("جهت", current.direction)
                InfoRow("تحویل", current.deliveryDate?.let { Formatters.toPersianDigits(it) })
            }
        }

        item {
            SectionCard(title = "قیمت‌گذاری") {
                InfoRow("قیمت هر متر", Formatters.amountWithUnit(current.pricePerMeter))
                InfoRow("قیمت کل", Formatters.amountWithUnit(current.totalPrice), emphasize = true)
            }
        }

        item {
            SectionCard(title = "فایل‌های پیش‌فروش این واحد (${Formatters.number(preFiles.size)})") {
                if (preFiles.isEmpty()) {
                    Text("فایل پیش‌فروشی برای این واحد ثبت نشده", style = MaterialTheme.typography.bodySmall)
                } else {
                    preFiles.forEach { row ->
                        Card(
                            onClick = { onOpenPreFile(row.preFile.id) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                        ) {
                            Column(Modifier.padding(10.dp)) {
                                Text(
                                    "${row.preFile.draftNumber} • مالک: ${row.preFile.ownerName ?: "—"}",
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                                Text(
                                    "قیمت کل: ${Formatters.amountShort(row.preFile.displayPrice)} تومان • ${Constants.preFileStatusLabel(row.preFile.status)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
                SpacerH(10)
                Button(onClick = onNewPreFile, modifier = Modifier.fillMaxWidth()) {
                    Text("ثبت پیش‌فروش برای این واحد")
                }
            }
        }

        item {
            SoftDivider()
            SpacerH(6)
            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("بازگشت") }
        }
    }

    if (showDelete) {
        ConfirmDialog(
            title = "حذف واحد",
            message = "واحد ${current.unitNumber} حذف شود؟",
            onConfirm = { viewModel.delete(onBack) },
            onDismiss = { showDelete = false },
        )
    }
}
